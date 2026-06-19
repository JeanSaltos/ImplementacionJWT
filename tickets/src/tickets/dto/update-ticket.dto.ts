import { PartialType } from '@nestjs/mapped-types';
import { CreateTicketDto } from './create-ticket.dto';
import { IsBoolean, IsNumber, IsOptional } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateTicketDto extends PartialType(CreateTicketDto) {
    @ApiProperty({
        description: 'Estado del ticket. Enviar false para cerrar el ticket y calcular el cobro',
        example: false,
        required: false,
    })
    @IsBoolean()
    @IsOptional()
    activo?: boolean;

    @ApiProperty({
        description: 'Valor recaudado por la estadía (en USD)',
        example: 2.50,
        required: false,
    })
    @IsNumber()
    @IsOptional()
    valorRecaudo?: number;
}
