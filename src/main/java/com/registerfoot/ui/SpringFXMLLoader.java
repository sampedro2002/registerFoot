package com.registerfoot.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Carga vistas FXML resolviendo los controllers como beans de Spring, de modo
 * que los controllers pueden inyectar servicios por constructor.
 */
@Component
public class SpringFXMLLoader {

    private final ApplicationContext context;

    public SpringFXMLLoader(ApplicationContext context) { this.context = context; }

    public Parent load(String classpathFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(classpathFxml));
            loader.setControllerFactory(context::getBean);
            return loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar FXML: " + classpathFxml, e);
        }
    }
}
