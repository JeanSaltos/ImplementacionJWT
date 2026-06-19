import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { VehiqlosModule } from './vehiqlos/vehiqlos.module';
import { databaseConfig } from './config/database-config';
import { AuthModule } from './auth/auth.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true, envFilePath: '.env' }),
    TypeOrmModule.forRoot(databaseConfig),
    VehiqlosModule,
    AuthModule,
  ],
})
export class AppModule { }