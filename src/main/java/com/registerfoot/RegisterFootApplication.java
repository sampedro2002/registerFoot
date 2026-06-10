package com.registerfoot;

import com.registerfoot.ui.JavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada. Delega el arranque a JavaFX; {@link JavaFxApplication}
 * inicializa el contexto de Spring durante su fase {@code init()}.
 * {@code AppProperties} se enlaza por ser un @Component @ConfigurationProperties.
 */
@SpringBootApplication
public class RegisterFootApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}
