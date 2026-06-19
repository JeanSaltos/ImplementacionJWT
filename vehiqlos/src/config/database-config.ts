import { TypeOrmModuleOptions } from '@nestjs/typeorm';
import { Vehiqlo } from '../vehiqlos/entities/vehiqlo.entity';
import { Auto } from '../vehiqlos/entities/auto.entity';
import { Camioneta } from '../vehiqlos/entities/camioneta.entity';
import { Moto } from '../vehiqlos/entities/moto.entity';

/**
 * Configuración de TypeORM para conectar con PostgreSQL.
 *
 * Se usa en `AppModule` con `TypeOrmModule.forRoot(databaseConfig)`.
 * Los valores pueden sobrescribirse mediante variables de entorno.
 */
export const databaseConfig: TypeOrmModuleOptions = {
  type: 'postgres',
  host: process.env.DB_HOST || 'localhost',
  port: Number(process.env.DB_PORT) || 5432,
  username: process.env.DB_USER || process.env.DB_USUARIO || 'postgres',
  password: process.env.DB_PASS || process.env.DB_PASSWORD || process.env.DB_CONTRASENA || '123',
  database: process.env.DB_NAME || 'vehiqlos',
  // Las entidades deben estar registradas para que TypeORM pueda crear las tablas.
  entities: [Vehiqlo, Auto, Camioneta, Moto],
  // En desarrollo podemos habilitar synchronize; en producción usar migraciones.
  synchronize: process.env.NODE_ENV !== 'production',
};
