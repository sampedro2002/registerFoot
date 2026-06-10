# Ejecutar RegisterFoot desde IntelliJ IDEA

Ya hay dos *run configurations* listas en `.idea/runConfigurations/` (incluyen las
credenciales de BD `admin` / `BN2002sg`). Solo necesitas abrir el proyecto y darle Run.

## 1. Abrir el proyecto

`File ▸ Open…` → selecciona la **carpeta del proyecto** (la que tiene `pom.xml`).
IntelliJ lo detecta como proyecto **Maven** y descarga las dependencias solo.
Espera a que termine la indexación (barra inferior).

## 2. Configurar el JDK (una vez)

`File ▸ Project Structure ▸ Project`:
- **SDK**: Java 17 o superior (tienes JDK 21, sirve).
- **Language level**: 17.

> JavaFX NO viene del JDK: se descarga como dependencia Maven, así que cualquier
> JDK 17+ funciona.

## 3. Levantar MySQL (si no está corriendo)

```bash
docker start registerfoot-mysql        # si ya lo creaste antes
# o crearlo de cero:
docker run -d --name registerfoot-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=BN2002sg -e MYSQL_DATABASE=registerfoot \
  -e MYSQL_USER=admin -e MYSQL_PASSWORD=BN2002sg mysql:8
```

## 4. Ejecutar

Arriba a la derecha, en el selector de configuraciones, elige una:

| Configuración | Tipo | Cuándo usarla |
|---------------|------|---------------|
| **RegisterFoot (App)** | Application | Uso normal y **depuración** (botón 🐞 Debug). Recomendada. |
| **RegisterFoot (javafx:run)** | Maven | Si la anterior diera problemas de JavaFX. |

Pulsa **Run ▶**. Abre la ventana de login → entra con **`admin` / `admin123`**.

> Dos "admin" distintos: `admin/BN2002sg` es de **MySQL** (conexión);
> `admin/admin123` es el **login de la aplicación**.

## 5. Si aparece "JavaFX runtime components are missing"

No debería pasar (el `main` no extiende `Application`). Si ocurre, usa la
configuración **RegisterFoot (javafx:run)**, o añade en *VM options* de la
configuración App:
```
--module-path /ruta/a/javafx/lib --add-modules javafx.controls,javafx.fxml
```

## 6. Notas

- La configuración **App** corre la clase `com.registerfoot.RegisterFootApplication`
  con las variables `DB_USER`/`DB_PASSWORD` ya puestas. Si cambias la contraseña de
  MySQL, edítalas en `Run ▸ Edit Configurations… ▸ Environment variables`.
- El nombre de módulo esperado es `register-foot` (el `artifactId`). Si IntelliJ usa
  otro, abre `Edit Configurations…` y selecciónalo en el desplegable **module**.
- Las credenciales quedan en un archivo del repo; para un entorno real, quítalas de
  ahí y pásalas por variables de entorno del sistema.
