import { IsNotEmpty, IsString, IsUUID, Matches } from "class-validator";
import { ApiProperty } from "@nestjs/swagger";

export class CreateTicketDto {

    @ApiProperty({
        description: 'Placa del vehículo. Auto/Camioneta: AAA-1234, Moto: AB-123-C',
        example: 'ABC-1234',
    })
    @IsString()
    @IsNotEmpty()
    @Matches(/^([A-Za-z]{3}-\d{4}|[A-Z]{2}-\d{3}-[A-Z])$/, {
        message: 'Formato de placa no válido. Use AAA-1234 para autos/camionetas o AB-123-C para motos',
    })
    placa!: string;

    @ApiProperty({
        description: 'Número de cédula (DNI) del propietario del vehículo',
        example: '1712345678',
    })
    @IsString()
    @IsNotEmpty()
    dni!: string;

    @ApiProperty({
        description: 'UUID del espacio de parqueo a ocupar',
        example: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    })
    @IsUUID()
    @IsNotEmpty()
    idEspacio!: string;

}
