USE oceanlink;

INSERT INTO pais (codigo_pais, nombre) VALUES
  ('PE', 'Perú'),
  ('CL', 'Chile'),
  ('CO', 'Colombia'),
  ('PA', 'Panamá');
INSERT INTO rol (codigo_rol, nombre) VALUES
  ('ADMIN', 'Administrador'),
  ('CAPACITY', 'Planificador de capacidad'),
  ('NETWORK', 'Operador de red'),
  ('MAINTENANCE', 'Coordinador de mantenimiento'),
  ('SUPERVISOR', 'Supervisor');
INSERT INTO usuario (codigo_usuario, nombre_completo, correo, hash_contrasena, telefono, id_rol, id_pais, estado, fecha_ingreso, fecha_primera_activacion, ultimo_acceso) VALUES
  ('USR-001', 'Rosales, Alonso', 'arosales@oceanlink.pe', 'NO_VALIDO_DEMO', '+51 987 420 118', 1, 1, 1, '2024-03-12 00:00:00', '2024-03-12 00:00:00', '2026-09-13 13:42:00'),
  ('USR-002', 'Ávalos, Kevin', 'kavalos@oceanlink.pe', 'NO_VALIDO_DEMO', '+51 945 771 203', 4, 1, 1, '2025-01-08 00:00:00', '2025-01-08 00:00:00', '2026-09-13 12:18:00'),
  ('USR-003', 'Aguilar, Matías', 'maguilar@oceanlink.pe', 'NO_VALIDO_DEMO', '+51 932 608 451', 2, 1, 1, '2023-07-17 00:00:00', '2023-07-17 00:00:00', '2026-08-29 17:06:00'),
  ('USR-004', 'Leon, Axel', 'aleon@oceanlink.pe', 'NO_VALIDO_DEMO', '+51 944 212 876', 5, 1, 1, '2026-01-01 00:00:00', '2026-01-01 00:00:00', '2026-09-18 16:05:00'),
  ('USR-005', 'Silvera, Ronal', 'rsilvera@oceanlink.pe', 'NO_VALIDO_DEMO', '+51 966 308 541', 3, 1, 1, '2026-01-01 00:00:00', '2026-01-01 00:00:00', '2026-09-18 15:31:00');
INSERT INTO estacion (codigo_estacion, nombre, ubicacion, id_pais, capacidad_energia_kw, estado, condicion_operativa, fecha_registro) VALUES
  ('PE-0001', 'Lurín', 'Lima, Perú', 1, 500.0, 1, 'OPERATIVA', '2020-01-15 00:00:00'),
  ('CL-0002', 'Valparaíso', 'Valparaíso, Chile', 2, 520.0, 1, 'OPERATIVA', '2020-03-22 00:00:00'),
  ('CO-0003', 'Buenaventura', 'Valle del Cauca, Colombia', 3, 480.0, 1, 'OPERATIVA', '2021-06-18 00:00:00'),
  ('PE-0004', 'Máncora', 'Piura, Perú', 1, 450.0, 1, 'EN_MANTENIMIENTO', '2021-11-09 00:00:00'),
  ('CL-0005', 'Punta Arenas', 'Magallanes, Chile', 2, 510.0, 1, 'OPERATIVA', '2022-04-14 00:00:00'),
  ('PA-0006', 'Panamá', 'Ciudad de Panamá, Panamá', 4, 540.0, 1, 'OPERATIVA', '2022-08-30 00:00:00');
INSERT INTO segmento (codigo_segmento, id_estacion_origen, id_estacion_destino, capacidad_total_tbps, estado, condicion_operativa) VALUES
  ('SEG-001', 1, 2, 20, 1, 'OPERATIVO'),
  ('SEG-002', 1, 3, 40, 1, 'OPERATIVO'),
  ('SEG-003', 2, 5, 15, 1, 'DEGRADADO'),
  ('SEG-004', 3, 6, 8, 1, 'DEGRADADO'),
  ('SEG-005', 4, 1, 6, 1, 'EN_MANTENIMIENTO'),
  ('SEG-006', 5, 6, 4, 1, 'FUERA_SERVICIO');
