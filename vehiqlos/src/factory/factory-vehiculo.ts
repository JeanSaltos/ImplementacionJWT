
import { Auto } from '../vehiqlos/entities/auto.entity';
import { Camioneta } from '../vehiqlos/entities/camioneta.entity';
import { Moto } from '../vehiqlos/entities/moto.entity';
import { Vehiqlo } from '../vehiqlos/entities/vehiqlo.entity';
import { CreateVehiqloDto } from '../vehiqlos/dto/create-vehiqlo.dto';


export class FactoryVehiculos {
    static crear(dto: CreateVehiqloDto): Vehiqlo {
        const tipoNormalizado = (dto.tipo || '').toLowerCase();
        switch (tipoNormalizado) {
            case 'auto':
                const auto = new Auto();
                Object.assign(auto, dto.datos);
                return auto;
            case 'moto':
            case 'motocicleta':
                const moto = new Moto();
                Object.assign(moto, dto.datos);
                return moto;
            case 'camion':
            case 'camioneta':
                const camion = new Camioneta();
                Object.assign(camion, dto.datos);
                return camion;
            default:
                throw new Error(`Tipo de vehículo no soportado: ${dto.tipo}`);
        }
    }
}