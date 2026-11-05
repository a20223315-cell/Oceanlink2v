CREATE DATABASE IF NOT EXISTS oceanlink CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE oceanlink;

CREATE TABLE pais (
  id_pais BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_pais CHAR(2) NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE rol (
  id_rol BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_rol VARCHAR(30) NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id_usuario BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_usuario VARCHAR(16) NOT NULL UNIQUE,
  nombre_completo VARCHAR(160) NOT NULL,
  correo VARCHAR(254) NOT NULL UNIQUE,
  hash_contrasena VARCHAR(255) NOT NULL,
  telefono VARCHAR(30) NOT NULL CHECK (CHAR_LENGTH(TRIM(telefono)) > 0),
  telefono_region CHAR(2),
  id_rol BIGINT UNSIGNED NOT NULL,
  id_pais BIGINT UNSIGNED NOT NULL,
  estado TINYINT NOT NULL DEFAULT 1,
  fecha_ingreso DATETIME NOT NULL,
  fecha_primera_activacion DATETIME,
  ultimo_acceso DATETIME,
  CHECK (estado IN (0,1)),
  CHECK (estado=0 OR fecha_primera_activacion IS NOT NULL),
  FOREIGN KEY (id_rol) REFERENCES rol(id_rol),
  FOREIGN KEY (id_pais) REFERENCES pais(id_pais)
) ENGINE=InnoDB;

CREATE TABLE estacion (
  id_estacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_estacion VARCHAR(16) NOT NULL UNIQUE,
  nombre VARCHAR(150) NOT NULL,
  ubicacion VARCHAR(160) NOT NULL,
  id_pais BIGINT UNSIGNED NOT NULL,
  capacidad_energia_kw DECIMAL(12,3) NOT NULL,
  estado TINYINT NOT NULL DEFAULT 1,
  condicion_operativa ENUM('OPERATIVA','EN_MANTENIMIENTO','FUERA_SERVICIO') NOT NULL DEFAULT 'OPERATIVA',
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK (capacidad_energia_kw > 0),
  CHECK (estado IN (0,1)),
  FOREIGN KEY (id_pais) REFERENCES pais(id_pais)
) ENGINE=InnoDB;

CREATE TABLE segmento (
  id_segmento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_segmento VARCHAR(16) NOT NULL UNIQUE,
  id_estacion_origen BIGINT UNSIGNED NOT NULL,
  id_estacion_destino BIGINT UNSIGNED NOT NULL,
  capacidad_total_tbps DECIMAL(14,4) NOT NULL,
  estado TINYINT NOT NULL DEFAULT 1,
  condicion_operativa ENUM('OPERATIVO','DEGRADADO','EN_MANTENIMIENTO','FUERA_SERVICIO') NOT NULL DEFAULT 'OPERATIVO',
  fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CHECK (id_estacion_origen <> id_estacion_destino),
  CHECK (capacidad_total_tbps > 0),
  CHECK (estado IN (0,1)),
  FOREIGN KEY (id_estacion_origen) REFERENCES estacion(id_estacion),
  FOREIGN KEY (id_estacion_destino) REFERENCES estacion(id_estacion)
) ENGINE=InnoDB;

CREATE TABLE cliente (
  id_cliente BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_cliente VARCHAR(16) NOT NULL UNIQUE,
  razon_social VARCHAR(180) NOT NULL,
  tipo_identificacion VARCHAR(20) NOT NULL,
  identificacion_fiscal VARCHAR(40) NOT NULL,
  nombre_contacto VARCHAR(160) NOT NULL,
  correo_contacto VARCHAR(254) NOT NULL,
  telefono_contacto VARCHAR(30) NOT NULL CHECK (CHAR_LENGTH(TRIM(telefono_contacto)) > 0),
  estado TINYINT NOT NULL DEFAULT 1,
  UNIQUE KEY uq_cliente_documento (tipo_identificacion,identificacion_fiscal),
  CHECK (estado IN (0,1))
) ENGINE=InnoDB;

CREATE TABLE estado_solicitud_capacidad (
  id_estado_solicitud BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(40) NOT NULL UNIQUE
) ENGINE=InnoDB;
INSERT INTO estado_solicitud_capacidad (nombre) VALUES
 ('REGISTRADA'),('EN_EVALUACION'),('APROBADA'),('PROVISIONADA'),
 ('ACTIVA'),('RECHAZADA'),('PENDIENTE_CAPACIDAD');
CREATE TABLE solicitud_capacidad (
  id_solicitud BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_solicitud VARCHAR(16) NOT NULL UNIQUE,
  id_cliente BIGINT UNSIGNED NOT NULL,
  id_estacion_origen BIGINT UNSIGNED NOT NULL,
  id_estacion_destino BIGINT UNSIGNED NOT NULL,
  id_estado_solicitud BIGINT UNSIGNED NOT NULL,
  capacidad_solicitada_tbps DECIMAL(14,4) NOT NULL,
  fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_provision DATETIME,
  observacion VARCHAR(2000),
  creado_por BIGINT UNSIGNED NOT NULL,
  CHECK (id_estacion_origen <> id_estacion_destino),
  CHECK (capacidad_solicitada_tbps > 0),
  FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  FOREIGN KEY (id_estacion_origen) REFERENCES estacion(id_estacion),
  FOREIGN KEY (id_estacion_destino) REFERENCES estacion(id_estacion),
  FOREIGN KEY (id_estado_solicitud) REFERENCES estado_solicitud_capacidad(id_estado_solicitud),
  FOREIGN KEY (creado_por) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;
CREATE TABLE solicitud_segmento (
  id_solicitud_segmento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_solicitud BIGINT UNSIGNED NOT NULL,
  id_segmento BIGINT UNSIGNED NOT NULL,
  orden SMALLINT UNSIGNED NOT NULL,
  fecha_reserva DATETIME,
  UNIQUE KEY uq_solicitud_orden (id_solicitud,orden),
  UNIQUE KEY uq_solicitud_segmento (id_solicitud,id_segmento),
  CHECK (orden > 0),
  FOREIGN KEY (id_solicitud) REFERENCES solicitud_capacidad(id_solicitud),
  FOREIGN KEY (id_segmento) REFERENCES segmento(id_segmento)
) ENGINE=InnoDB;
CREATE TABLE servicio (
  id_servicio BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_servicio VARCHAR(16) NOT NULL UNIQUE,
  id_solicitud BIGINT UNSIGNED NOT NULL UNIQUE,
  capacidad_contratada_tbps DECIMAL(14,4) NOT NULL,
  estado ENUM('PENDIENTE','ACTIVO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
  fecha_activacion DATETIME,
  fecha_cancelacion DATETIME,
  CHECK (capacidad_contratada_tbps > 0),
  CHECK (estado <> 'ACTIVO' OR fecha_activacion IS NOT NULL),
  CHECK (estado <> 'CANCELADO' OR fecha_cancelacion IS NOT NULL),
  FOREIGN KEY (id_solicitud) REFERENCES solicitud_capacidad(id_solicitud)
) ENGINE=InnoDB;

CREATE TABLE servicio_segmento (
  id_servicio_segmento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_servicio BIGINT UNSIGNED NOT NULL,
  id_segmento BIGINT UNSIGNED NOT NULL,
  orden SMALLINT UNSIGNED NOT NULL,
  UNIQUE KEY uq_servicio_orden (id_servicio,orden),
  UNIQUE KEY uq_servicio_segmento (id_servicio,id_segmento),
  CHECK (orden > 0),
  FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio),
  FOREIGN KEY (id_segmento) REFERENCES segmento(id_segmento)
) ENGINE=InnoDB;

CREATE TABLE incidencia (
  id_incidencia BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_incidencia VARCHAR(16) NOT NULL UNIQUE,
  severidad ENUM('BAJA','MEDIA','ALTA','CRITICA') NOT NULL,
  fase ENUM('DETECTADA','EN_ANALISIS','REPARACION_PROGRAMADA','EN_REPARACION','RESTAURADA','CERRADA') NOT NULL DEFAULT 'DETECTADA',
  fecha_deteccion DATETIME NOT NULL,
  descripcion VARCHAR(2000) NOT NULL,
  fecha_restauracion DATETIME,
  fecha_cierre DATETIME,
  resultado VARCHAR(2000),
  creado_por BIGINT UNSIGNED NOT NULL,
  fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CHECK (fecha_restauracion IS NULL OR fecha_restauracion >= fecha_deteccion),
  CHECK (fecha_cierre IS NULL OR (fecha_restauracion IS NOT NULL AND fecha_cierre >= fecha_restauracion)),
  CHECK (fase <> 'CERRADA' OR (fecha_cierre IS NOT NULL AND resultado IS NOT NULL)),
  FOREIGN KEY (creado_por) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE incidencia_segmento (
  id_incidencia_segmento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_incidencia BIGINT UNSIGNED NOT NULL,
  id_segmento BIGINT UNSIGNED NOT NULL,
  UNIQUE KEY uq_incidencia_segmento (id_incidencia,id_segmento),
  FOREIGN KEY (id_incidencia) REFERENCES incidencia(id_incidencia),
  FOREIGN KEY (id_segmento) REFERENCES segmento(id_segmento)
) ENGINE=InnoDB;

CREATE TABLE observacion_incidencia (
  id_observacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_incidencia BIGINT UNSIGNED NOT NULL,
  fase ENUM('DETECTADA','EN_ANALISIS','REPARACION_PROGRAMADA','EN_REPARACION','RESTAURADA','CERRADA') NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  observacion VARCHAR(2000) NOT NULL,
  registrado_por BIGINT UNSIGNED NOT NULL,
  CHECK (CHAR_LENGTH(TRIM(observacion)) > 0),
  FOREIGN KEY (id_incidencia) REFERENCES incidencia(id_incidencia),
  FOREIGN KEY (registrado_por) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE tipo_mantenimiento (
  id_tipo_mantenimiento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB;
INSERT INTO tipo_mantenimiento (nombre) VALUES ('PREVENTIVO'),('CORRECTIVO');

CREATE TABLE mantenimiento (
  id_mantenimiento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_mantenimiento VARCHAR(16) NOT NULL UNIQUE,
  id_tipo_mantenimiento BIGINT UNSIGNED NOT NULL,
  id_estacion BIGINT UNSIGNED,
  id_segmento BIGINT UNSIGNED,
  fase ENUM('PROGRAMADO','EN_PROGRESO','FINALIZADO') NOT NULL DEFAULT 'PROGRAMADO',
  inicio_programado DATETIME NOT NULL,
  fin_programado DATETIME NOT NULL,
  inicio_real DATETIME,
  fecha_cierre DATETIME,
  descripcion VARCHAR(2000) NOT NULL,
  resultado VARCHAR(2000),
  creado_por BIGINT UNSIGNED NOT NULL,
  fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CHECK ((id_estacion IS NOT NULL AND id_segmento IS NULL) OR
         (id_estacion IS NULL AND id_segmento IS NOT NULL)),
  CHECK (fin_programado > inicio_programado),
  CHECK (fase <> 'FINALIZADO' OR
         (fecha_cierre IS NOT NULL AND resultado IS NOT NULL AND CHAR_LENGTH(TRIM(resultado)) > 0)),
  FOREIGN KEY (id_tipo_mantenimiento) REFERENCES tipo_mantenimiento(id_tipo_mantenimiento),
  FOREIGN KEY (id_estacion) REFERENCES estacion(id_estacion),
  FOREIGN KEY (id_segmento) REFERENCES segmento(id_segmento),
  FOREIGN KEY (creado_por) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE actividad_mantenimiento (
  id_actividad BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_mantenimiento BIGINT UNSIGNED NOT NULL,
  tipo_actividad VARCHAR(80) NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  descripcion VARCHAR(2000) NOT NULL,
  registrado_por BIGINT UNSIGNED NOT NULL,
  FOREIGN KEY (id_mantenimiento) REFERENCES mantenimiento(id_mantenimiento),
  FOREIGN KEY (registrado_por) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE historial_capacidad_segmento (
  id_historial BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_segmento BIGINT UNSIGNED NOT NULL,
  fecha DATETIME NOT NULL,
  capacidad_total_tbps DECIMAL(14,4) NOT NULL,
  capacidad_utilizada_tbps DECIMAL(14,4) NOT NULL,
  capacidad_reservada_tbps DECIMAL(14,4) NOT NULL,
  CHECK (capacidad_total_tbps > 0),
  CHECK (capacidad_utilizada_tbps >= 0),
  CHECK (capacidad_reservada_tbps >= 0),
  CHECK (capacidad_utilizada_tbps + capacidad_reservada_tbps <= capacidad_total_tbps),
  FOREIGN KEY (id_segmento) REFERENCES segmento(id_segmento),
  INDEX ix_historial_segmento_fecha (id_segmento,fecha)
) ENGINE=InnoDB;
CREATE TABLE reporte_generado (
  id_reporte BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo_reporte VARCHAR(30) NOT NULL UNIQUE,
  tipo ENUM('OPERACION_GENERAL','CAPACIDAD_UTILIZACION','INCIDENCIAS',
            'MANTENIMIENTOS','SERVICIOS_CLIENTES','INDICADORES_HISTORICOS') NOT NULL,
  id_segmento BIGINT UNSIGNED,
  fecha_inicio DATETIME NOT NULL,
  fecha_fin DATETIME NOT NULL,
  generado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  generado_por BIGINT UNSIGNED NOT NULL,
  CHECK (fecha_fin >= fecha_inicio),
  FOREIGN KEY (id_segmento) REFERENCES segmento(id_segmento),
  FOREIGN KEY (generado_por) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS reporte_contenido (
  id_reporte BIGINT UNSIGNED PRIMARY KEY,
  contenido LONGBLOB NOT NULL,
  FOREIGN KEY (id_reporte) REFERENCES reporte_generado(id_reporte)
) ENGINE=InnoDB;