INSERT INTO cliente (codigo_cliente, razon_social, tipo_identificacion, identificacion_fiscal, nombre_contacto, correo_contacto, telefono_contacto, estado) VALUES
  ('CLI-001', 'Telco Global', 'RUC', '20547896321', 'Ana Vega', 'operaciones@telcoglobal.com', '+51 988 112 235', 1),
  ('CLI-002', 'Red Andina', 'RUC', '20678123459', 'Diego Paredes', 'red@andina.pe', '+51 988 112 236', 1),
  ('CLI-003', 'Pacífico Net', 'RUC', '20456987123', 'Silvera, Ronal', 'soporte@pacificonet.com', '+51 988 112 237', 1),
  ('CLI-004', 'DataSur', 'RUC', '20600451238', 'Martín Flores', 'operaciones@datasur.pe', '+51 988 112 238', 1);
INSERT INTO solicitud_capacidad (codigo_solicitud, id_cliente, id_estacion_origen, id_estacion_destino, id_estado_solicitud, capacidad_solicitada_tbps, fecha_solicitud, creado_por, fecha_provision) VALUES
  ('REQ-501', 1, 1, 2, 1, 0.5, '2026-09-12 09:00:00', 3, NULL),
  ('REQ-502', 1, 1, 6, 2, 0.5, '2026-08-28 09:00:00', 3, NULL),
  ('REQ-503', 2, 4, 2, 3, 0.3, '2026-09-05 09:00:00', 3, NULL),
  ('REQ-504', 3, 1, 3, 4, 1.0, '2026-09-07 09:00:00', 3, '2026-09-09 09:00:00'),
  ('REQ-505', 1, 1, 2, 5, 2.0, '2026-01-02 09:00:00', 3, '2026-01-14 00:00:00'),
  ('REQ-506', 2, 4, 6, 7, 4.0, '2026-09-11 09:00:00', 3, NULL),
  ('REQ-507', 3, 2, 6, 6, 1.0, '2026-09-10 09:00:00', 3, NULL),
  ('REQ-508', 2, 4, 6, 3, 0.3, '2026-09-12 09:00:00', 3, NULL),
  ('REQ-902', 1, 1, 3, 5, 0.5, '2026-05-27 09:00:00', 3, '2026-06-02 00:00:00'),
  ('REQ-915', 1, 1, 3, 5, 1.0, '2026-02-23 09:00:00', 3, '2026-03-01 00:00:00'),
  ('REQ-930', 1, 3, 6, 5, 1.0, '2026-07-02 09:00:00', 3, '2026-07-08 00:00:00'),
  ('REQ-910', 2, 4, 1, 5, 1.0, '2026-02-10 09:00:00', 3, '2026-02-16 00:00:00'),
  ('REQ-810', 3, 1, 2, 5, 0.2, '2026-01-21 09:00:00', 3, '2026-01-27 00:00:00'),
  ('REQ-941', 4, 1, 2, 5, 13.8, '2026-01-08 09:00:00', 3, '2026-01-14 00:00:00'),
  ('REQ-942', 4, 1, 3, 5, 8.5, '2026-01-08 09:00:00', 3, '2026-01-14 00:00:00'),
  ('REQ-943', 3, 2, 5, 5, 14.5, '2026-01-08 09:00:00', 3, '2026-01-14 00:00:00'),
  ('REQ-944', 2, 3, 6, 5, 5.7, '2026-01-08 09:00:00', 3, '2026-01-14 00:00:00'),
  ('REQ-945', 4, 4, 1, 5, 3.1, '2026-01-08 09:00:00', 3, '2026-01-14 00:00:00'),
  ('REQ-946', 1, 1, 2, 5, 0.4, '2026-02-01 09:00:00', 3, '2026-02-07 00:00:00');
