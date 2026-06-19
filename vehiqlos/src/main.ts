import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ValidationPipe } from '@nestjs/common';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import helmet from 'helmet';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  app.use(helmet());

  const corsAllowedOrigins = process.env.CORS_ALLOWED_ORIGINS
    ? process.env.CORS_ALLOWED_ORIGINS.split(',').map(o => o.trim())
    : ['http://localhost:5173', 'http://localhost:4200', 'http://localhost:3002'];

  app.enableCors({
    origin: corsAllowedOrigins,
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS',
    credentials: true,
  });

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );

  const config = new DocumentBuilder()
    .setTitle('Microservicio Vehículos – Parqueaderito')
    .setDescription(
      'API para gestión de vehículos registrados en el sistema de parqueo. ' +
      'Soporta tres tipos de vehículo: Auto, Moto y Camioneta, cada uno con sus atributos específicos. ' +
      'Formatos de placa Ecuador: Auto/Camioneta → AAA-1234 | Moto → AB-123-C',
    )
    .setVersion('1.0')
    .addBearerAuth(
      {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
        name: 'JWT',
        description: 'Ingrese el token JWT obtenido del microservicio usuarios',
        in: 'header',
      },
      'JWT-auth',
    )
    .addTag('Vehículos', 'Operaciones CRUD sobre vehículos registrados')
    .build();

  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api', app, document, {
    swaggerOptions: { persistAuthorization: true },
    customSiteTitle: 'Vehículos API Docs',
  });

  await app.listen(process.env.PORT ?? 3000);
  console.log(`Vehículos MS corriendo en puerto ${process.env.PORT ?? 3000}`);
  console.log(`Swagger UI disponible en http://localhost:${process.env.PORT ?? 3000}/api`);
}
bootstrap();
