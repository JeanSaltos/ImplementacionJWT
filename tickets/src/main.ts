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
    .setTitle('Microservicio Tickets – Parqueaderito')
    .setDescription(
      'API para gestión de tickets de ingreso, salida y reservas de vehículos en el parqueadero. ' +
      'Integra con los microservicios de vehículos, usuarios y zonas-espacios.',
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
      'JWT-auth', // This name must match the one used in the @ApiBearerAuth decorator
    )
    .addTag('Tickets', 'Operaciones sobre tickets de parqueo')
    .build();

  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api', app, document, {
    swaggerOptions: { persistAuthorization: true },
    customSiteTitle: 'Tickets API Docs',
  });

  await app.listen(process.env.PORT ?? 3000);
  console.log(`Tickets MS corriendo en puerto ${process.env.PORT ?? 3000}`);
  console.log(`Swagger UI disponible en http://localhost:${process.env.PORT ?? 3000}/api`);
}
bootstrap();