INSERT INTO solicitud_segmento (id_solicitud, id_segmento, orden, fecha_reserva) VALUES
  (1, 1, 1, NULL),
  (2, 2, 1, NULL),
  (2, 4, 2, NULL),
  (3, 5, 1, NULL),
  (3, 1, 2, NULL),
  (4, 2, 1, '2026-09-09 09:00:00'),
  (5, 1, 1, '2026-01-14 00:00:00'),
  (6, 5, 1, NULL),
  (6, 2, 2, NULL),
  (6, 4, 3, NULL),
  (7, 3, 1, NULL),
  (7, 6, 2, NULL),
  (8, 5, 1, NULL),
  (8, 2, 2, NULL),
  (8, 4, 3, NULL),
  (9, 2, 1, '2026-06-02 00:00:00'),
  (10, 2, 1, '2026-03-01 00:00:00'),
  (11, 4, 1, '2026-07-08 00:00:00'),
  (12, 5, 1, '2026-02-16 00:00:00'),
  (13, 1, 1, '2026-01-27 00:00:00'),
  (14, 1, 1, '2026-01-14 00:00:00'),
  (15, 2, 1, '2026-01-14 00:00:00'),
  (16, 3, 1, '2026-01-14 00:00:00'),
  (17, 4, 1, '2026-01-14 00:00:00'),
  (18, 5, 1, '2026-01-14 00:00:00'),
  (19, 1, 1, '2026-02-07 00:00:00');

INSERT INTO servicio (codigo_servicio, id_solicitud, capacidad_contratada_tbps, estado, fecha_activacion, fecha_cancelacion) VALUES
  ('SRV-901', 5, 2.0, 'ACTIVO', '2026-01-15 00:00:00', NULL),
  ('SRV-902', 9, 0.5, 'ACTIVO', '2026-06-03 00:00:00', NULL),
  ('SRV-915', 10, 1.0, 'ACTIVO', '2026-03-02 00:00:00', NULL),
  ('SRV-930', 11, 1.0, 'ACTIVO', '2026-07-09 00:00:00', NULL),
  ('SRV-877', 8, 0.3, 'PENDIENTE', NULL, NULL),
  ('SRV-910', 12, 1.0, 'ACTIVO', '2026-02-17 00:00:00', NULL),
  ('SRV-810', 13, 0.2, 'ACTIVO', '2026-01-28 00:00:00', NULL),
  ('SRV-941', 14, 13.8, 'ACTIVO', '2026-01-15 00:00:00', NULL),
  ('SRV-942', 15, 8.5, 'ACTIVO', '2026-01-15 00:00:00', NULL),
  ('SRV-943', 16, 14.5, 'ACTIVO', '2026-01-15 00:00:00', NULL),
  ('SRV-944', 17, 5.7, 'ACTIVO', '2026-01-15 00:00:00', NULL),
  ('SRV-945', 18, 3.1, 'ACTIVO', '2026-01-15 00:00:00', NULL),
  ('SRV-946', 19, 0.4, 'CANCELADO', '2026-02-08 00:00:00', '2026-08-15 00:00:00');
INSERT INTO servicio_segmento (id_servicio, id_segmento, orden) VALUES
  (1, 1, 1),
  (2, 2, 1),
  (3, 2, 1),
  (4, 4, 1),
  (5, 5, 1),
  (5, 2, 2),
  (5, 4, 3),
  (6, 5, 1),
  (7, 1, 1),
  (8, 1, 1),
  (9, 2, 1),
  (10, 3, 1),
  (11, 4, 1),
  (12, 5, 1),
  (13, 1, 1);
INSERT INTO incidencia (codigo_incidencia, severidad, fase, fecha_deteccion, descripcion, fecha_restauracion, fecha_cierre, resultado, creado_por) VALUES
  ('INC-001', 'ALTA', 'EN_ANALISIS', '2026-09-11 08:20:00', 'Pérdida de señal detectada sobre el tramo Buenaventura - Panamá.', NULL, NULL, NULL, 5),
  ('INC-002', 'MEDIA', 'REPARACION_PROGRAMADA', '2026-09-10 14:10:00', 'Intermitencia asociada a trabajos sobre la estación de Máncora.', NULL, NULL, NULL, 5),
  ('INC-003', 'BAJA', 'RESTAURADA', '2026-09-08 06:30:00', 'Degradación temporal ya restaurada en el tramo Lurín - Valparaíso.', '2026-09-08 08:10:00', NULL, NULL, 5),
  ('INC-005', 'CRITICA', 'EN_REPARACION', '2026-09-10 03:15:00', 'Pérdida severa de potencia óptica en el tramo Valparaíso - Punta Arenas.', NULL, NULL, NULL, 5),
  ('INC-004', 'MEDIA', 'CERRADA', '2026-09-02 10:05:00', 'Atenuación temporal del enlace, restaurada y cerrada.', '2026-09-02 16:50:00', '2026-09-02 18:40:00', 'Servicio restaurado y monitoreo normalizado.', 5),
  ('INC-009', 'ALTA', 'CERRADA', '2026-08-23 22:40:00', 'Falla de empalme con impacto sobre una ruta de servicio.', '2026-08-24 05:20:00', '2026-08-24 06:15:00', 'Empalme corregido y servicios verificados.', 5);
