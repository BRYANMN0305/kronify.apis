# Modelo de perfiles

El sistema usa un perfil global por usuario para simplificar el MVP.

## Regla principal

Un usuario solo puede tener un `profileType`:

- `CLIENT`: usuario que agenda citas.
- `BUSINESS`: usuario de negocio. Puede ser propietario y, si atiende citas,
  tambien tener registro en `Employee`.

No se permite que la misma cuenta sea cliente en un negocio y empleado o dueno
en otro.

## Entidades

- `User`: identidad y tipo global de cuenta.
- `Business`: negocio creado por un usuario `BUSINESS`.
- `Employee`: usuario `BUSINESS` que atiende citas dentro de un negocio.
- `Customer`: ficha de reserva para usuario `CLIENT` o cliente invitado.
- `Service`: servicio ofrecido por un negocio.
- `EmployeeService`: servicios que puede realizar cada empleado.
- `WeeklySchedule`: horario semanal base del empleado.
- `ScheduleBlock`: bloqueos puntuales del empleado.
- `Appointment`: cita entre cliente, servicio y empleado.
- `Review`: resena asociada a una cita completada.
- `Plan` y `BusinessPlan`: limites freemium del negocio.

## Flujo simple

1. El usuario se registra como `CLIENT` o `BUSINESS`.
2. Si es `CLIENT`, puede reservar citas y se usa/crea una ficha `Customer`.
3. Si es `BUSINESS`, puede crear un `Business`.
4. Si el dueno tambien atiende, se crea un `Employee` con `owner = true`.
5. Si un empleado acepta invitacion, se crea su `User` tipo `BUSINESS` y su
   registro `Employee`.

## Reglas de aplicacion

- Solo usuarios `BUSINESS` pueden crear negocios o ser empleados.
- Solo usuarios `CLIENT` pueden actuar como clientes registrados.
- Los clientes invitados se guardan en `Customer` con `userId = null`.
- Una cita debe usar un empleado y servicio del mismo negocio.
- El empleado debe tener asignado el servicio en `EmployeeService`.
