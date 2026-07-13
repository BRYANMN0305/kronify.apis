# Hoja de Ruta de Desarrollo - Plataforma de Reservas

## Principio de division del trabajo

La division no sera "backend vs. frontend". La division sera por eje vertical de dominio: cada persona toma modulos completos de punta a punta, desde entidades y reglas hasta API e integracion frontend.

Llamemos a las personas **Dev A** y **Dev B**.

El unico punto que ambos deben construir juntos es la base comun del sistema. En esta version del MVP se simplifica la identidad: un usuario tiene un solo perfil global, **Cliente** o **Negocio**.

---

## Fase 0 - Cimientos compartidos

**Duracion sugerida: primeros 3-5 dias.**

- Definir el esquema inicial de base de datos: `users`, `oauth_account`, `business`, `employee`, `customer`, `service`, `employee_service`, `weekly_schedule`, `schedule_block`, `appointment`, `review`, `plan`, `business_plan`.
- Definir la regla de perfil global: `profileType = CLIENT | BUSINESS`.
- Definir el contrato de JWT: `userId`, `email`, `profileType`.
- Definir reglas basicas de permisos:
  - `CLIENT`: puede reservar, ver sus citas, cancelar segun politica y dejar resenas.
  - `BUSINESS`: puede crear/gestionar negocio o actuar como empleado si tiene registro `Employee`.
- Configurar estructura base del backend Spring/Kotlin por capas.
- Configurar estructura base del frontend.
- Configurar Flyway para migraciones versionadas.
- Configurar CI basico: build y tests.

**Rama:** trabajar desde `develop` con ramas cortas `feature/*`.

---

## Fase 1 en adelante - Division por eje vertical

### Dev A - Identidad, negocio y empleados

Dueno de todo lo relacionado con cuentas, negocio, empleados, servicios y planes.

| Modulo | Contenido |
|---|---|
| Modulo 1 | Autenticacion email/password, OAuth2, JWT con `profileType`, panel personal |
| Modulo 2 | Invitaciones de empleados, validacion de perfil `BUSINESS`, registro `Employee`, flag `owner`, autogestion de horario |
| Modulo 3 | Configuracion del negocio, CRUD de servicios, asignacion de servicios a empleados |
| Modulo 7 | Freemium: limites de servicios/citas y validacion de plan |

**Ramas sugeridas:**

```text
feature/auth-email-password
feature/auth-oauth2-google
feature/auth-jwt-profile-type
feature/user-settings-panel
feature/employee-invitations
feature/employee-schedule-permissions
feature/business-config
feature/services-crud
feature/freemium-limits
```

### Dev B - Disponibilidad, reservas y cliente

Dueno de todo lo relacionado con calculo de disponibilidad, flujo publico de reserva, agenda y resenas.

| Modulo | Contenido |
|---|---|
| Modulo 4 | Motor de disponibilidad: horarios semanales, bloqueos, citas existentes y calculo de slots |
| Modulo 5 | Pagina publica por slug, flujo de reserva invitado/cliente, autocompletado para cliente registrado, calendario |
| Modulo 6 | Resenas de clientes registrados con citas completadas |

**Ramas sugeridas:**

```text
feature/availability-engine-core
feature/availability-engine-blocks
feature/availability-engine-tests
feature/public-business-page
feature/booking-flow
feature/booking-autofill
feature/employee-agenda-view
feature/calendar-unified-filter
feature/reviews
```

---

## Por que esta division

- Dev A deja listas las bases de identidad y negocio que Dev B necesita consultar.
- Dev B puede avanzar el motor de disponibilidad con fixtures mientras existen los endpoints reales.
- La regla de perfil unico reduce integracion: no hay selector de contexto ni roles por tenant.
- Cada eje sigue siendo demostrable de forma independiente.

## Punto de integracion a vigilar

El flujo de reserva necesita leer `Business`, `Service`, `Employee`, `EmployeeService`, `WeeklySchedule`, `ScheduleBlock` y `Appointment`.

Mientras el CRUD real no este completo, Dev B puede trabajar con datos mockeados usando las mismas estructuras de dominio acordadas en Fase 0.

Reglas compartidas importantes:

- Solo `CLIENT` puede reservar como cliente registrado.
- Un cliente invitado se guarda en `Customer` sin `User`.
- Solo `BUSINESS` puede ser propietario o empleado.
- Una cita debe usar servicio y empleado del mismo negocio.
- El empleado debe tener asignado el servicio.

---

## Estrategia de ramas

```text
main      -> produccion / entrega final
develop   -> integracion continua
feature/* -> una rama por tarea concreta
```

**Reglas practicas:**

- Ramas cortas, idealmente 2-4 dias.
- PR obligatorio hacia `develop`.
- Rebase o merge frecuente desde `develop`.
- Nadie hace push directo a `main`.

## Sincronizacion sugerida

- **Daily corto:** que se hizo, que sigue, bloqueos.
- **Checkpoint semanal:** integrar ambos ejes y probar flujo completo: crear negocio -> invitar empleado -> configurar horario -> cliente reserva.
- **Contrato compartido temprano:** antes de separarse, acordar entidades de dominio de `User`, `Business`, `Employee`, `Customer`, `Service`, `WeeklySchedule`, `ScheduleBlock` y `Appointment`.
