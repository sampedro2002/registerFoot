package com.registerfoot.ui;

import com.registerfoot.RegisterFootApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Application JavaFX que hospeda el contexto de Spring Boot.
 *  - init():  arranca Spring (fuera del hilo de UI).
 *  - start(): publica el Stage para que la UI se construya.
 *  - stop():  cierra el contexto ordenadamente.
 */
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.context = new SpringApplicationBuilder(RegisterFootApplication.class)
                .headless(false)            // necesario para AWT/printing/QR
                .run(args);
    }

    @Override
    public void start(Stage stage) {
        context.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }
}
