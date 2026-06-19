import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { CreateVehiqloDto } from './dto/create-vehiqlo.dto';
import { UpdateVehiqloDto } from './dto/update-vehiqlo.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Vehiqlo } from './entities/vehiqlo.entity';
import { Repository } from 'typeorm';
import { FactoryVehiculos } from 'src/factory/factory-vehiculo';

@Injectable()
export class VehiqlosService {
  constructor(
    @InjectRepository(Vehiqlo)
    private repositoryVehiqlo: Repository<Vehiqlo>
  ) { }
  async create(createVehiqloDto: CreateVehiqloDto): Promise<Vehiqlo> {
    const existe = await this.repositoryVehiqlo.findOne({
      where: {
        placa: createVehiqloDto.datos.placa,
      }
    });
    if (existe) {
      throw new BadRequestException("El vehiqlo ya existe");
    }
    const vehiqlo = FactoryVehiculos.crear(createVehiqloDto);
    return this.repositoryVehiqlo.save(vehiqlo);
  }

  async findAll(): Promise<Vehiqlo[]> {
    return this.repositoryVehiqlo.find();
  }

  async findByPlaca(placa: string): Promise<Vehiqlo[]> {
    return this.repositoryVehiqlo.find({
      where: { placa }
    });
  }

  async findOne(id: string): Promise<Vehiqlo> {
    const vehiqlo = await this.repositoryVehiqlo.findOne({
      where: { id }
    });
    if (!vehiqlo) {
      throw new NotFoundException("El vehiqlo no existe");
    }
    return vehiqlo;
  }


  async update(id: string, updateVehiqloDto: UpdateVehiqloDto): Promise<Vehiqlo> {
    const vehiqlo = await this.findOne(id);

    if (updateVehiqloDto.datos) {
      if (updateVehiqloDto.datos.placa && updateVehiqloDto.datos.placa !== vehiqlo.placa) {
        const existePlaca = await this.repositoryVehiqlo.findOne({
          where: { placa: updateVehiqloDto.datos.placa }
        });
        if (existePlaca) {
          throw new BadRequestException("La placa ya está registrada en otro vehículo");
        }
      }
      Object.assign(vehiqlo, updateVehiqloDto.datos);
    }

    return this.repositoryVehiqlo.save(vehiqlo);
  }

  async remove(id: string): Promise<void> {
    const vehiqlo = await this.findOne(id);
    await this.repositoryVehiqlo.remove(vehiqlo);
  }
}
