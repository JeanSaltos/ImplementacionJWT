import { Controller, Get, Post, Body, Patch, Param, Delete, UseGuards, Headers } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiParam, ApiBody, ApiBearerAuth } from '@nestjs/swagger';
import { TicketsService } from './tickets.service';
import { CreateTicketDto } from './dto/create-ticket.dto';
import { UpdateTicketDto } from './dto/update-ticket.dto';
import { TicketResponseDto } from './dto/ticket-response.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RolesGuard } from '../auth/roles.guard';
import { Roles } from '../auth/roles.decorator';

@ApiTags('Tickets')
@ApiBearerAuth('JWT-auth')
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('tickets')
export class TicketsController {
  constructor(private readonly ticketsService: TicketsService) {}

  @Post('reservar')
  @Roles('ADMIN', 'OPERADOR')
  @ApiOperation({ summary: 'Crear una reserva de espacio de parqueo', description: 'Reserva un espacio sin que el vehículo haya ingresado aún. El espacio quedará en estado RESERVADO.' })
  @ApiBody({ type: CreateTicketDto })
  @ApiResponse({ status: 201, description: 'Reserva creada exitosamente', type: Object })
  @ApiResponse({ status: 400, description: 'Formato de placa inválido, espacio no disponible o vehículo/usuario ya tiene ticket activo' })
  @ApiResponse({ status: 404, description: 'Espacio, vehículo o usuario no encontrado' })
  reservar(@Body() createTicketDto: CreateTicketDto, @Headers('authorization') authHeader: string) {
    return this.ticketsService.reservar(createTicketDto, authHeader);
  }

  @Post()
  @Roles('ADMIN', 'OPERADOR')
  @ApiOperation({ summary: 'Registrar ingreso de vehículo al parqueo', description: 'Crea un ticket de ingreso. Verifica que el espacio esté disponible, el vehículo y el usuario existan.' })
  @ApiBody({ type: CreateTicketDto })
  @ApiResponse({ status: 201, description: 'Ticket de ingreso creado exitosamente', type: Object })
  @ApiResponse({ status: 400, description: 'Formato de placa inválido, espacio no disponible o datos incorrectos' })
  @ApiResponse({ status: 404, description: 'Espacio, vehículo o usuario no encontrado' })
  create(@Body() createTicketDto: CreateTicketDto, @Headers('authorization') authHeader: string) {
    return this.ticketsService.create(createTicketDto, authHeader);
  }

  @Get()
  @Roles('ADMIN', 'OPERADOR', 'SUPERVISOR')
  @ApiOperation({ summary: 'Listar todos los tickets', description: 'Retorna todos los tickets registrados en el sistema (activos e inactivos).' })
  @ApiResponse({ status: 200, description: 'Lista de tickets', type: [Object] })
  findAll() {
    return this.ticketsService.findAll();
  }

  @Get(':id/emitir')
  @Roles('ADMIN', 'OPERADOR', 'SUPERVISOR')
  @ApiOperation({ summary: 'Emitir comprobante de ticket', description: 'Retorna el detalle completo del ticket con datos del propietario, tiempo de estadía y valor a pagar.' })
  @ApiParam({ name: 'id', description: 'UUID del ticket', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
  @ApiResponse({ status: 200, description: 'Comprobante del ticket', type: TicketResponseDto })
  @ApiResponse({ status: 404, description: 'Ticket no encontrado' })
  emitir(@Param('id') id: string, @Headers('authorization') authHeader: string) {
    return this.ticketsService.emitir(id, authHeader);
  }

  @Get(':id')
  @Roles('ADMIN', 'OPERADOR', 'SUPERVISOR')
  @ApiOperation({ summary: 'Obtener ticket por ID' })
  @ApiParam({ name: 'id', description: 'UUID del ticket', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
  @ApiResponse({ status: 200, description: 'Datos del ticket', type: Object })
  @ApiResponse({ status: 404, description: 'Ticket no encontrado' })
  findOne(@Param('id') id: string) {
    return this.ticketsService.findOne(id);
  }

  @Patch(':id/confirmar')
  @Roles('ADMIN', 'OPERADOR')
  @ApiOperation({ summary: 'Confirmar una reserva', description: 'Convierte una reserva en un ticket de ocupación real. El espacio pasa de RESERVADO a OCUPADO y se registra la hora de ingreso.' })
  @ApiParam({ name: 'id', description: 'UUID del ticket de reserva', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
  @ApiResponse({ status: 200, description: 'Reserva confirmada exitosamente', type: Object })
  @ApiResponse({ status: 400, description: 'El ticket no es una reserva o ya está inactivo' })
  @ApiResponse({ status: 404, description: 'Ticket no encontrado' })
  confirmarReserva(@Param('id') id: string, @Headers('authorization') authHeader: string) {
    return this.ticketsService.confirmarReserva(id, authHeader);
  }

  @Patch(':id')
  @Roles('ADMIN', 'OPERADOR')
  @ApiOperation({ summary: 'Actualizar ticket / registrar salida', description: 'Para registrar la salida enviar { "activo": false }. Esto calcula el valor de cobro y libera el espacio.' })
  @ApiParam({ name: 'id', description: 'UUID del ticket', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
  @ApiBody({ type: UpdateTicketDto })
  @ApiResponse({ status: 200, description: 'Ticket actualizado exitosamente', type: Object })
  @ApiResponse({ status: 400, description: 'El ticket ya está cerrado' })
  @ApiResponse({ status: 404, description: 'Ticket no encontrado' })
  update(@Param('id') id: string, @Body() updateTicketDto: UpdateTicketDto, @Headers('authorization') authHeader: string) {
    return this.ticketsService.update(id, updateTicketDto, authHeader);
  }

  @Delete(':id')
  @Roles('ADMIN')
  @ApiOperation({ summary: 'Eliminar ticket por ID' })
  @ApiParam({ name: 'id', description: 'UUID del ticket', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
  @ApiResponse({ status: 200, description: 'Ticket eliminado exitosamente' })
  @ApiResponse({ status: 404, description: 'Ticket no encontrado' })
  remove(@Param('id') id: string) {
    return this.ticketsService.remove(id);
  }
}
