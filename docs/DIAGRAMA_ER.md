# Diagrama Entidad-Relación — RegisterFoot

```mermaid
erDiagram
    USUARIOS {
        bigint id PK
        varchar username UK
        varchar password_hash
        varchar rol
        boolean activo
        int intentos_fallidos
    }
    CONCESIONES {
        bigint id PK
        varchar codigo UK
        varchar nombre
        varchar nit
        boolean activo
    }
    TIPOS_COMIDA {
        bigint id PK
        varchar codigo UK
        varchar nombre
        decimal valor
        boolean activo
    }
    HORARIOS_COMIDA {
        bigint id PK
        bigint tipo_comida_id FK
        time hora_inicio
        time hora_fin
        boolean activo
    }
    EMPLEADOS {
        bigint id PK
        varchar codigo_biometrico UK
        varchar documento UK
        varchar nombres
        varchar apellidos
        bigint concesion_id FK
        varchar estado
    }
    REGISTROS_ALIMENTACION {
        bigint id PK
        bigint empleado_id FK
        bigint tipo_comida_id FK
        bigint concesion_id FK
        date fecha
        time hora
        decimal valor
        varchar origen
    }
    CONTROL_CONSUMO {
        bigint id PK
        bigint empleado_id FK
        bigint tipo_comida_id FK
        date fecha
        bigint registro_id FK
    }
    TICKETS {
        bigint id PK
        varchar numero UK
        bigint registro_id FK
        bigint empleado_id FK
        bigint tipo_comida_id FK
        bigint concesion_id FK
        datetime fecha_hora
        decimal valor
        varchar qr_payload
        int reimpresiones
        varchar estado
    }
    AUDITORIA {
        bigint id PK
        varchar usuario
        varchar accion
        varchar entidad
        varchar resultado
        datetime fecha_hora
    }
    IMPRESORAS {
        bigint id PK
        varchar nombre UK
        varchar tipo
        boolean por_defecto
    }
    DISPOSITIVOS_BIOMETRICOS {
        bigint id PK
        varchar nombre UK
        varchar marca
        varchar ip
    }

    CONCESIONES   ||--o{ EMPLEADOS              : "emplea"
    CONCESIONES   ||--o{ REGISTROS_ALIMENTACION : "factura"
    TIPOS_COMIDA  ||--o{ HORARIOS_COMIDA        : "define ventana"
    TIPOS_COMIDA  ||--o{ REGISTROS_ALIMENTACION : "clasifica"
    EMPLEADOS     ||--o{ REGISTROS_ALIMENTACION : "consume"
    EMPLEADOS     ||--o{ CONTROL_CONSUMO        : "controla"
    TIPOS_COMIDA  ||--o{ CONTROL_CONSUMO        : "limita"
    REGISTROS_ALIMENTACION ||--|| CONTROL_CONSUMO : "respalda"
    REGISTROS_ALIMENTACION ||--|| TICKETS         : "genera"
    EMPLEADOS     ||--o{ TICKETS                 : "titular"
```

## Restricciones clave

- **`control_consumo`** tiene `UNIQUE (empleado_id, fecha, secuencia)` →
  garantiza el **límite de consumos por día** según la categoría del empleado
  (la `secuencia` 1..N hace atómico el tope ante concurrencia).
- **`categorias_personal.limite_diario`** define cuántas veces puede consumir un
  empleado por día (NORMAL=1, ESPECIAL=2…); `empleados.categoria_id` lo asigna.
- **`tickets.numero`** es único e imprimible (formato `RF-yyyyMMddHHmmss-NNNN`).
- **`empleados.codigo_biometrico`** y **`empleados.documento`** son únicos.
- Borrado **lógico** (campos `activo`/`estado`) para preservar integridad
  referencial e historia.

## Tablas

Base obligatoria: `concesiones`, `tipos_comida`, `empleados`,
`registros_alimentacion`.
Agregadas: `usuarios`, `horarios_comida`, `control_consumo`, `tickets`,
`auditoria`, `impresoras`, `dispositivos_biometricos`.

Script completo en `src/main/resources/db/schema.sql`.
