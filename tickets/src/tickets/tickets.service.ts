import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { CreateTicketDto } from './dto/create-ticket.dto';
import { UpdateTicketDto } from './dto/update-ticket.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Ticket } from './entities/ticket.entity';
import { Repository } from 'typeorm';
import { ConfigService } from '@nestjs/config';
import { TicketResponseDto } from './dto/ticket-response.dto';

@Injectable()
export class TicketsService {
  constructor(
    @InjectRepository(Ticket)
    private readonly ticketRepository: Repository<Ticket>,
    private readonly configService: ConfigService,
  ) {}

  private async authenticatedFetch(url: string, options: RequestInit = {}, token?: string): Promise<Response> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);

    const headers = {
      ...(options.headers || {}),
      'Content-Type': 'application/json',
    };

    if (token) {
      headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
        signal: controller.signal,
      });
      clearTimeout(timeoutId);
      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      throw error;
    }
  }

  async create(createTicketDto: CreateTicketDto, token?: string): Promise<Ticket> {
    const ticketActivoVehiculo = await this.ticketRepository.findOne({
      where: { placa: createTicketDto.placa, activo: true }
    });
    if (ticketActivoVehiculo) {
      throw new BadRequestException(`El vehículo con placa ${createTicketDto.placa} ya tiene un ticket o reserva activa`);
    }

    const msPersonaUrl = this.configService.get<string>('MS_PERSONA') || '';
    const msZonasUrl = this.configService.get<string>('MS_ZONAS') || '';
    const msVehiculosUrl = this.configService.get<string>('MS_VEHICULOS') || '';

    // 1. Verificar Espacio en zonas-espacios
    let espacioData: any;
    try {
      const res = await this.authenticatedFetch(`${msZonasUrl}/${createTicketDto.idEspacio}`, {}, token);
      if (!res.ok) {
        throw new NotFoundException(`El espacio con ID ${createTicketDto.idEspacio} no existe en zonas-espacios`);
      }
      espacioData = await res.json();
    } catch (error: any) {
      if (error instanceof NotFoundException) throw error;
      throw new BadRequestException(`No se pudo conectar con el servicio de zonas: ${error.message}`);
    }

    if (espacioData.estado !== 'DISPONIBLE') {
      throw new BadRequestException(`El espacio ${espacioData.nombre} no está disponible (Estado actual: ${espacioData.estado})`);
    }

    // 2. Verificar Vehículo en vehiqlos
    let vehiculoExiste = false;
    try {
      const res = await this.authenticatedFetch(msVehiculosUrl, {}, token);
      if (!res.ok) {
        throw new BadRequestException(`El servicio de vehículos devolvió un estado incorrecto: ${res.status}`);
      }
      const vehiculos: any[] = await res.json();
      vehiculoExiste = vehiculos.some(v => v.placa === createTicketDto.placa);
    } catch (error: any) {
      if (error instanceof BadRequestException) throw error;
      throw new BadRequestException(`No se pudo conectar con el servicio de vehículos: ${error.message}`);
    }

    if (!vehiculoExiste) {
      throw new NotFoundException(`El vehículo con placa ${createTicketDto.placa} no está registrado`);
    }

    // 3. Verificar Usuario en usuarios (DNI)
    let usuarioExiste = false;
    try {
      const res = await this.authenticatedFetch(msPersonaUrl, {}, token);
      if (!res.ok) {
        throw new BadRequestException(`El servicio de usuarios devolvió un estado incorrecto: ${res.status}`);
      }
      const usuarios: any[] = await res.json();
      usuarioExiste = usuarios.some(u => u.person && String(u.person.dni) === String(createTicketDto.dni));
    } catch (error: any) {
      if (error instanceof BadRequestException) throw error;
      throw new BadRequestException(`No se pudo conectar con el servicio de usuarios: ${error.message}`);
    }

    if (!usuarioExiste) {
      throw new NotFoundException(`El usuario con DNI ${createTicketDto.dni} no está registrado`);
    }

    // 4. Crear Ticket
    const ticket = this.ticketRepository.create({
      placa: createTicketDto.placa,
      dni: createTicketDto.dni,
      idEspacio: createTicketDto.idEspacio,
      nombreZona: espacioData.nombreZona || 'Sin Zona',
      fechhaHoraIngreso: new Date(),
      activo: true,
    });

    const savedTicket = await this.ticketRepository.save(ticket);

    // 5. Ocupar el espacio
    try {
      const patchRes = await this.authenticatedFetch(`${msZonasUrl}/${createTicketDto.idEspacio}/estado/OCUPADO`, {
        method: 'PATCH',
      }, token);
      if (!patchRes.ok) {
        console.error(`No se pudo actualizar el estado del espacio a OCUPADO en zonas-espacios: ${patchRes.statusText}`);
      }
    } catch (error: any) {
      console.error(`Error al enviar PATCH para ocupar espacio:`, error);
    }

    return savedTicket;
  }

  async findAll(): Promise<Ticket[]> {
    return this.ticketRepository.find();
  }

  async findOne(id: string): Promise<Ticket> {
    const ticket = await this.ticketRepository.findOne({ where: { id } });
    if (!ticket) {
      throw new NotFoundException(`Ticket con ID ${id} no encontrado`);
    }
    return ticket;
  }

  async update(id: string, updateTicketDto: UpdateTicketDto, token?: string): Promise<Ticket> {
    const ticket = await this.findOne(id);

    if (updateTicketDto.activo === false) {
      if (!ticket.activo) {
        throw new BadRequestException(`El ticket con ID ${id} ya está cerrado/inactivo`);
      }
      ticket.fechhaHoraSalida = new Date();
      ticket.activo = false;

      // Calcular valorRecaudo
      const diffMs = ticket.fechhaHoraSalida.getTime() - ticket.fechhaHoraIngreso.getTime();
      const diffHours = diffMs / (1000 * 60 * 60);
      const horas = Math.max(1, Math.ceil(diffHours));
      
      const tarifaHoraStr = this.configService.get<string>('TARIFA_HORA') || '1.0';
      const tarifaHora = parseFloat(tarifaHoraStr);
      ticket.valorRecaudo = horas * tarifaHora;

      // Liberar espacio en zonas-espacios
      const msZonasUrl = this.configService.get<string>('MS_ZONAS') || '';
      try {
        const patchRes = await this.authenticatedFetch(`${msZonasUrl}/${ticket.idEspacio}/estado/DISPONIBLE`, {
          method: 'PATCH',
        }, token);
        if (!patchRes.ok) {
          console.error(`No se pudo actualizar el estado del espacio a DISPONIBLE en zonas-espacios: ${patchRes.statusText}`);
        }
      } catch (error: any) {
        console.error(`Error al enviar PATCH para liberar espacio:`, error);
      }
    }

    if (updateTicketDto.placa !== undefined) ticket.placa = updateTicketDto.placa;
    if (updateTicketDto.dni !== undefined) ticket.dni = updateTicketDto.dni;
    if (updateTicketDto.idEspacio !== undefined) ticket.idEspacio = updateTicketDto.idEspacio;
    if (updateTicketDto.valorRecaudo !== undefined) ticket.valorRecaudo = updateTicketDto.valorRecaudo;

    return this.ticketRepository.save(ticket);
  }

  async remove(id: string): Promise<void> {
    const ticket = await this.findOne(id);
    await this.ticketRepository.remove(ticket);
  }

  async mapToResponseDto(ticket: Ticket, token?: string): Promise<TicketResponseDto> {
    const msPersonaUrl = this.configService.get<string>('MS_PERSONA') || '';
    let datosPersona = 'Usuario no encontrado';

    try {
      const res = await this.authenticatedFetch(msPersonaUrl, {}, token);
      if (res.ok) {
        const usuarios: any[] = await res.json();
        const user = usuarios.find(u => u.person && String(u.person.dni) === String(ticket.dni));
        if (user && user.person) {
          const person = user.person;
          datosPersona = `${person.firstName} ${person.middleName ? person.middleName + ' ' : ''}${person.lastName}`.trim();
        }
      }
    } catch (error: any) {
      console.error(`Error al conectar con el servicio de usuarios para obtener datos de la persona: ${error.message}`);
    }

    const salida = ticket.fechhaHoraSalida ? new Date(ticket.fechhaHoraSalida) : new Date();
    const diffMs = salida.getTime() - new Date(ticket.fechhaHoraIngreso).getTime();
    const diffHours = diffMs / (1000 * 60 * 60);
    const tiempoHoras = Math.max(1, Math.ceil(diffHours));

    const tarifaHoraStr = this.configService.get<string>('TARIFA_HORA') || '1.0';
    const tarifaHora = parseFloat(tarifaHoraStr);
    const ValorRecaudo = ticket.valorRecaudo !== null && ticket.valorRecaudo !== undefined
      ? Number(ticket.valorRecaudo)
      : tiempoHoras * tarifaHora;

    return {
      id: ticket.id,
      placa: ticket.placa,
      dni: ticket.dni,
      datosPersona,
      idEspacio: ticket.idEspacio,
      zona: ticket.nombreZona || 'Sin Zona',
      fechaHoraIngreso: ticket.fechhaHoraIngreso,
      fechaHoraSalida: ticket.fechhaHoraSalida || null,
      ValorRecaudo,
      activo: ticket.activo,
      tiempoHoras,
    } as any;
  }

  async emitir(id: string, token?: string): Promise<TicketResponseDto> {
    const ticket = await this.findOne(id);
    return this.mapToResponseDto(ticket, token);
  }

  async reservar(createTicketDto: CreateTicketDto, token?: string): Promise<Ticket> {
    const ticketActivoVehiculo = await this.ticketRepository.findOne({
      where: { placa: createTicketDto.placa, activo: true }
    });
    if (ticketActivoVehiculo) {
      throw new BadRequestException(`El vehículo con placa ${createTicketDto.placa} ya tiene un ticket o reserva activa`);
    }

    const msPersonaUrl = this.configService.get<string>('MS_PERSONA') || '';
    const msZonasUrl = this.configService.get<string>('MS_ZONAS') || '';
    const msVehiculosUrl = this.configService.get<string>('MS_VEHICULOS') || '';

    // 1. Verificar Espacio en zonas-espacios
    let espacioData: any;
    try {
      const res = await this.authenticatedFetch(`${msZonasUrl}/${createTicketDto.idEspacio}`, {}, token);
      if (!res.ok) {
        throw new NotFoundException(`El espacio con ID ${createTicketDto.idEspacio} no existe en zonas-espacios`);
      }
      espacioData = await res.json();
    } catch (error: any) {
      if (error instanceof NotFoundException) throw error;
      throw new BadRequestException(`No se pudo conectar con el servicio de zonas: ${error.message}`);
    }

    if (espacioData.estado !== 'DISPONIBLE') {
      throw new BadRequestException(`El espacio ${espacioData.nombre} no está disponible (Estado actual: ${espacioData.estado})`);
    }

    // 2. Verificar Vehículo en vehiqlos
    let vehiculoExiste = false;
    try {
      const res = await this.authenticatedFetch(msVehiculosUrl, {}, token);
      if (!res.ok) {
        throw new BadRequestException(`El servicio de vehículos devolvió un estado incorrecto: ${res.status}`);
      }
      const vehiculos: any[] = await res.json();
      vehiculoExiste = vehiculos.some(v => v.placa === createTicketDto.placa);
    } catch (error: any) {
      if (error instanceof BadRequestException) throw error;
      throw new BadRequestException(`No se pudo conectar con el servicio de vehículos: ${error.message}`);
    }

    if (!vehiculoExiste) {
      throw new NotFoundException(`El vehículo con placa ${createTicketDto.placa} no está registrado`);
    }

    // 3. Verificar Usuario en usuarios (DNI)
    let usuarioExiste = false;
    try {
      const res = await this.authenticatedFetch(msPersonaUrl, {}, token);
      if (!res.ok) {
        throw new BadRequestException(`El servicio de usuarios devolvió un estado incorrecto: ${res.status}`);
      }
      const usuarios: any[] = await res.json();
      usuarioExiste = usuarios.some(u => u.person && String(u.person.dni) === String(createTicketDto.dni));
    } catch (error: any) {
      if (error instanceof BadRequestException) throw error;
      throw new BadRequestException(`No se pudo conectar con el servicio de usuarios: ${error.message}`);
    }

    if (!usuarioExiste) {
      throw new NotFoundException(`El usuario con DNI ${createTicketDto.dni} no está registrado`);
    }

    // 4. Crear Ticket con esReserva = true
    const ticket = this.ticketRepository.create({
      placa: createTicketDto.placa,
      dni: createTicketDto.dni,
      idEspacio: createTicketDto.idEspacio,
      nombreZona: espacioData.nombreZona || 'Sin Zona',
      fechhaHoraIngreso: new Date(),
      activo: true,
      esReserva: true,
    });

    const savedTicket = await this.ticketRepository.save(ticket);

    // 5. Reservar el espacio
    try {
      const patchRes = await this.authenticatedFetch(`${msZonasUrl}/${createTicketDto.idEspacio}/estado/RESERVADO`, {
        method: 'PATCH',
      }, token);
      if (!patchRes.ok) {
        console.error(`No se pudo actualizar el estado del espacio a RESERVADO en zonas-espacios: ${patchRes.statusText}`);
      }
    } catch (error: any) {
      console.error(`Error al enviar PATCH para reservar espacio:`, error);
    }

    return savedTicket;
  }

  async confirmarReserva(id: string, token?: string): Promise<Ticket> {
    const ticket = await this.findOne(id);

    if (!ticket.activo) {
      throw new BadRequestException(`El ticket con ID ${id} ya no está activo`);
    }

    if (!ticket.esReserva) {
      throw new BadRequestException(`El ticket con ID ${id} no es una reserva`);
    }

    // Cambiar a ocupación normal
    ticket.esReserva = false;
    ticket.fechhaHoraIngreso = new Date();

    const savedTicket = await this.ticketRepository.save(ticket);

    // Actualizar el estado del espacio a OCUPADO en zonas-espacios
    const msZonasUrl = this.configService.get<string>('MS_ZONAS') || '';
    try {
      const patchRes = await this.authenticatedFetch(`${msZonasUrl}/${ticket.idEspacio}/estado/OCUPADO`, {
        method: 'PATCH',
      }, token);
      if (!patchRes.ok) {
        console.error(`No se pudo actualizar el estado del espacio a OCUPADO en zonas-espacios: ${patchRes.statusText}`);
      }
    } catch (error: any) {
      console.error(`Error al enviar PATCH para ocupar espacio desde reserva:`, error);
    }

    return savedTicket;
  }
}
