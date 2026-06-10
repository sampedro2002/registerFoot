package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.Rol;
import com.registerfoot.security.AuthService;
import com.registerfoot.ui.ViewManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/** Shell principal: barra lateral por rol + area de contenido conmutable. */
@Component
@Scope("prototype")
public class MainView {

    private final List<ModuleView> modulos;
    private final AuthService authService;
    private final ObjectProvider<ViewManager> viewManager;

    private BorderPane root;
    private StackPane content;
    private VBox menu;

    public MainView(List<ModuleView> modulos, AuthService authService,
                    ObjectProvider<ViewManager> viewManager) {
        this.modulos = modulos.stream().sorted(Comparator.comparingInt(ModuleView::orden)).toList();
        this.authService = authService;
        this.viewManager = viewManager;
    }

    public Parent build() {
        Rol rol = authService.getUsuarioActual().getRol();

        root = new BorderPane();
        content = new StackPane();
        content.getStyleClass().add("content");

        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("RegisterFoot");
        brand.getStyleClass().add("sidebar-brand");
        Label user = new Label(authService.getUsuarioActual().getNombreCompleto()
                + "\n[" + rol + "]");
        user.getStyleClass().add("sidebar-user");
        sidebar.getChildren().addAll(brand, user);

        menu = new VBox(2);
        for (ModuleView m : modulos) {
            if (!m.visiblePara(rol)) continue;
            Button b = new Button(m.icono() + "  " + m.nombre());
            b.getStyleClass().add("btn-ghost");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(e -> seleccionar(b, m));
            menu.getChildren().add(b);
        }
        sidebar.getChildren().add(menu);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button salir = new Button("Cerrar sesion");
        salir.getStyleClass().add("btn-ghost");
        salir.setMaxWidth(Double.MAX_VALUE);
        salir.setOnAction(e -> {
            authService.logout();
            viewManager.getObject().mostrarLogin();
        });
        sidebar.getChildren().addAll(spacer, salir);

        root.setLeft(sidebar);
        root.setCenter(content);

        // selecciona el primer modulo disponible
        if (!menu.getChildren().isEmpty()) {
            Button first = (Button) menu.getChildren().get(0);
            seleccionar(first, modulos.stream().filter(m -> m.visiblePara(rol)).findFirst().orElseThrow());
        }
        return root;
    }

    private void seleccionar(Button boton, ModuleView modulo) {
        menu.getChildren().forEach(n -> n.getStyleClass().remove("btn-active"));
        boton.getStyleClass().add("btn-active");
        content.getChildren().setAll(wrap(modulo));
    }

    private Node wrap(ModuleView modulo) {
        VBox box = new VBox(14);
        Label title = new Label(modulo.nombre());
        title.getStyleClass().add("page-title");
        Node body;
        try {
            body = modulo.crear();
        } catch (Exception ex) {
            body = new Label("Error cargando el modulo: " + ex.getMessage());
        }
        VBox.setVgrow(body, Priority.ALWAYS);
        box.getChildren().addAll(title, body);
        box.setAlignment(Pos.TOP_LEFT);
        return box;
    }
}
