-- Demo data, reloaded on every start (the schema is create-drop).
-- Dates are relative to "now" so the list always looks current.

-- Ids 1 and 3 are referenced from Java as Estado.OPEN_ID and Estado.CLOSED_ID; keep them in step.
INSERT INTO estado (id, nombre) VALUES (1, 'ABIERTO'), (2, 'EN CURSO'), (3, 'CERRADO');

INSERT INTO incidencia (id, labor, titol, descripcion, data_obertura, data_tancament, temps_resolucio, prioritat, estado_id) VALUES
 (1,  'Soporte',    'El portal de reservas no carga',            'Los usuarios ven un error 502 al abrir /reservas desde media manana.',           DATEADD('HOUR', -30, CURRENT_TIMESTAMP), NULL, NULL, 'ALTA',  2),
 (2,  'Infra',      'Copia de seguridad nocturna fallida',       'El job de backup termino con codigo 1 dos noches seguidas.',                     DATEADD('HOUR', -52, CURRENT_TIMESTAMP), NULL, NULL, 'ALTA',  1),
 (3,  'Soporte',    'Impresora de recepcion sin conexion',       'La impresora de la planta baja no aparece en la red.',                           DATEADD('HOUR', -70, CURRENT_TIMESTAMP), DATEADD('HOUR', -68, CURRENT_TIMESTAMP), '2h 0m',  'BAIXA', 3),
 (4,  'Desarrollo', 'Error de calculo en la factura mensual',    'El total no aplica el descuento cuando hay mas de dos lineas.',                  DATEADD('HOUR', -26, CURRENT_TIMESTAMP), NULL, NULL, 'MITJA', 2),
 (5,  'Soporte',    'Un usuario no puede cambiar su contrasena', 'El enlace de recuperacion caduca antes de que llegue el correo.',                DATEADD('HOUR', -96, CURRENT_TIMESTAMP), DATEADD('HOUR', -73, CURRENT_TIMESTAMP), '23h 0m', 'MITJA', 3),
 (6,  'Infra',      'Disco al 92% en el servidor de aplicaciones','Los logs sin rotar estan llenando la particion.',                              DATEADD('HOUR', -20, CURRENT_TIMESTAMP), NULL, NULL, 'ALTA',  2),
 (7,  'Desarrollo', 'La exportacion a CSV pierde los acentos',   'El fichero sale en ISO-8859-1 en vez de UTF-8.',                                 DATEADD('HOUR', -44, CURRENT_TIMESTAMP), NULL, NULL, 'BAIXA', 1),
 (8,  'Soporte',    'Correo de confirmacion marcado como spam',  'Falta configurar SPF y DKIM en el dominio de envio.',                            DATEADD('HOUR', -140, CURRENT_TIMESTAMP), DATEADD('HOUR', -86, CURRENT_TIMESTAMP), '54h 0m', 'MITJA', 3),
 (9,  'Infra',      'Certificado TLS caduca en 5 dias',          'Renovar el certificado del dominio principal antes del corte.',                  DATEADD('HOUR', -12, CURRENT_TIMESTAMP), NULL, NULL, 'ALTA',  1),
 (10, 'Desarrollo', 'Boton Guardar deshabilitado en Firefox',    'El formulario de alta no valida bien el campo telefono en Firefox.',             DATEADD('HOUR', -8,  CURRENT_TIMESTAMP), NULL, NULL, 'BAIXA', 2),
 (11, 'Soporte',    'Portatil nuevo sin acceso a la VPN',        'El equipo de un empleado nuevo no tiene el perfil de VPN instalado.',            DATEADD('HOUR', -34, CURRENT_TIMESTAMP), DATEADD('HOUR', -33, CURRENT_TIMESTAMP), '1h 0m',  'MITJA', 3),
 (12, 'Infra',      'Latencia alta contra la base de datos',     'Las consultas del panel tardan mas de 4 segundos en hora punta.',                DATEADD('HOUR', -5,  CURRENT_TIMESTAMP), NULL, NULL, 'ALTA',  2);

-- Move the identity counters past the seeded ids so new rows do not collide.
ALTER TABLE estado ALTER COLUMN id RESTART WITH 10;
ALTER TABLE incidencia ALTER COLUMN id RESTART WITH 100;
