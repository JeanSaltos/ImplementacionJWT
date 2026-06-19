import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { VehiqlosService } from './vehiqlos.service';
import { VehiqlosController } from './vehiqlos.controller';
import { Vehiqlo } from './entities/vehiqlo.entity';
import { Auto } from './entities/auto.entity';
import { Camioneta } from './entities/camioneta.entity';
import { Moto } from './entities/moto.entity';
import { AuthModule } from '../auth/auth.module';

@Module({
  imports: [TypeOrmModule.forFeature([Vehiqlo, Auto, Camioneta, Moto]), AuthModule],
  controllers: [VehiqlosController],
  providers: [VehiqlosService],
})
export class VehiqlosModule {}
