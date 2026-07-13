# Plataforma de Reservas y Gestión de Citas

## 1. Descripción general

Plataforma web genérica de reservas y gestión de citas para negocios de servicios (barberías, salones de belleza, spas, consultorios, entrenadores personales, talleres, etc.). A diferencia de un sistema atado a un solo rubro, cada negocio configura sus propios servicios, empleados y horarios, obteniendo una página pública propia para que sus clientes agenden citas online.

El proyecto está pensado como **MVP con potencial real de venta** (modelo freemium), no solo como ejercicio académico, aplicando **arquitectura por capas** (dominio, aplicación, infraestructura, presentación) y diseño **API-first** para permitir en el futuro una app mobile sin modificar el backend.

**Alcance del MVP:** solo la parte web (frontend + backend). La app mobile queda fuera del MVP.

---

## 2. Roles del sistema

El MVP usa un **perfil global único por cuenta**. Al registrarse, una persona elige
entre **Cliente** o **Negocio**. La misma cuenta no puede ser Cliente en un lugar y
Dueño/Empleado en otro; esta simplificación evita selector de contextos y reduce
la complejidad de permisos.

### 2.1 Prestador (Dueño del negocio)
Cuenta de tipo **Negocio** que registra el negocio, gestiona empleados, servicios, horarios generales y ve reportes globales. Puede además auto-asignarse como Empleado si él mismo atiende citas.

### 2.2 Empleado
Cuenta de tipo **Negocio** con credenciales propias, asociada a un Prestador por invitación. Gestiona su propia agenda, su horario de atención y los servicios que puede realizar. No tiene acceso a configuración general del negocio ni a reportes financieros globales.

- El Empleado **no se auto-registra como empleado**: es agregado por el Prestador mediante invitación por email.
- Si el correo invitado ya pertenece a una cuenta Cliente, esa cuenta no puede aceptar la invitación; debe usarse una cuenta de tipo Negocio.
- Si el Prestador también atiende citas, se marca como `es_dueño = true` dentro de su propio registro de Empleado, usando las mismas credenciales de Negocio.

### 2.3 Cliente
Puede agendar de dos formas:
- **Invitado**: solo nombre y contacto (teléfono/email). Permisos limitados: no puede cancelar, reprogramar, ver historial ni dejar reseña.
- **Con cuenta**: cuenta de tipo **Cliente**. Puede cancelar, reprogramar, ver historial completo y dejar reseñas en citas completadas. Una cuenta Cliente no puede administrar negocios ni aceptar invitaciones de empleado.

---

## 3. Modelo de página pública por negocio

- Cada Prestador obtiene una URL propia mediante **slug**: `tuapp.com/{slug}` (ej: `tuapp.com/barberia-el-corte`).
- El slug debe ser único, en minúsculas, sin espacios (guiones permitidos), validado contra duplicados.
- Existe un endpoint público (sin autenticación) que retorna información del negocio, servicios, empleados y disponibilidad dado el slug.
- Se descarta el uso de subdominios para el MVP por la complejidad de configurar wildcard DNS/SSL en hosting gratuito (Vercel).
- La página pública muestra: nombre y descripción del negocio, servicios ofrecidos, calendario de disponibilidad, reseñas, botón de agendar.

---

## 4. Entidades principales

| Entidad | Descripción |
|---|---|
| **Usuario** | Cuenta global del sistema. Tiene un único tipo: Cliente o Negocio. |
| **Prestador** | Usuario de tipo Negocio que crea y administra un negocio. |
| **Negocio** | Datos públicos y operativos del negocio: nombre, slug, descripción, categoría, plan, teléfono WhatsApp. |
| **Empleado** | Usuario de tipo Negocio que atiende citas en un negocio. Puede ser el mismo dueño (`es_dueño`). |
| **Servicio** | Servicio ofrecido por el negocio: nombre, duración, precio, descripción. Pertenece a un Prestador. |
| **EmpleadoServicio** | Relación muchos-a-muchos: qué empleados pueden realizar qué servicios. |
| **HorarioEmpleado** | Disponibilidad semanal configurada por cada empleado (días, horas de inicio/fin). |
| **BloqueoHorario** | Excepciones puntuales: ausencia de un empleado, cierre por festivo, etc. |
| **Cliente** | Persona que agenda citas. Puede ser invitado o tener una cuenta global de tipo Cliente. |
| **Cita** | Reserva: Prestador, Empleado, Servicio, Cliente, fecha/hora, estado (pendiente, confirmada, completada, cancelada, no-show). |
| **Reseña** | Calificación y comentario, asociada a una Cita completada y a un Cliente con cuenta. |

---

## 5. Lógica de dominio central: motor de disponibilidad

Es el núcleo del sistema, aislado en la capa de dominio (sin dependencias de base de datos ni HTTP).

**Entrada:** un Servicio + una Fecha + (opcionalmente un Empleado específico elegido por el cliente).

**Proceso:**
1. Determinar qué empleados pueden realizar ese servicio (`EmpleadoServicio`).
2. Obtener el horario de atención de cada uno de esos empleados para esa fecha (`HorarioEmpleado`).
3. Restar bloqueos puntuales (`BloqueoHorario`).
4. Restar citas ya existentes de cada empleado ese día (`Cita`).
5. Retornar los slots libres resultantes.

**Modos de reserva del cliente:**
- **Sin preferencia de empleado:** el sistema asigna automáticamente cualquier empleado disponible en el slot elegido.
- **Con preferencia de empleado:** el cliente elige un empleado específico y solo ve los horarios libres de esa persona.

---

## 6. Casos de uso — Prestador (Dueño del negocio)

