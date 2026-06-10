-- ======================================================================
--  RegisterFoot - Datos de prueba (idempotente con INSERT IGNORE)
--  Los usuarios se crean desde DataInitializer para garantizar el hash
--  BCrypt correcto. Aqui se cargan datos de referencia y demo.
-- ======================================================================

-- Concesiones
INSERT IGNORE INTO concesiones (id, codigo, nombre, nit, contacto, telefono, activo) VALUES
 (1, 'CONC-001', 'Mineria del Norte S.A.', '900111222-1', 'Carlos Ruiz',  '3001112233', TRUE),
 (2, 'CONC-002', 'Constructora Andina',   '900333444-2', 'Ana Gomez',    '3004445566', TRUE),
 (3, 'CONC-003', 'Casino Central',        '900555666-3', 'Luis Pardo',   '3007778899', TRUE);

-- Tipos de comida
INSERT IGNORE INTO tipos_comida (id, codigo, nombre, valor, activo) VALUES
 (1, 'DES', 'Desayuno',   8500.00,  TRUE),
 (2, 'ALM', 'Almuerzo',   14000.00, TRUE),
 (3, 'CEN', 'Cena',       12000.00, TRUE),
 (4, 'REF', 'Refrigerio', 5000.00,  TRUE);

-- Horarios de comida
INSERT IGNORE INTO horarios_comida (id, tipo_comida_id, hora_inicio, hora_fin, activo) VALUES
 (1, 1, '06:00:00', '09:30:00', TRUE),
 (2, 2, '11:30:00', '14:30:00', TRUE),
 (3, 3, '18:00:00', '20:30:00', TRUE),
 (4, 4, '15:00:00', '16:30:00', TRUE);

-- Categorias de personal (definen el limite de consumos por dia)
INSERT IGNORE INTO categorias_personal (id, codigo, nombre, limite_diario, activo) VALUES
 (1, 'NORMAL',   'Personal normal',  1, TRUE),
 (2, 'ESPECIAL', 'Personal especial', 2, TRUE);

-- Empleados de demostracion (categoria 1=NORMAL limite 1, 2=ESPECIAL limite 2)
INSERT IGNORE INTO empleados (id, codigo_biometrico, documento, nombres, apellidos, cargo, concesion_id, categoria_id, estado) VALUES
 (1, 'BIO-1001', '10203040', 'Juan',   'Perez Lopez',   'Operario',   1, 1, 'ACTIVO'),
 (2, 'BIO-1002', '20304050', 'Maria',  'Diaz Castro',   'Supervisora',1, 2, 'ACTIVO'),
 (3, 'BIO-1003', '30405060', 'Pedro',  'Ramirez Soto',  'Conductor',  2, 1, 'ACTIVO'),
 (4, 'BIO-1004', '40506070', 'Lucia',  'Torres Vega',   'Tecnica',    2, 1, 'INACTIVO'),
 (5, 'BIO-1005', '50607080', 'Andres', 'Mejia Rojas',   'Ayudante',   3, 2, 'ACTIVO');

-- Impresoras
INSERT IGNORE INTO impresoras (id, nombre, tipo, destino, char_por_linea, por_defecto, activo) VALUES
 (1, 'Termica Cocina', 'MOCK', 'simulada', 42, TRUE, TRUE);

-- Dispositivos biometricos
INSERT IGNORE INTO dispositivos_biometricos (id, nombre, marca, ip, puerto, activo) VALUES
 (1, 'Lector Entrada', 'MOCK', '192.168.1.201', 4370, TRUE);
