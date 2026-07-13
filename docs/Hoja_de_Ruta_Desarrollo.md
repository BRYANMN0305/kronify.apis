# Hoja de Ruta de Desarrollo — Plataforma de Reservas

## Principio de división del trabajo

La división no es "backend vs. frontend" (eso los obliga a esperarse constantemente el uno al otro). La división es por **eje vertical de dominio**: cada persona es dueña de un conjunto de módulos de punta a punta (dominio + API + integración frontend de eso específico), minimizando puntos de espera mutua.

Llamemos a las personas **Dev A** y **Dev B**.

El único punto que **ambos deben construir juntos, antes que nada**, es la base común de la que todo lo demás depende (Fase 0). Sin eso bien cerrado, cualquier división posterior genera conflictos de integración.

---

## Fase 0 — Cimientos compartidos (ambos, en pareja o muy coordinados)

**Duración sugerida: primeros 3-5 días. No paralelizar esta fase, o el resto del proyecto hereda inconsistencias.**

- Definir el esquema de base de datos inicial (tablas núcleo: `usuario`, `negocio`, `usuario_negocio_rol`, `invitacion`)
- Definir el "contrato de JWT" (claims exactos: `userId`, `email`, roles por tenant, `es_dueño`)
- Configurar el esqueleto del proyecto Ktor (estructura de capas: `domain`, `application`, `infrastructure`, `api`)
- Configurar el esqueleto del proyecto Vue 3 (routing base, estructura de carpetas, cliente HTTP con interceptor de JWT)
- Configurar Flyway para migraciones versionadas
- Configurar CI básico (lint + build) para evitar romper `main`

**Rama:** trabajar directo sobre `develop` con commits pequeños y frecuentes, o `feature/fundacion` fusionada rápido. Esta fase no debe vivir más de una semana en una rama separada.

---

## Fase 1 en adelante — División por eje vertical

### 🔵 Dev A — Eje: Identidad, Negocio y Empleados

Dueño de todo lo relacionado a **quién es cada usuario y cómo se organiza el negocio**.

| Módulo | Contenido |
|---|---|
| Módulo 1 | Autenticación (email/contraseña, OAuth2 Google, JWT), panel de configuración de usuario |
| Módulo 2 | Invitaciones de empleados, vinculación de cuentas, flag `es_dueño`, permisos de autogestión de horario (RF32/33) |
| Módulo 3 | Configuración del negocio, CRUD de servicios, asignación de servicios a empleados |
| Módulo 7 | Freemium: límites y validación de plan (porque depende directamente de servicios/negocio) |

**Ramas sugeridas (naming: `feature/{modulo}-{descripcion}`):**
```
feature/auth-email-password
feature/auth-oauth2-google
feature/auth-jwt-contract
feature/user-settings-panel
feature/employee-invitations
feature/employee-schedule-permissions
feature/business-config
feature/services-crud
feature/freemium-limits
```

### 🟢 Dev B — Eje: Disponibilidad, Reservas y Experiencia del Cliente

Dueño de todo lo relacionado a **cuándo se puede agendar y cómo se agenda**.

| Módulo | Contenido |
|---|---|
| Módulo 4 | Motor de disponibilidad (horarios semanales, bloqueos, cálculo de slots) — pieza crítica, prioridad #1 |
| Módulo 5 | Página pública por slug, flujo de reserva, autocompletado de datos, agenda/calendario unificado |
| Módulo 6 | Reseñas |

**Ramas sugeridas:**
```
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

## Por qué esta división y no otra

- **Minimiza dependencias cruzadas del día a día:** Dev A puede terminar todo su eje sin necesitar que el motor de disponibilidad exista. Dev B necesita que existan `usuario`, `negocio` y `servicio` (de la Fase 0), pero no necesita esperar a que Dev A termine invitaciones o freemium para avanzar en el motor.
- **El motor de disponibilidad queda con dueño único y foco total** (Dev B), que es justo la pieza que identificamos como más riesgosa — más vale que una sola persona la domine a fondo para la sustentación, en vez de repartirla y que ninguno la explique bien.
- **Cada eje es demostrable de forma independiente**, lo cual ayuda mucho para ir mostrando avances parciales sin depender del otro módulo.

## Único punto real de integración a vigilar

El **flujo de reserva** (Dev B) necesita leer `servicios` y `empleados` (Dev A). Mientras Dev A no tenga el CRUD de servicios listo, Dev B puede:
- Trabajar el motor de disponibilidad con datos mockeados/fixtures propios (no depende de la API real, solo de estructuras de dominio ya acordadas en la Fase 0).
- Integrar con la API real de servicios/empleados solo cuando Dev A la publique.

Por eso es clave que en la Fase 0 ambos acuerden **las entidades de dominio (data classes) de `Servicio`, `Empleado`, `HorarioSemanal`, `Bloqueo`, `Cita`** antes de separarse — aunque cada quien implemente su parte, si el "contrato" de esas estructuras está fijo desde el día uno, pueden trabajar en paralelo sin bloquearse.

---

## Estrategia de ramas (Git)

```
main            → producción / entrega final, solo merge desde develop
develop         → integración continua, ambos hacen merge aquí vía PR
feature/*       → una rama por tarea concreta (ver tablas arriba), siempre desde develop
```

**Reglas prácticas:**
- Ramas cortas (2-4 días máximo). Si una tarea se extiende más, es señal de que hay que partirla en tareas más chicas.
- PR obligatorio hacia `develop`, aunque sean solo ustedes dos — el otro revisa (aunque sea rápido), para que ambos entiendan el código completo de cara a la sustentación.
- Rebase frecuente desde `develop` en ramas largas (como `feature/availability-engine-core`) para evitar conflictos grandes al final.
- Nadie hace push directo a `main` o `develop`.

## Sincronización sugerida

- **Daily corto (10-15 min):** qué se hizo, qué sigue, algún bloqueo.
- **Checkpoint de integración cada ~1 semana:** merge de ambos ejes a `develop`, correr todo junto, verificar que el flujo completo (crear negocio → invitar empleado → configurar horario → cliente reserva) funcione de punta a punta.
- **Definir juntos el contrato de entidades de dominio ANTES de separarse** (esto no es negociable — es la bisagra de todo el paralelismo).

¿Quieres que arme también un orden sugerido de sprints/semanas (qué se espera tener listo en cada checkpoint), o con esta división de ejes y ramas es suficiente por ahora?