INSERT INTO incidencia_segmento (id_incidencia, id_segmento) VALUES
  (1, 4),
  (2, 5),
  (3, 1),
  (4, 3),
  (5, 1),
  (6, 4),
  (6, 2);
INSERT INTO observacion_incidencia (id_incidencia, fase, fecha, observacion, registrado_por) VALUES
  (1, 'DETECTADA', '2026-09-11 08:20:00', 'Se registra degradación de señal sobre SEG-004.', 5),
  (1, 'EN_ANALISIS', '2026-09-11 08:42:00', 'Se identifican los servicios que utilizan el segmento afectado.', 5),
  (2, 'DETECTADA', '2026-09-10 14:10:00', 'Se detecta intermitencia sobre SEG-005.', 5),
  (2, 'EN_ANALISIS', '2026-09-10 14:35:00', 'Se confirma relación con la estación EST-004.', 5),
  (2, 'REPARACION_PROGRAMADA', '2026-09-10 16:00:00', 'Se programa intervención técnica.', 5),
  (3, 'DETECTADA', '2026-09-08 06:30:00', 'Se registra degradación temporal.', 5),
  (3, 'EN_ANALISIS', '2026-09-08 06:42:00', 'Se valida afectación parcial.', 5),
  (3, 'REPARACION_PROGRAMADA', '2026-09-08 07:00:00', 'Se coordina intervención.', 5),
  (3, 'EN_REPARACION', '2026-09-08 07:20:00', 'Equipo técnico interviene el tramo.', 5),
  (3, 'RESTAURADA', '2026-09-08 08:10:00', 'Servicio normalizado y validado.', 5),
  (4, 'DETECTADA', '2026-09-10 03:15:00', 'Alarma crítica reportada sobre SEG-003.', 5),
  (4, 'EN_ANALISIS', '2026-09-10 03:28:00', 'Se confirma degradación severa de potencia.', 5),
  (4, 'REPARACION_PROGRAMADA', '2026-09-10 04:10:00', 'Se coordina intervención prioritaria.', 5),
  (4, 'EN_REPARACION', '2026-09-10 05:00:00', 'Cuadrilla trabajando sobre el tramo.', 5),
  (5, 'DETECTADA', '2026-09-02 10:05:00', 'Se detecta atenuación.', 5),
  (5, 'EN_ANALISIS', '2026-09-02 10:30:00', 'Se valida el tramo afectado.', 5),
  (5, 'REPARACION_PROGRAMADA', '2026-09-02 12:00:00', 'Intervención programada.', 5),
  (5, 'EN_REPARACION', '2026-09-02 14:20:00', 'Se corrige conector óptico.', 5),
  (5, 'RESTAURADA', '2026-09-02 16:50:00', 'Se restablece el servicio.', 5),
  (5, 'CERRADA', '2026-09-02 18:40:00', 'Servicio restaurado y monitoreo normalizado.', 5),
  (6, 'DETECTADA', '2026-08-23 22:40:00', 'Se registra pérdida de potencia.', 5),
  (6, 'EN_ANALISIS', '2026-08-23 23:00:00', 'Se localiza afectación en dos segmentos.', 5),
  (6, 'REPARACION_PROGRAMADA', '2026-08-24 00:15:00', 'Cuadrilla movilizada.', 5),
  (6, 'EN_REPARACION', '2026-08-24 02:10:00', 'Corrección del empalme.', 5),
  (6, 'RESTAURADA', '2026-08-24 05:20:00', 'Servicios verificados.', 5),
  (6, 'CERRADA', '2026-08-24 06:15:00', 'Empalme corregido y servicios verificados.', 5);
