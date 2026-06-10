package com.registerfoot.ui;

import com.registerfoot.ui.view.MainView;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Coordina la navegacion entre escenas (login <-> aplicacion principal) y
 * expone utilidades de dialogos. Es el unico que conoce el Stage primario.
 */
@Component
public class ViewManager {

    private final SpringFXMLLoader fxmlLoader;
    private final ObjectProvider<MainView> mainViewProvider;
    private Stage stage;

    public ViewManager(SpringFXMLLoader fxmlLoader, ObjectProvider<MainView> mainViewProvider) {
        this.fxmlLoader = fxmlLoader;
        this.mainViewProvider = mainViewProvider;
    }

    @EventListener
    public void onStageReady(StageReadyEvent event) {
        this.stage = event.getStage();
        stage.setTitle("RegisterFoot - Sistema de Tickets de Alimentacion");
        mostrarLogin();
        stage.show();
    }

    public void mostrarLogin() {
        Parent root = fxmlLoader.load("/fxml/login.fxml");
        aplicarEscena(root, 420, 520, false);
    }

    public void mostrarPrincipal() {
        Parent root = mainViewProvider.getObject().build();
        aplicarEscena(root, 1180, 720, true);
        stage.centerOnScreen();
    }

    private void aplicarEscena(Parent root, double w, double h, boolean maximizable) {
        Scene scene = new Scene(root, w, h);
        var css = getClass().getResource("/css/styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.setResizable(maximizable);
    }

    public Stage getStage() { return stage; }

    public void info(String titulo, String msg)  { alerta(Alert.AlertType.INFORMATION, titulo, msg); }
    public void error(String titulo, String msg) { alerta(Alert.AlertType.ERROR, titulo, msg); }
    public void warn(String titulo, String msg)  { alerta(Alert.AlertType.WARNING, titulo, msg); }

    private void alerta(Alert.AlertType tipo, String titulo, String msg) {
        Runnable r = () -> {
            Alert a = new Alert(tipo);
            a.setTitle(titulo);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        };
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }
}
