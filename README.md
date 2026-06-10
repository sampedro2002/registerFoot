# RegisterFoot

Aplicación de escritorio para la **gestión de tickets de alimentación mediante
dispositivos biométricos**. Un empleado se identifica en un lector (huella /
rostro), el sistema valida su derecho de consumo y genera e imprime
automáticamente un ticket en impresora térmica.

> Proyecto académico — Semestre 6, Desarrollo de Software.

---

## 1. Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Backend / IoC | Spring Boot 3.2 (Data JPA, Security, Validation) |
| ORM | Hibernate / JPA |
| UI escritorio | JavaFX 21 (FXML + programático) |
| Base de datos | MySQL 8 |
| Build | Maven |
| Reportes | JasperReports (PDF) + Apache POI (Excel) |
| Códigos QR | ZXing |
| Impresión térmica | escpos-coffee (ESC/POS) + Java Print Service |
| Seguridad | Spring Security (BCrypt, roles, method security) |

## 2. Arquitectura

Arquitectura **en capas + Clean Architecture + principios SOLID**:

```
ui (JavaFX)  ->  service (casos de uso)  ->  repository (Spring Data)  ->  domain (entidades)
                       |                            |
              biometric / printing (interfaces desacopladas + adaptadores)
                  dto (transporte)   ·   config / security (infraestructura)
```

- **Repository Pattern**: interfaces `*Repository` (Spring Data JPA).
- **DTO**: `record`/JavaBeans para no exponer entidades a la UI.
- **DIP**: `BiometricProvider` y `TicketPrinter` son interfaces; el SDK/hardware
  concreto se inyecta por configuración (Factory + `@Bean`).
- **Auditoría transversal** con transacción independiente (`REQUIRES_NEW`).

Ver detalle en [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md),
[`docs/DIAGRAMA_ER.md`](docs/DIAGRAMA_ER.md) y
[`docs/CASOS_USO.md`](docs/CASOS_USO.md).

## 3. Funcionalidades

Autenticación con 4 roles · CRUD de empleados / concesiones / tipos de comida /
horarios / **categorías de personal** · gestión de impresoras y biométricos ·
dashboard con KPIs · reportes PDF/Excel · auditoría completa · reimpresión de
tickets · **control de consumo diario por categoría** (límite de consumos/día
configurable: NORMAL=1, ESPECIAL=2…, garantizado por `UNIQUE(empleado, fecha,
secuencia)` en BD) · registro automático de fecha/hora · búsqueda rápida de
empleados.

### Flujo biométrico (caso de uso central)

`código → empleado → estado activo → horario vigente → consumo previo →
registrar → generar ticket → imprimir → auditar`
(implementado en `service/ConsumoService.java`).

## 4. Roles y credenciales por defecto

Creadas automáticamente al primer arranque (`config/DataInitializer.java`):

| Usuario | Contraseña | Rol | Acceso |
|---------|-----------|-----|--------|
| `admin` | `admin123` | ADMINISTRADOR | Todo |
| `supervisor` | `supervisor123` | SUPERVISOR | Operación + maestros + reportes |
| `operador` | `operador123` | OPERADOR | Dashboard, Consumos, Tickets |
| `auditor` | `auditor123` | AUDITOR | Dashboard, Reportes, Auditoría, Tickets |

> Cambie estas contraseñas en producción.

## 5. Ejecución rápida

Requisitos: **JDK 17+**, **Maven**, **MySQL 8** en ejecución.

```bash
# 1. Crear/usar la BD (la URL incluye createDatabaseIfNotExist=true)
#    Ajuste credenciales por variables de entorno si es necesario:
export DB_USER=root DB_PASSWORD=tu_clave

# 2. Ejecutar (usa el Maven Wrapper incluido; no requiere Maven instalado)
./mvnw javafx:run
#   o bien
./mvnw spring-boot:run
```

El esquema (`db/schema.sql`) y los datos de prueba (`db/seed.sql`) se cargan
solos al arrancar.

## 6. Configuración (`src/main/resources/application.yml`)

```yaml
registerfoot:
  biometric:
    provider: MOCK        # MOCK | ZKTECO | SUPREMA | ANVIZ | HIKVISION
    device-ip: 192.168.1.201
    device-port: 4370
  printing:
    backend: MOCK         # MOCK | ESC_POS | JAVA_PRINT
    printer-name: ""      # vacío = impresora por defecto del SO
```

Con `provider: MOCK` el lector se simula desde el módulo **Consumos** (campo de
texto o botones `BIO-1001`…`BIO-1005`). Con `backend: MOCK` el ticket se
"imprime" al log. Cambie a un valor real para usar hardware.

## 7. Estructura del proyecto

```
src/main/java/com/registerfoot/
├── RegisterFootApplication.java     # main -> lanza JavaFX
├── config/        # AppProperties, Security, Biometric/Printing factories, seed
├── domain/        # entity/ + enums/
├── repository/    # Spring Data JPA
├── dto/           # records / JavaBeans de transporte
├── service/       # casos de uso (Consumo, Ticket, CRUDs, Dashboard, Report, Auditoria)
├── security/      # AuthService, UserDetails, UserDetailsService
├── biometric/     # BiometricProvider + Mock + adaptadores ZKTeco/Suprema/Anviz/Hikvision
├── printing/      # TicketPrinter + ESC/POS + JavaPrint + Mock + QR
├── exception/     # excepciones de negocio
└── ui/            # JavaFX: bootstrap, ViewManager, controllers, view/ (módulos)
src/main/resources/
├── application.yml
├── db/{schema.sql, seed.sql}
├── fxml/login.fxml
├── css/styles.css
└── reports/consumos.jrxml
```

## 8. Integración de hardware real

Las interfaces `BiometricProvider` y `TicketPrinter` aíslan el hardware. Para
ZKTeco/Suprema/Anviz/Hikvision, implemente los `TODO` del adaptador
correspondiente en `biometric/` (envolviendo el SDK del fabricante) y cambie
`registerfoot.biometric.provider`. La impresión ESC/POS ya está implementada;
solo configure el nombre de la impresora.
