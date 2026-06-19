import { ApiProperty } from '@nestjs/swagger';

export class TicketResponseDto {
    @ApiProperty({ description: 'ID único del ticket (UUID)', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
    id!: string;

    @ApiProperty({ description: 'Placa del vehículo', example: 'ABC-1234' })
    placa!: string;

    @ApiProperty({ description: 'DNI del propietario', example: '1712345678' })
    dni!: string;

    @ApiProperty({ description: 'Nombre completo del propietario', example: 'Juan Pérez' })
    datosPersona?: string;

    @ApiProperty({ description: 'UUID del espacio asignado', example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' })
    idEspacio!: string;

    @ApiProperty({ description: 'Nombre de la zona de parqueo', example: 'Zona A' })
    zona!: string;

    @ApiProperty({ description: 'Fecha y hora de ingreso al parqueo' })
    fechaHoraIngreso!: Date;

    @ApiProperty({ description: 'Fecha y hora de salida del parqueo', nullable: true })
    fechaHoraSalida!: Date | null;

    @ApiProperty({ description: 'Valor cobrado en USD', example: 2.50 })
    ValorRecaudo!: number;

    @ApiProperty({ description: 'Indica si el ticket está activo', example: true })
    activo!: boolean;

    @ApiProperty({ description: 'Tiempo total de estadía en horas (redondeado hacia arriba)', example: 2 })
    tiempoHoras!: number;
}