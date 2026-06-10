-- ======================================================================
--  RegisterFoot - Esquema de base de datos (MySQL 8)
--  Sistema de gestion de tickets de alimentacion biometricos
-- ======================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------
--  USUARIOS Y SEGURIDAD
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(60)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    email           VARCHAR(120),
    rol             VARCHAR(20)  NOT NULL,           -- ADMINISTRADOR|SUPERVISOR|OPERADOR|AUDITOR
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos INT        NOT NULL DEFAULT 0,
    ultimo_acceso   DATETIME,
    creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_usuarios_rol (rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  CONCESIONES (empresas/contratos que pagan la alimentacion)
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS concesiones (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(30)  NOT NULL UNIQUE,
    nombre          VARCHAR(150) NOT NULL,
    nit             VARCHAR(30),
    contacto        VARCHAR(120),
    telefono        VARCHAR(30),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  TIPOS DE COMIDA (desayuno, almuerzo, cena, refrigerio...)
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tipos_comida (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(30)   NOT NULL UNIQUE,
    nombre          VARCHAR(80)   NOT NULL,
    valor           DECIMAL(12,2) NOT NULL DEFAULT 0,
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    creado_en       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  HORARIOS DE COMIDA (ventana horaria valida por tipo de comida)
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS horarios_comida (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_comida_id  BIGINT      NOT NULL,
    hora_inicio     TIME        NOT NULL,
    hora_fin        TIME        NOT NULL,
    activo          BOOLEAN     NOT NULL DEFAULT TRUE,
    creado_en       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_horario_tipo FOREIGN KEY (tipo_comida_id) REFERENCES tipos_comida(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  CATEGORIAS DE PERSONAL (definen el limite de consumos por dia)
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias_personal (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(30)  NOT NULL UNIQUE,
    nombre          VARCHAR(80)  NOT NULL,
    limite_diario   INT          NOT NULL DEFAULT 1,   -- max consumos por dia
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  EMPLEADOS
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS empleados (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_biometrico   VARCHAR(60)  NOT NULL UNIQUE,    -- id que envia el lector
    documento           VARCHAR(30)  NOT NULL UNIQUE,
    nombres             VARCHAR(100) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    cargo               VARCHAR(80),
    concesion_id        BIGINT       NOT NULL,
    categoria_id        BIGINT,                                  -- limite diario; NULL => 1
    estado              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',  -- ACTIVO|INACTIVO|SUSPENDIDO
    creado_en           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_empleado_concesion FOREIGN KEY (concesion_id) REFERENCES concesiones(id),
    CONSTRAINT fk_empleado_categoria FOREIGN KEY (categoria_id) REFERENCES categorias_personal(id),
    INDEX idx_empleado_biometrico (codigo_biometrico),
    INDEX idx_empleado_nombre (apellidos, nombres)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  REGISTROS DE ALIMENTACION (cada transaccion de consumo)
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registros_alimentacion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    empleado_id     BIGINT        NOT NULL,
    tipo_comida_id  BIGINT        NOT NULL,
    concesion_id    BIGINT        NOT NULL,
    fecha           DATE          NOT NULL,
    hora            TIME          NOT NULL,
    valor           DECIMAL(12,2) NOT NULL DEFAULT 0,
    origen          VARCHAR(20)   NOT NULL DEFAULT 'BIOMETRICO', -- BIOMETRICO|MANUAL
    usuario_id      BIGINT,                                      -- operador si fue manual
    creado_en       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_empleado   FOREIGN KEY (empleado_id)    REFERENCES empleados(id),
    CONSTRAINT fk_reg_tipo       FOREIGN KEY (tipo_comida_id) REFERENCES tipos_comida(id),
    CONSTRAINT fk_reg_concesion  FOREIGN KEY (concesion_id)   REFERENCES concesiones(id),
    CONSTRAINT fk_reg_usuario    FOREIGN KEY (usuario_id)     REFERENCES usuarios(id),
    INDEX idx_reg_fecha (fecha),
    INDEX idx_reg_empleado_fecha (empleado_id, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  CONTROL DE CONSUMO (1 fila por consumo; secuencia 1..N en el dia)
--  Garantiza el limite diario por categoria: UNIQUE(empleado, fecha,
--  secuencia) impide exceder el cupo incluso ante consumos concurrentes.
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS control_consumo (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    empleado_id     BIGINT   NOT NULL,
    tipo_comida_id  BIGINT   NOT NULL,
    fecha           DATE     NOT NULL,
    secuencia       INT      NOT NULL,          -- numero de consumo del dia (1,2,...)
    registro_id     BIGINT   NOT NULL,
    creado_en       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_control UNIQUE (empleado_id, fecha, secuencia),
    CONSTRAINT fk_control_empleado FOREIGN KEY (empleado_id)    REFERENCES empleados(id),
    CONSTRAINT fk_control_tipo     FOREIGN KEY (tipo_comida_id) REFERENCES tipos_comida(id),
    CONSTRAINT fk_control_registro FOREIGN KEY (registro_id)    REFERENCES registros_alimentacion(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  TICKETS
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tickets (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero          VARCHAR(40)   NOT NULL UNIQUE,    -- numero unico imprimible
    registro_id     BIGINT        NOT NULL,
    empleado_id     BIGINT        NOT NULL,
    tipo_comida_id  BIGINT        NOT NULL,
    concesion_id    BIGINT        NOT NULL,
    fecha_hora      DATETIME      NOT NULL,
    valor           DECIMAL(12,2) NOT NULL,
    qr_payload      VARCHAR(255)  NOT NULL,
    impreso         BOOLEAN       NOT NULL DEFAULT FALSE,
    reimpresiones   INT           NOT NULL DEFAULT 0,
    estado          VARCHAR(20)   NOT NULL DEFAULT 'GENERADO', -- GENERADO|IMPRESO|ANULADO
    creado_en       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ticket_registro  FOREIGN KEY (registro_id)    REFERENCES registros_alimentacion(id),
    CONSTRAINT fk_ticket_empleado  FOREIGN KEY (empleado_id)    REFERENCES empleados(id),
    CONSTRAINT fk_ticket_tipo      FOREIGN KEY (tipo_comida_id) REFERENCES tipos_comida(id),
    CONSTRAINT fk_ticket_concesion FOREIGN KEY (concesion_id)   REFERENCES concesiones(id),
    INDEX idx_ticket_fecha (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  AUDITORIA (trazabilidad completa de acciones)
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario         VARCHAR(60),
    accion          VARCHAR(40)  NOT NULL,    -- LOGIN|CREAR|EDITAR|ELIMINAR|CONSUMO|IMPRIMIR|REIMPRIMIR...
    entidad         VARCHAR(60),
    entidad_id      VARCHAR(40),
    detalle         VARCHAR(500),
    resultado       VARCHAR(20)  NOT NULL DEFAULT 'OK', -- OK|ERROR|RECHAZADO
    ip              VARCHAR(45),
    fecha_hora      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_aud_fecha (fecha_hora),
    INDEX idx_aud_usuario (usuario),
    INDEX idx_aud_accion (accion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  IMPRESORAS (catalogo configurable)
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS impresoras (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL UNIQUE,
    tipo            VARCHAR(20)  NOT NULL DEFAULT 'ESC_POS', -- ESC_POS|JAVA_PRINT|MOCK
    destino         VARCHAR(150),  -- nombre del SO / IP:puerto
    char_por_linea  INT          NOT NULL DEFAULT 42,
    por_defecto     BOOLEAN      NOT NULL DEFAULT FALSE,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------
--  DISPOSITIVOS BIOMETRICOS (catalogo configurable)
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dispositivos_biometricos (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL UNIQUE,
    marca           VARCHAR(30)  NOT NULL DEFAULT 'MOCK', -- ZKTECO|SUPREMA|ANVIZ|HIKVISION|MOCK
    ip              VARCHAR(45),
    puerto          INT,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
