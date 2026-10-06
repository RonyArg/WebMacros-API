# WebMacros API

API REST para gestionar usuarios, autenticación, cálculos nutricionales e historial de calorías y macronutrientes de WebMacros.

## Funcionalidades previstas

- Registro e inicio de sesión de usuarios.
- Autenticación y autorización mediante Spring Security.
- Cálculo de calorías y macronutrientes.
- Registro del historial de cálculos nutricionales.
- Consulta y gestión del historial personal de cada usuario.
- Validación de los datos recibidos por la API.

## Funcionalidades implementadas

- Implementar el registro de usuarios mediante `POST /auth/register`.
- Validar los datos recibidos mediante Bean Validation.
- Normalizar nombres y correos electrónicos.
- Almacenar las contraseñas utilizando BCrypt.
- Persistir los usuarios en PostgreSQL.
- Asignar el rol `USER` por defecto.
- Gestionar las migraciones de base de datos con Flyway.
- Configurar el acceso público al endpoint de registro.
- Gestionar emails duplicados y errores de validación.
- Añadir tests para `AuthService` y `AuthController`.

## Tecnologías

- Java 25
- Spring Boot
- Maven
- Spring Security
- Spring Data JPA
- PostgreSQL

## Estado

En desarrollo.