### Configuración inicial
1. Registrarse con perfil global "Soy Negocio" y crear el negocio (nombre, categoría, descripción, slug).
2. Configurar datos públicos de la página (descripción, teléfono de WhatsApp, categoría).
3. Crear y gestionar servicios (crear, editar, eliminar; nombre, duración, precio).

### Gestión de empleados
4. Agregar empleado (nombre, email para invitación, servicios que puede realizar).
5. Editar/eliminar empleado.
6. Asignar/editar qué servicios puede realizar cada empleado.
7. Auto-asignarse como empleado si el dueño también atiende citas.

### Gestión de horarios
8. Configurar horario de atención por empleado (días, horas).
9. Registrar bloqueos puntuales (ausencias, festivos) por empleado.

### Gestión de agenda
10. Ver agenda general del negocio (todos los empleados, vista tipo calendario con columnas).
11. Filtrar la agenda por uno o varios empleados específicos (incluyendo "solo yo" si el dueño atiende).
12. Crear cita manualmente (para clientes que llaman o llegan sin usar la app).
13. Reprogramar o cancelar una cita.
14. Marcar cita como completada, cancelada o no-show.

### Gestión de clientes
15. Ver listado de clientes con historial de citas.
16. Ver ficha detallada de un cliente (citas pasadas, servicios más usados).
17. Agregar notas internas sobre un cliente.

### Negocio y reportes
18. Ver reporte de ingresos (por día/semana/mes).
19. Ver dashboard con métricas (citas del día, ocupación semanal, clientes nuevos vs. recurrentes).
20. Recibir notificación cuando un cliente agenda o cancela.

---

## 7. Casos de uso — Empleado

1. Iniciar sesión con credenciales propias (creadas por invitación del Prestador).
2. Ver su propia agenda (no la de otros empleados).
3. Configurar su propio horario de atención.
4. Registrar sus propios bloqueos/ausencias puntuales.
5. Marcar sus citas como completadas o no-show.
6. Ver historial de sus propios clientes atendidos.

**Restricciones:** no puede crear/eliminar servicios del negocio, no ve reportes financieros generales, no gestiona otros empleados, no cambia configuración del negocio.

---

## 8. Casos de uso — Cliente

### Descubrimiento y reserva
1. Acceder a la página pública del negocio mediante su slug.
2. Ver servicios ofrecidos, empleados disponibles y reseñas del negocio.
3. Elegir servicio y (opcionalmente) empleado específico.
4. Ver disponibilidad real (slots libres calculados por el motor de disponibilidad).
5. Agendar cita como invitado (nombre + contacto) o con cuenta Cliente.
6. Recibir confirmación de la cita.
7. Contactar al prestador vía WhatsApp tras confirmar la reserva (botón con enlace `wa.me` y mensaje precargado con fecha/hora/servicio).

### Gestión de citas (solo cliente con cuenta)
8. Ver sus próximas citas.
9. Cancelar o reprogramar una cita propia (con reglas de anticipación mínima).
10. Ver historial de citas pasadas.
11. Dejar reseña y calificación en una cita completada.

### Perfil (solo cliente con cuenta)
12. Registrarse / iniciar sesión con perfil global "Soy Cliente".
13. Editar su perfil (nombre, contacto).

---

## 9. Botón de contacto por WhatsApp

Implementación de bajo costo técnico usando enlace `wa.me`, sin necesidad de API de WhatsApp Business:

```
https://wa.me/57XXXXXXXXXX?text=Hola,%20quiero%20confirmar%20mi%20cita%20del%20[fecha]%20a%20las%20[hora]
```

Requiere: campo de teléfono de WhatsApp en el Prestador (o Empleado), y armar el enlace dinámicamente en el frontend tras confirmar la reserva.

---

## 10. Modelo de negocio: Freemium

- **Plan gratis:** límite de servicios configurados y/o número de citas mensuales, sin recordatorios automáticos, sin reportes avanzados.
- **Plan pro (pago mensual):** servicios y citas ilimitadas, recordatorios automáticos, reportes avanzados, personalización de página pública.
- No se integra pasarela de pago en el MVP (queda para una versión posterior). El campo `plan` en el Prestador simplemente habilita/limita funcionalidades.

---

## 11. Decisiones de arquitectura

- **Backend:** Kotlin, arquitectura por capas (dominio / aplicación / infraestructura / presentación), API-first (JWT para autenticación, sin sesiones/cookies) para soportar futura app mobile sin cambios.
- **Frontend:** SPA (framework a definir), alojado gratuitamente en Vercel.
  - Requiere configurar rewrites en `vercel.json` para rutas dinámicas (slug) en SPA:
    ```json
    { "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }] }
    ```
- **Backend hosting:** Vercel no es apto para Ktor (requiere proceso JVM persistente, no serverless). Se evalúa hosting alternativo (ej. Oracle Cloud Free Tier).
- **CORS:** debe configurarse correctamente en Ktor dado que frontend y backend estarán en dominios distintos.
- **Slug en ruta**, no subdominio, para evitar complejidad de wildcard DNS/SSL en el MVP.

---

## 12. Fuera de alcance del MVP

- Aplicación mobile nativa.
- Marketplace / búsqueda entre negocios (cada negocio tiene su página independiente).
- Integración de pasarela de pago.
- Notificaciones vía WhatsApp Business API (se usa enlace `wa.me` simple).
- Cuentas separadas para empleados con roles granulares más allá de lo descrito.
- Cuentas con múltiples perfiles simultáneos (por ejemplo, Cliente en un negocio y Empleado/Dueño en otro).
