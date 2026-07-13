# Requisitos del Sistema — Plataforma de Reservas y Gestión de Clientes

## Requisitos Funcionales (RF)

### Módulo 1 — Autenticación y gestión de cuentas

- **RF01** – El sistema debe permitir que todos los usuarios (Negocio/Prestador, Empleado y Cliente) se registren e inicien sesión mediante email/contraseña, OAuth2 con Google, u OAuth2 con Microsoft.
- **RF02** – Al crear una cuenta, el sistema debe presentar únicamente dos opciones de tipo de perfil: **"Soy Cliente"** o **"Soy Negocio"**. El rol Empleado no se elige en este paso; se asigna al aceptar una invitación.
- **RF03** – El sistema debe permitir que cualquier usuario se registre e inicie sesión vía OAuth2, vinculando la identidad del proveedor a una cuenta interna del sistema.
- **RF04** – Si un usuario se registra por OAuth2 y luego intenta iniciar sesión con el mismo correo mediante método tradicional (o viceversa), el sistema debe manejar la vinculación/conflicto de cuentas de forma consistente (unificar por email verificado).
- **RF05** – El sistema debe emitir un JWT propio tras una autenticación exitosa (tradicional u OAuth2), manteniendo un esquema de sesión único para toda la plataforma.
- **RF06** – Todo usuario debe contar con un panel de configuración personal donde pueda editar nombre, teléfono, cambiar contraseña (si aplica) y gestionar sus métodos de autenticación vinculados.
- **RF07** – El Prestador debe contar, adicionalmente, con una sección separada de configuración del negocio (nombre, descripción, logo, slug), distinta de su panel personal.

### Módulo 2 — Gestión de empleados

- **RF08** – El Prestador debe poder invitar empleados ingresando su dirección de correo electrónico, generando una invitación pendiente (sin crear la cuenta directamente).
- **RF09** – El Empleado invitado debe poder registrarse de forma independiente con cualquiera de los métodos de autenticación soportados, usando el correo invitado.
- **RF10** – Al completar su registro (o si ya tenía cuenta con ese correo), el sistema debe vincular automáticamente al Empleado con el negocio que lo invitó, asignándole el rol correspondiente dentro de ese tenant.
- **RF11** – El Prestador debe poder visualizar el estado de cada invitación (pendiente/aceptada) y reenviar o cancelar invitaciones pendientes.
- **RF12** – El sistema debe soportar el flag `es_dueño`, permitiendo que el Prestador opere también como Empleado con un único JWT combinando permisos.
- **RF13** – El Prestador debe poder asignar servicios específicos a cada Empleado.
- **RF32** – Por defecto, todo Empleado debe poder gestionar libremente su propio horario semanal base y sus bloqueos puntuales de disponibilidad. El Prestador debe poder, opcionalmente, restringir esta autogestión para un Empleado específico, tomando control centralizado de su horario cuando el modelo de negocio lo requiera.
- **RF33** – Si un Empleado intenta crear un bloqueo que coincide con una cita ya confirmada, el sistema debe impedir la acción o solicitar la reprogramación/cancelación previa de dicha cita.

### Módulo 3 — Gestión del negocio

- **RF14** – El Prestador debe poder crear, editar y eliminar servicios (nombre, duración, precio, descripción).
- **RF15** – El Prestador debe poder definir horarios semanales de disponibilidad por empleado.
- **RF16** – El Prestador debe poder definir bloqueos puntuales (vacaciones, permisos, días no laborables).
- **RF17** – El Prestador debe poder visualizar y gestionar todas las citas del negocio.

### Módulo 4 — Motor de disponibilidad

- **RF18** – El sistema debe calcular los espacios disponibles para agendar una cita según servicio, fecha y empleado (opcional), considerando horarios semanales, bloqueos puntuales y citas existentes.
- **RF19** – El motor de disponibilidad debe funcionar como componente puro de dominio, sin dependencias de infraestructura.

### Módulo 5 — Reservas y agenda

