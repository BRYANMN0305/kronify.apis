# Requisitos del Sistema - Plataforma de Reservas y Gestion de Clientes

## Requisitos Funcionales (RF)

### Modulo 1 - Autenticacion y gestion de cuentas

- **RF01** - El sistema debe permitir que los usuarios se registren e inicien sesion mediante email/contrasena, OAuth2 con Google, u OAuth2 con Microsoft.
- **RF02** - Al crear una cuenta, el sistema debe presentar un perfil global unico: **"Soy Cliente"** o **"Soy Negocio"**. Una cuenta no puede cambiar ni combinar ambos perfiles dentro del MVP. El Empleado se maneja como un usuario de tipo Negocio vinculado a un negocio por invitacion.
- **RF03** - El sistema debe permitir que cualquier usuario se registre e inicie sesion via OAuth2, vinculando la identidad del proveedor a una cuenta interna del sistema.
- **RF04** - Si un usuario se registra por OAuth2 y luego intenta iniciar sesion con el mismo correo mediante metodo tradicional, o viceversa, el sistema debe manejar la vinculacion/conflicto de cuentas de forma consistente.
- **RF05** - El sistema debe emitir un JWT propio tras una autenticacion exitosa, manteniendo un esquema de sesion unico para toda la plataforma.
- **RF06** - Todo usuario debe contar con un panel de configuracion personal donde pueda editar nombre, telefono, cambiar contrasena si aplica y gestionar sus metodos de autenticacion vinculados.
- **RF07** - El Prestador debe contar, adicionalmente, con una seccion separada de configuracion del negocio.

### Modulo 2 - Gestion de empleados

- **RF08** - El Prestador debe poder invitar empleados ingresando su correo electronico, generando una invitacion pendiente.
- **RF09** - El Empleado invitado debe poder registrarse con cualquiera de los metodos de autenticacion soportados, usando el correo invitado. Si el correo ya pertenece a una cuenta Cliente, la invitacion no debe aceptarse con esa cuenta.
- **RF10** - Al completar su registro como usuario de tipo Negocio, o si ya tenia una cuenta Negocio con ese correo, el sistema debe vincular al Empleado con el negocio mediante un registro de Empleado.
- **RF11** - El Prestador debe poder visualizar el estado de cada invitacion y reenviar o cancelar invitaciones pendientes.
- **RF12** - El sistema debe soportar el flag `owner`, permitiendo que el Prestador opere tambien como Empleado del mismo negocio con su misma cuenta de tipo Negocio.
- **RF13** - El Prestador debe poder asignar servicios especificos a cada Empleado.
- **RF32** - Por defecto, todo Empleado debe poder gestionar su propio horario semanal base y sus bloqueos puntuales. El Prestador debe poder restringir esta autogestion para un Empleado especifico.
- **RF33** - Si un Empleado intenta crear un bloqueo que coincide con una cita confirmada, el sistema debe impedir la accion o solicitar reprogramacion/cancelacion previa.

### Modulo 3 - Gestion del negocio

- **RF14** - El Prestador debe poder crear, editar y eliminar servicios.
- **RF15** - El Prestador debe poder definir horarios semanales de disponibilidad por empleado.
- **RF16** - El Prestador debe poder definir bloqueos puntuales.
- **RF17** - El Prestador debe poder visualizar y gestionar todas las citas del negocio.

### Modulo 4 - Motor de disponibilidad

- **RF18** - El sistema debe calcular espacios disponibles segun servicio, fecha y empleado opcional, considerando horarios semanales, bloqueos y citas existentes.
- **RF19** - El motor de disponibilidad debe funcionar como componente puro de dominio, sin dependencias de infraestructura.

### Modulo 5 - Reservas y agenda

- **RF20** - El Cliente debe poder ver la pagina publica del negocio mediante slug unico.
- **RF21** - El Cliente debe poder reservar una cita seleccionando servicio, empleado opcional y horario disponible.
- **RF22** - Si el Cliente tiene sesion iniciada, el sistema debe autocompletar sus datos de contacto en el formulario de reserva.
- **RF23** - El sistema debe enviar confirmacion de la cita con opcion de contacto directo por canal externo de mensajeria.
- **RF24** - El Empleado debe poder ver su propia agenda de citas.
- **RF25** - El sistema debe ofrecer una vista de calendario unificada con filtro por empleado.
- **RF26** - El Prestador/Empleado debe poder cancelar o reprogramar una cita.
- **RF27** - El Cliente debe poder cancelar su propia cita segun politica definida.

### Modulo 6 - Resenas

- **RF28** - El sistema debe permitir que Clientes registrados con citas completadas dejen una resena.
- **RF29** - El sistema debe mostrar las resenas en la pagina publica del negocio.

### Modulo 7 - Planes y limites

- **RF30** - El sistema debe restringir el numero de servicios y/o citas segun el plan del Prestador.
- **RF31** - El sistema debe distinguir y aplicar funcionalidades habilitadas segun el plan activo.

---

## Requisitos No Funcionales (RNF)

### Arquitectura y rendimiento

- **RNF01 - API-first:** el backend debe exponer una API REST estructurada.
- **RNF02 - Aislamiento por negocio:** el sistema debe aislar correctamente los datos de distintos negocios identificados por slug.
- **RNF03 - Rendimiento del motor de disponibilidad:** el calculo de horarios disponibles debe responder en tiempo aceptable.
- **RNF04 - Mantenibilidad:** el motor de disponibilidad debe mantenerse desacoplado de infraestructura.
- **RNF05 - Escalabilidad logica:** el diseno debe permitir agregar planes o integraciones sin romper la arquitectura. La gestion de roles se mantiene simple para el MVP.

### Seguridad y autenticacion

- **RNF06 - Seguridad:** toda comunicacion autenticada debe usar JWT; las contrasenas deben almacenarse con hash seguro.
- **RNF07 - Interoperabilidad de autenticacion:** el sistema debe seguir flujos OAuth2 estandar y permitir nuevos proveedores.
- **RNF08 - Consistencia de perfil unico:** un mismo email debe estar asociado a un unico perfil global (`Cliente` o `Negocio`). El sistema debe impedir que una cuenta Cliente sea usada como Empleado/Prestador, y debe impedir que una cuenta Negocio actue como Cliente registrado.
- **RNF09 - Integridad de invitaciones:** las invitaciones deben poder expirar o cancelarse, y no deben vincular un correo a un negocio sin consentimiento.

### Infraestructura y operacion

- **RNF10 - Compatibilidad de hosting:** el backend debe ejecutarse dentro de infraestructura gratuita o de bajo costo.
- **RNF11 - Disponibilidad:** el sistema debe estar operativo durante la sustentacion y uso posterior.
- **RNF12 - Portabilidad de datos:** el modelo debe soportar exportacion/migracion futura.

### Usabilidad y documentacion

- **RNF13 - Usabilidad:** la pagina publica de reservas debe ser utilizable sin registro.
- **RNF14 - Documentacion tecnica:** el motor de disponibilidad y la arquitectura deben estar documentados para la defensa academica.
