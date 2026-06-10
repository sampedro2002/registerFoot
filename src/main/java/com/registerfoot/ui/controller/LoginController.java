package com.registerfoot.ui.controller;

import com.registerfoot.security.AuthService;
import com.registerfoot.ui.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Controlador de la pantalla de autenticacion. */
@Component
@Scope("prototype")
public class LoginController {

    private final AuthService authService;
    private final ViewManager viewManager;

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label mensaje;
    @FXML private Button btnLogin;

    public LoginController(AuthService authService, ViewManager viewManager) {
        this.authService = authService;
        this.viewManager = viewManager;
    }

    @FXML
    public void initialize() {
        password.setOnAction(e -> onLogin());
        Platform.runLater(() -> username.requestFocus());
    }

    @FXML
    public void onLogin() {
        mensaje.setText("");
        String user = username.getText() == null ? "" : username.getText().trim();
        String pass = password.getText() == null ? "" : password.getText();
        if (user.isBlank() || pass.isBlank()) {
            mensaje.setText("Ingrese usuario y contrasena.");
            return;
        }
        btnLogin.setDisable(true);
        try {
            authService.login(user, pass);
            viewManager.mostrarPrincipal();
        } catch (Exception ex) {
            mensaje.setText(ex.getMessage());
            password.clear();
        } finally {
            btnLogin.setDisable(false);
        }
    }
}
