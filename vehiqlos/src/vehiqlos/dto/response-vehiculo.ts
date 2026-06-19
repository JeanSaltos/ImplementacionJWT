import { TipoMoto } from "../entities/moto.entity";
import { Clasificacion } from "../entities/vehiqlo.entity";

export class ResponseVehiculo {
    id!: string;
    placa!: string;
    marca!: string;
    modelo!: string;
    color!: string;
    anio!: number;
    clasificacion!: Clasificacion;
    numeroPuertas!: number;
    capacidadMaletero!: number;
    capacidadCarga!: number;
    cabina!: string;
    cilindraje!: number;
    tipo!: TipoMoto;
    constructor(
        id: string,
        placa: string,
        marca: string,
        modelo: string,
        color: string,
        anio: number,
        clasificacion: Clasificacion
    ) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.color = color;
        this.anio = anio;
        this.clasificacion = clasificacion;
    }
}