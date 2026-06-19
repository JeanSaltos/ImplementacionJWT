import { Type } from "class-transformer";
import { IsIn, IsInt, IsNotEmpty, IsNumber, IsString, Matches, Max, MaxLength, Min, MinLength, ValidateNested } from "class-validator";
import { ApiProperty, ApiExtraModels } from "@nestjs/swagger";

class BaseVehiculoDto {
    @ApiProperty({
        description: 'Placa del vehículo. Auto/Camioneta: AAA-1234 | Moto: AB-123-C',
        example: 'ABC-1234',
    })
    @Matches(/^[A-Za-z]{3}-[0-9]{4}$/, { message: "Formato de placa no valido" })
    @IsString()
    placa!: string;

    @ApiProperty({ description: 'Marca del vehículo (3-15 letras)', example: 'Toyota' })
    @IsString()
    @IsNotEmpty({ message: "La marca es obligatoria" })
    @MinLength(3, { message: "La marca debe tener al menos 3 caracteres" })
    @MaxLength(15, { message: "La marca debe tener menos de 15 caracteres" })
    @Matches(/^[A-Za-záéíóúÁÉÍÓÚñÑ\s]+$/, { message: "La marca solo puede contener letras" })
    marca!: string;

    @ApiProperty({ description: 'Modelo del vehículo (3-20 letras)', example: 'Corolla' })
    @IsString()
    @IsNotEmpty({ message: "El modelo es obligatorio" })
    @MinLength(3, { message: "El modelo debe tener al menos 3 caracteres" })
    @MaxLength(20, { message: "El modelo debe tener menos de 20 caracteres" })
    @Matches(/^[A-Za-záéíóúÁÉÍÓÚñÑ\s]+$/, { message: "El modelo solo puede contener letras" })
    modelo!: string;

    @ApiProperty({ description: 'Color del vehículo (4-20 letras)', example: 'Rojo' })
    @IsString()
    @IsNotEmpty()
    @MinLength(4, { message: "El color debe tener al menos 4 caracteres" })
    @MaxLength(20, { message: "El color debe tener menos de 20 caracteres" })
    @Matches(/^[A-Za-záéíóúÁÉÍÓÚñÑ\s]+$/, { message: "El color solo puede contener letras" })
    color!: string;

    @ApiProperty({ description: 'Año de fabricación del vehículo (1900-2026)', example: 2022 })
    @IsNumber()
    @Min(1900, { message: "El año debe ser mayor a 1900" })
    @Max(2026, { message: "El año debe ser menor a 2026" })
    @IsInt()
    @IsNotEmpty()
    @Matches(/^[0-9]+$/, { message: "El año debe ser un número" })
    anio!: number;
}

class AutoDto extends BaseVehiculoDto {
    @ApiProperty({ description: 'Número de puertas del auto (2-5)', example: 4 })
    @IsNumber()
    @Min(2, { message: "El numero de puertas debe ser minimo 2" })
    @Max(5, { message: "El numero de puertas debe ser maximo 5" })
    @IsInt()
    @IsNotEmpty()
    @Matches(/^[0-9]+$/, { message: "El numero de puertas debe ser un número" })
    numPuertas!: number;

    @ApiProperty({ description: 'Capacidad del maletero en litros (100-1000)', example: 450 })
    @IsNumber()
    @Min(100, { message: 'La capacidad del maletero debe ser al menos 100 litros' })
    @Max(1000, { message: 'La capacidad del maletero no puede ser mayor a 1000 litros' })
    @IsInt()
    @IsNotEmpty()
    capacidadMaletero!: number;
}

class CamionetaDto extends BaseVehiculoDto {
    @ApiProperty({ description: 'Tipo de cabina (Simple, Doble, Extra)', example: 'Doble' })
    @IsString()
    @IsNotEmpty()
    @MinLength(2, { message: "El numero de puertas debe ser minimo 2" })
    @MaxLength(4, { message: "El numero de puertas debe ser maximo 4" })
    @Matches(/^[A-Za-záéíóúÁÉÍÓÚñÑ\s]+$/, { message: "El numero de puertas solo puede contener letras" })
    cabina!: string;

    @ApiProperty({ description: 'Capacidad de carga en kg (500-3000)', example: 1000 })
    @IsNumber()
    @IsNotEmpty()
    @Min(500, { message: "La capacidad de carga debe ser minimo 500kg" })
    @Max(3000, { message: "La capacidad de carga no puede ser mayor a 3000kg" })
    @IsInt()
    @IsNotEmpty()
    capacidadCarga!: number;
}

class MotoDto extends BaseVehiculoDto {
    @ApiProperty({
        description: 'Placa de la moto en formato ecuatoriano: AB-123-C',
        example: 'AB-123-C',
    })
    @IsString()
    @Matches(/^[A-Z]{2}-[0-9]{3}-[A-Z]{1}$/, { message: "Formato de placa no valido" })
    declare placa: string;

    @ApiProperty({ description: 'Cilindraje del motor en cc (50-1500)', example: 150 })
    @IsNumber()
    @IsInt()
    @IsNotEmpty()
    @Min(50, { message: "El cilindraje debe ser minimo 50cc" })
    @Max(1500, { message: "El cilindraje no puede ser mayor a 1500cc" })
    cilindraje!: number;

    @ApiProperty({ description: 'Tipo de moto (ej: Deportiva, Scooter, Enduro)', example: 'Scooter' })
    @IsString()
    @IsNotEmpty()
    @MinLength(3, { message: "El tipo debe tener al menos 3 caracteres" })
    @MaxLength(20, { message: "El tipo debe tener menos de 20 caracteres" })
    @Matches(/^[A-Za-záéíóúÁÉÍÓÚñÑ\s]+$/, { message: "El tipo solo puede contener letras" })
    tipo!: string;
}

export class CreateVehiqloDto {
    @ApiProperty({
        description: 'Tipo de vehículo',
        enum: ['Auto', 'Moto', 'Camioneta'],
        example: 'Auto',
    })
    @IsIn(['Auto', 'Moto', 'Camioneta', 'auto', 'moto', 'motocicleta', 'camion', 'camioneta'])
    tipo!: string;

    @ApiProperty({
        description:
            'Datos del vehículo según su tipo. ' +
            'Auto: { placa, marca, modelo, color, anio, numPuertas, capacidadMaletero }. ' +
            'Moto: { placa (AB-123-C), marca, modelo, color, anio, cilindraje, tipo }. ' +
            'Camioneta: { placa, marca, modelo, color, anio, cabina, capacidadCarga }.',
        example: { placa: 'ABC-1234', marca: 'Toyota', modelo: 'Corolla', color: 'Rojo', anio: 2022, numPuertas: 4, capacidadMaletero: 450 },
    })
    @ValidateNested()
    @Type((opts) => {
        const object = opts?.object as CreateVehiqloDto;
        if (!object || !object.tipo) return BaseVehiculoDto;

        switch (object.tipo.toLowerCase()) {
            case 'auto':
                return AutoDto;
            case 'moto':
            case 'motocicleta':
                return MotoDto;
            case 'camion':
            case 'camioneta':
                return CamionetaDto;
            default:
                return BaseVehiculoDto;
        }
    })
    datos!: AutoDto | MotoDto | CamionetaDto;
}
