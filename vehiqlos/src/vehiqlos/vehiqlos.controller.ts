import { Controller, Get, Post, Body, Patch, Param, Delete, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiParam, ApiBody, ApiBearerAuth } from '@nestjs/swagger';
import { VehiqlosService } from './vehiqlos.service';
import { CreateVehiqloDto } from './dto/create-vehiqlo.dto';
import { UpdateVehiqloDto } from './dto/update-vehiqlo.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { RolesGuard } from '../auth/roles.guard';
import { Roles } from '../auth/roles.decorator';

@ApiTags('Vehículos')
@ApiBearerAuth('JWT-auth')
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('vehiqlos')
export class VehiqlosController {
  constructor(private readonly vehiqlosService: VehiqlosService) { }

  @Post()
  @Roles('ADMIN')
  @ApiOperation({
    summary: 'Registrar un nuevo vehículo',
    description:
      'Registra un vehículo en el sistema. Se debe indicar el tipo (Auto, Moto, Camioneta) y los datos específicos. ' +
      'Formato de placa Ecuador: Auto/Camioneta → AAA-1234 | Moto → AB-123-C.',
  })
  @ApiBody({ type: CreateVehiqloDto })
  @ApiResponse({ status: 201, description: 'Vehículo registrado exitosamente' })
  @ApiResponse({ status: 400, description: 'Formato de placa inválido, datos incorrectos o vehículo ya registrado' })
  create(@Body() createVehiqloDto: CreateVehiqloDto) {
    return this.vehiqlosService.create(createVehiqloDto);
  }

  @Get()
  @Roles('ADMIN', 'OPERADOR', 'SUPERVISOR', 'SERVICE_TICKETS')
  @ApiOperation({ summary: 'Listar todos los vehículos registrados o buscar por placa' })
  @ApiResponse({ status: 200, description: 'Lista de todos los vehículos' })
  findAll(@Query('placa') placa?: string) {
    if (placa) {
      return this.vehiqlosService.findByPlaca(placa);
    }
    return this.vehiqlosService.findAll();
  }

  @Get(':id')
  @Roles('ADMIN', 'OPERADOR', 'SUPERVISOR', 'SERVICE_TICKETS')
  @ApiOperation({ summary: 'Obtener un vehículo por ID' })
  @ApiParam({ name: 'id', description: 'UUID del vehículo', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
  @ApiResponse({ status: 200, description: 'Datos del vehículo' })
  @ApiResponse({ status: 404, description: 'Vehículo no encontrado' })
  findOne(@Param('id') id: string) {
    return this.vehiqlosService.findOne(id);
  }

  @Patch(':id')
  @Roles('ADMIN')
  @ApiOperation({
    summary: 'Actualizar datos de un vehículo',
    description: 'Permite actualizar parcialmente los datos de un vehículo. La placa no puede estar registrada en otro vehículo.',
  })
  @ApiParam({ name: 'id', description: 'UUID del vehículo', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
  @ApiBody({ type: UpdateVehiqloDto })
  @ApiResponse({ status: 200, description: 'Vehículo actualizado exitosamente' })
  @ApiResponse({ status: 400, description: 'La placa ya está registrada en otro vehículo' })
  @ApiResponse({ status: 404, description: 'Vehículo no encontrado' })
  update(@Param('id') id: string, @Body() updateVehiqloDto: UpdateVehiqloDto) {
    return this.vehiqlosService.update(id, updateVehiqloDto);
  }

  @Delete(':id')
  @Roles('ADMIN')
  @ApiOperation({ summary: 'Eliminar un vehículo por ID' })
  @ApiParam({ name: 'id', description: 'UUID del vehículo', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
  @ApiResponse({ status: 200, description: 'Vehículo eliminado exitosamente' })
  @ApiResponse({ status: 404, description: 'Vehículo no encontrado' })
  remove(@Param('id') id: string) {
    return this.vehiqlosService.remove(id);
  }
}
