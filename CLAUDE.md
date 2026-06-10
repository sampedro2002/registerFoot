# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Qué es

App de **escritorio** (JavaFX) para tickets de alimentación biométricos: un empleado se
identifica en un lector → el sistema valida derecho de consumo → genera e imprime un ticket
térmico. Java 17 · Spring Boot 3.2 · JavaFX 21 · MySQL 8 · Hibernate/JPA · Spring Security ·
JasperReports · ESC/POS. Detalle en `README.md` y `docs/` (ARQUITECTURA, DIAGRAMA_ER, CASOS_USO).

## Comandos

> **No hay Maven instalado en el sistema** (solo JDK 21). Usa el **Maven Wrapper**
> `./mvnw` (descarga Maven 3.9.9 a `~/.m2/wrapper` en la 1ª ejecución; ya no depende
> de `/tmp`). La red (Maven Central) funciona.

```bash
./mvnw compile                 # compilar
./mvnw test                    # todos los tests
./mvnw test -Dtest=HorarioComidaTest          # una clase de test
./mvnw test -Dtest=HorarioComidaTest#incluyeHoraDentroDeLaVentana   # un método
./mvnw package -DskipTests     # fat jar -> target/register-foot.jar (~90MB)
DB_USER=admin DB_PASSWORD=BN2002sg ./mvnw javafx:run   # EJECUTAR (requiere display + MySQL 8)
```

BD por variables de entorno: `DB_HOST DB_PORT DB_USER DB_PASSWORD` (la URL trae
`createDatabaseIfNotExist=true`). Login por defecto: `admin / admin123` (también
`supervisor/supervisor123`, `operador/operador123`, `auditor/auditor123`).

## Arquitectura (big picture)

Capas con regla de dependencia hacia el dominio (`ui → service → repository → domain`).
El hardware se aísla tras **interfaces** y se inyecta por configuración (DIP):

- **`service/ConsumoService`** es el corazón: implementa el flujo biométrico de 9 pasos
  (código → empleado → estado → horario vigente → consumo previo → registrar → ticket →
  imprimir → auditar). Es `@Transactional` con **self-injection** (`@Lazy ConsumoService self`)
  para que el rollback del candado de duplicados funcione vía proxy.
- **`biometric/`** — `BiometricProvider` (interfaz) + `AbstractBiometricProvider` (Template
  Method + Observer) + `MockBiometricProvider` (funcional, sin hardware) + adaptadores stub
  ZKTeco/Suprema/Anviz/Hikvision (con `TODO` donde se enchufa el SDK). La implementación activa
  la elige `BiometricProviderFactory` según `registerfoot.biometric.provider`.
- **`printing/`** — `TicketPrinter` (interfaz) + `EscPosTicketPrinter` (real, escpos-coffee) +
  `JavaPrintTicketPrinter` + `MockTicketPrinter`; elige `PrintingConfig` según
  `registerfoot.printing.backend`. QR con ZXing (`QrCodeGenerator`).
- **`ui/`** — Integración Spring+JavaFX: `RegisterFootApplication.main` lanza
  `JavaFxApplication` (arranca Spring en `init()`, publica `StageReadyEvent`). `ViewManager`
  conmuta login ↔ shell. Cada módulo del menú implementa **`ui/view/ModuleView`** (declara
  nombre, icono, roles permitidos y construye su `Node`); `MainView` arma el sidebar filtrado
  por rol. Login usa FXML (`fxml/login.fxml` + `LoginController`); el resto es programático.

**Hardware en MOCK por defecto** (`application.yml`): el lector se simula desde el módulo
Consumos (botones `BIO-1001…1005`) y la impresión va al log. Cambiar `provider`/`backend` para
hardware real.

## Convenciones y trampas no obvias

- **DTOs son `record`** → en `TableView` usar extractores funcionales (`UiTables.col(titulo,
  Dto::campo)` o `ui/view/EmpleadosView` helper), **nunca `PropertyValueFactory`** (busca
  getters `getX` que los records no tienen → columnas vacías).
- **Esquema lo crea `db/schema.sql`** (`spring.jpa.hibernate.ddl-auto=none`), no Hibernate. Los
  datos de prueba están en `db/seed.sql` (idempotente con `INSERT IGNORE`); los **usuarios** los
  crea `config/DataInitializer` para garantizar el hash BCrypt.
- **Límite de consumos por día por CATEGORÍA de personal** (no por tipo de comida). Cada
  empleado tiene una `CategoriaPersonal` con `limiteDiario` (NORMAL=1, ESPECIAL=2).
  `ConsumoService` cuenta los consumos del día y rechaza al alcanzar el límite. `control_consumo`
  lleva `secuencia` 1..N con `UNIQUE(empleado, fecha, secuencia)`, que hace atómico el tope ante
  concurrencia (motivo de rechazo `LIMITE_DIARIO_ALCANZADO`).
- **Borrado lógico** en todos los CRUD (campos `activo`/`estado`), nunca `DELETE` físico, para
  preservar integridad referencial e historia.
- **Auditoría** (`AuditoriaService`) usa `REQUIRES_NEW` para persistir aunque el negocio haga
  rollback.
- Autorización: roles en `domain/enums/Rol` (ADMINISTRADOR/SUPERVISOR/OPERADOR/AUDITOR), aplicada
  en el sidebar (`ModuleView.rolesPermitidos`) y disponible por método (`@EnableMethodSecurity`).
- Config tipada en `config/AppProperties` (prefijo `registerfoot.*`); es `@Component
  @ConfigurationProperties` (no usar `@ConfigurationPropertiesScan`, duplicaría el bean).

## Estado y pendientes

Construido desde cero; compila, pasa tests y empaqueta. **Stub/pendiente**: SDKs biométricos
reales (solo estructura + `TODO`); gestión de usuarios en la UI (el `UsuarioService` ya existe,
falta la vista); Maven Wrapper (`mvnw`); plantilla `.jrxml` del ticket más elaborada.
