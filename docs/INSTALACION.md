# Manual de instalación — RegisterFoot

## 1. Requisitos previos

| Componente | Versión mínima | Notas |
|------------|----------------|-------|
| JDK | 17 (probado con 21) | `java -version` |
| Maven | 3.8+ | `mvn -v` |
| MySQL Server | 8.0 | servicio en ejecución |
| SO | Windows / Linux / macOS con entorno gráfico | JavaFX requiere display |
| Impresora térmica (opcional) | ESC/POS | para impresión real |
| Lector biométrico (opcional) | ZKTeco/Suprema/Anviz/Hikvision | para captura real |

## 2. Preparar la base de datos

No es obligatorio crear la base manualmente: la URL JDBC incluye
`createDatabaseIfNotExist=true`. Si prefiere crearla a mano:

```sql
CREATE DATABASE registerfoot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'registerfoot'@'localhost' IDENTIFIED BY 'cambia_esta_clave';
GRANT ALL PRIVILEGES ON registerfoot.* TO 'registerfoot'@'localhost';
FLUSH PRIVILEGES;
```

El esquema y los datos de prueba se cargan automáticamente al arrancar desde
`src/main/resources/db/schema.sql` y `seed.sql`.

## 3. Configurar la conexión

Edite `src/main/resources/application.yml` o use variables de entorno:

| Variable | Por defecto | Descripción |
|----------|-------------|-------------|
| `DB_HOST` | `localhost` | host de MySQL |
| `DB_PORT` | `3306` | puerto |
| `DB_USER` | `root` | usuario |
| `DB_PASSWORD` | `root` | contraseña |

```bash
export DB_USER=registerfoot
export DB_PASSWORD=cambia_esta_clave
```

## 4. Compilar

```bash
mvn clean package -DskipTests
```

Genera `target/register-foot.jar`.

## 5. Ejecutar

**Opción A — durante desarrollo (recomendada):**
```bash
mvn javafx:run
```

**Opción B — Spring Boot plugin:**
```bash
mvn spring-boot:run
```

**Opción C — jar empaquetado** (requiere los módulos JavaFX en el runtime; si su
JDK no incluye JavaFX, use la Opción A):
```bash
java -jar target/register-foot.jar
```

## 6. Primer ingreso

Use `admin` / `admin123`. El sistema crea los 4 usuarios de rol al primer
arranque (ver README, sección 4).

## 7. Configurar hardware real

### Impresora térmica ESC/POS
1. Instale el driver de la impresora en el SO.
2. En `application.yml`:
   ```yaml
   registerfoot:
     printing:
       backend: ESC_POS
       printer-name: "Nombre exacto en el SO"   # vacío = por defecto
       char-per-line: 42
   ```

### Lector biométrico
1. Instale el SDK del fabricante.
2. Implemente los `TODO` del adaptador en `biometric/` (ZKTeco, Suprema, Anviz
   o Hikvision) envolviendo el SDK.
3. Configure:
   ```yaml
   registerfoot:
     biometric:
       provider: ZKTECO
       device-ip: 192.168.1.201
       device-port: 4370
   ```

## 8. Solución de problemas

| Síntoma | Causa probable | Solución |
|---------|----------------|----------|
| `Access denied for user` | credenciales MySQL | revise `DB_USER`/`DB_PASSWORD` |
| La ventana no abre | sin entorno gráfico | ejecute en escritorio, no por SSH headless |
| `No hay impresora por defecto` | backend ESC/POS sin impresora | use `backend: MOCK` o configure impresora |
| Tablas vacías | scripts no ejecutados | revise logs de `spring.sql.init` |
| `ClassNotFound javafx.*` | JavaFX no en runtime | use `mvn javafx:run` |