INSERT INTO mantenimiento (codigo_mantenimiento, id_tipo_mantenimiento, id_estacion, id_segmento, fase, inicio_programado, fin_programado, inicio_real, fecha_cierre, descripcion, resultado, creado_por) VALUES
  ('MNT-045', 1, 4, NULL, 'PROGRAMADO', '2026-09-21 22:00:00', '2026-09-22 02:00:00', NULL, NULL, 'Revisión preventiva de energía y climatización de la estación.', NULL, 2),
  ('MNT-046', 2, NULL, 4, 'EN_PROGRESO', '2026-09-16 09:00:00', '2026-09-16 15:00:00', '2026-09-16 09:00:00', NULL, 'Corrección de degradación detectada sobre el segmento.', NULL, 2),
  ('MNT-040', 1, NULL, 1, 'FINALIZADO', '2026-09-05 08:00:00', '2026-09-05 11:00:00', '2026-09-05 08:00:00', '2026-09-05 11:00:00', 'Inspección preventiva del tramo.', 'Sin observaciones', 2),
  ('MNT-038', 1, 2, NULL, 'FINALIZADO', '2026-08-30 07:00:00', '2026-08-30 09:00:00', '2026-08-30 07:00:00', '2026-08-30 09:00:00', 'Verificación de conectores y energía.', 'Conectores verificados', 2),
  ('MNT-031', 2, NULL, 4, 'FINALIZADO', '2026-08-22 20:00:00', '2026-08-23 01:00:00', '2026-08-22 20:00:00', '2026-08-23 01:00:00', 'Corrección de degradación de señal.', 'Señal restablecida', 2);
INSERT INTO actividad_mantenimiento (id_mantenimiento, tipo_actividad, fecha, descripcion, registrado_por) VALUES
  (2, 'Inicio de mantenimiento', '2026-09-16 09:00:00', 'Se inicia la ventana correctiva y se confirma el impacto.', 2),
  (2, 'Actividad registrada', '2026-09-16 10:35:00', 'Se inspecciona el tramo y se valida potencia óptica.', 2),
  (3, 'Inicio de mantenimiento', '2026-09-05 08:00:00', 'Inicio de ventana preventiva.', 2),
  (3, 'Finalizado', '2026-09-05 11:00:00', 'Sin observaciones.', 2),
  (4, 'Inicio de mantenimiento', '2026-08-30 07:00:00', 'Inicio de revisión.', 2),
  (4, 'Finalizado', '2026-08-30 09:00:00', 'Conectores verificados.', 2),
  (5, 'Inicio de mantenimiento', '2026-08-22 20:00:00', 'Inicio correctivo.', 2),
  (5, 'Finalizado', '2026-08-23 01:00:00', 'Señal restablecida.', 2);
INSERT INTO historial_capacidad_segmento (id_segmento, fecha, capacidad_total_tbps, capacidad_utilizada_tbps, capacidad_reservada_tbps) VALUES
  (1, '2026-09-01 12:00:00', 20, 16.0, 0),
  (2, '2026-09-01 12:00:00', 40, 10.0, 0),
  (3, '2026-09-01 12:00:00', 15, 14.5, 0),
  (4, '2026-09-01 12:00:00', 8, 6.7, 0),
  (5, '2026-09-01 12:00:00', 6, 4.1, 0),
  (6, '2026-09-01 12:00:00', 4, 0, 0),
  (1, '2026-09-20 12:00:00', 20, 16.0, 0),
  (2, '2026-09-20 12:00:00', 40, 10.0, 1.0),
  (3, '2026-09-20 12:00:00', 15, 14.5, 0),
  (4, '2026-09-20 12:00:00', 8, 6.7, 0),
  (5, '2026-09-20 12:00:00', 6, 4.1, 0),
  (6, '2026-09-20 12:00:00', 4, 0, 0);
INSERT INTO reporte_generado (codigo_reporte, tipo, id_segmento, fecha_inicio, fecha_fin, generado_en, generado_por) VALUES
  ('RPT-001', 'CAPACIDAD_UTILIZACION', NULL, '2026-08-20 00:00:00', '2026-09-20 23:59:59', '2026-09-21 09:00:00', 4);


INSERT INTO pais (codigo_pais,nombre) VALUES ('PE','Perú'),('CL','Chile'),('CO','Colombia'),('PA','Panamá') ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);
