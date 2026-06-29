# Telegram

## Objetivo

Enviar notificaciones del resultado del pipeline de GitHub Actions.

## Secretos requeridos

| Secret | Descripcion |
|---|---|
| `TELEGRAM_BOT_TOKEN` | Token generado por BotFather. |
| `TELEGRAM_CHAT_ID` | Identificador del chat, grupo o canal destino. |

## Configuracion

1. Crear un bot en Telegram usando BotFather.
2. Guardar el token como `TELEGRAM_BOT_TOKEN`.
3. Obtener el chat id del destino.
4. Guardar el valor como `TELEGRAM_CHAT_ID`.
5. Ejecutar el workflow.

## Comportamiento

Si los secretos no existen, el job `notify` no falla el pipeline. Solo omite la notificacion para permitir trabajo local o forks sin credenciales.