- **RF20** – El Cliente debe poder ver la página pública del negocio (mediante un slug único) con sus servicios y disponibilidad.
- **RF21** – El Cliente debe poder reservar una cita seleccionando servicio, empleado (opcional) y horario disponible.
- **RF22** – Si el Cliente tiene sesión iniciada, el sistema debe autocompletar automáticamente sus datos de contacto (nombre, teléfono, email) en el formulario de reserva, requiriendo solo la selección de servicio/empleado/horario. El Cliente debe poder editar estos datos si la reserva es para un tercero.
- **RF23** – El sistema debe enviar confirmación de la cita con opción de contacto directo vía mensaje prellenado a un canal externo de mensajería.
- **RF24** – El Empleado debe poder ver su propia agenda de citas.
- **RF25** – El sistema debe ofrecer una vista de calendario unificada con filtro por columna de empleado (por defecto, todos).
- **RF26** – El Prestador/Empleado debe poder cancelar o reprogramar una cita.
- **RF27** – El Cliente debe poder cancelar su propia cita (según política definida).

### Módulo 6 — Reseñas

- **RF28** – El sistema debe permitir que Clientes registrados con citas completadas dejen una reseña (calificación y comentario).
- **RF29** – El sistema debe mostrar las reseñas en la página pública del negocio.

### Módulo 7 — Planes y límites (freemium)

- **RF30** – El sistema debe restringir el número de servicios y/o citas según el plan del Prestador (gratuito vs. pago).
- **RF31** – El sistema debe permitir distinguir y aplicar las funcionalidades habilitadas según el plan activo.

---

## Requisitos No Funcionales (RNF)

### Arquitectura y rendimiento

- **RNF01 – API-first:** el backend debe exponer una API REST estructurada para permitir el consumo futuro desde una app móvil sin cambios en el backend.
- **RNF02 – Multitenancy:** el sistema debe aislar correctamente los datos entre distintos negocios (tenants) identificados por slug.
- **RNF03 – Rendimiento del motor de disponibilidad:** el cálculo de horarios disponibles debe responder en tiempo aceptable (idealmente <500ms) incluso con múltiples empleados y reglas de horario.
- **RNF04 – Mantenibilidad:** el motor de disponibilidad debe mantenerse en la capa de dominio, desacoplado de infraestructura, siguiendo una arquitectura en capas.
- **RNF05 – Escalabilidad lógica:** el diseño debe permitir agregar nuevos roles, planes o integraciones (pasarela de pago, notificaciones) sin romper la arquitectura existente.

### Seguridad y autenticación

- **RNF06 – Seguridad:** toda comunicación autenticada debe usar JWT; las contraseñas (cuando aplique) deben almacenarse con hash seguro; los tokens OAuth2 deben manejarse con buenas prácticas (sin client secrets en frontend, validación de tokens en backend, HTTPS en todos los flujos de redirect).
- **RNF07 – Interoperabilidad de autenticación:** el sistema debe seguir el flujo estándar OAuth2 (Authorization Code Flow, con PKCE si el frontend es SPA) y ser extensible a otros proveedores sin rediseñar el módulo de autenticación.
- **RNF08 – Consistencia de identidad multi-rol:** un mismo email debe poder estar asociado a distintos roles en distintos tenants (ej. Cliente en un negocio, Empleado en otro) sin conflicto, dado que el rol es contextual al tenant.
- **RNF09 – Integridad de invitaciones:** las invitaciones a empleados deben poder expirar o cancelarse, y no deben vincular un correo a un negocio sin consentimiento explícito del invitado.

### Infraestructura y operación

- **RNF10 – Compatibilidad de hosting:** el backend debe ejecutarse dentro de los límites de un plan de hosting gratuito o de bajo costo; el frontend debe desplegarse con ruteo basado en slugs (no subdominios).
- **RNF11 – Disponibilidad:** el sistema debe estar operativo y accesible públicamente durante la sustentación académica y uso comercial posterior, dentro de las limitaciones de infraestructura disponibles.
- **RNF12 – Portabilidad de datos:** el modelo de datos debe soportar exportación/migración futura si el proyecto escala más allá de la infraestructura inicial.

### Usabilidad y documentación

- **RNF13 – Usabilidad:** la página pública de reservas debe ser utilizable sin necesidad de registro (modo invitado).
- **RNF14 – Documentación técnica:** el motor de disponibilidad y la arquitectura general deben estar documentados con suficiente detalle para la defensa académica.
