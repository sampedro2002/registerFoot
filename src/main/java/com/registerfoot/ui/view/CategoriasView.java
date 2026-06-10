package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.CategoriaPersonalDTO;
import com.registerfoot.service.CategoriaPersonalService;
import com.registerfoot.ui.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

import java.util.Set;

/** CRUD de categorías de personal y su límite de consumos por día. */
@Component
public class CategoriasView implements ModuleView {

    private final CategoriaPersonalService service;
    private final ViewManager view;

    private final ObservableList<CategoriaPersonalDTO> data = FXCollections.observableArrayList();
    private TextField fCodigo, fNombre, fLimite;
    private CheckBox fActivo;
    private TableView<CategoriaPersonalDTO> tabla;
    private Long editId;

    public CategoriasView(CategoriaPersonalService service, ViewManager view) {
        this.service = service;
        this.view = view;
    }

    @Override public String nombre() { return "Categorias"; }
    @Override public String icono() { return "🏷"; }
    @Override public int orden() { return 35; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR);
    }

    @Override
    public Node crear() {
        tabla = new TableView<>(data);
        tabla.getColumns().addAll(
                UiTables.col("Codigo", CategoriaPersonalDTO::codigo),
                UiTables.col("Nombre", CategoriaPersonalDTO::nombre),
                UiTables.col("Limite/dia", c -> String.valueOf(c.limiteDiario())),
                UiTables.col("Activo", c -> c.activo() ? "Si" : "No"));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> cargar(b));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        fCodigo = new TextField(); fNombre = new TextField(); fLimite = new TextField("1");
        fActivo = new CheckBox("Activo"); fActivo.setSelected(true);

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8); form.setPadding(new Insets(8, 0, 8, 0));
        form.addRow(0, new Label("Codigo"), fCodigo, new Label("Nombre"), fNombre);
        form.addRow(1, new Label("Limite por dia"), fLimite, fActivo);

        Button nuevo = new Button("Nuevo"); nuevo.setOnAction(e -> limpiar());
        Button guardar = new Button("Guardar"); guardar.getStyleClass().add("btn-primary");
        guardar.setOnAction(e -> guardar());
        Button eliminar = new Button("Inactivar"); eliminar.setOnAction(e -> eliminar());

        Label nota = new Label("El limite es el total de consumos por dia (sin importar el tipo). "
                + "Ej: NORMAL=1, ESPECIAL=2.");
        nota.setStyle("-fx-text-fill:#64748b;");

        data.setAll(service.listar());
        return new VBox(10, tabla, new Separator(), form, nota,
                new HBox(8, nuevo, guardar, eliminar));
    }

    private void cargar(CategoriaPersonalDTO d) {
        if (d == null) return;
        editId = d.id();
        fCodigo.setText(d.codigo()); fNombre.setText(d.nombre());
        fLimite.setText(String.valueOf(d.limiteDiario()));
        fActivo.setSelected(d.activo());
    }

    private void guardar() {
        try {
            int limite = Integer.parseInt(fLimite.getText().trim());
            if (limite < 1) { view.warn("Validacion", "El limite debe ser >= 1."); return; }
            CategoriaPersonalDTO dto = new CategoriaPersonalDTO(editId, fCodigo.getText(),
                    fNombre.getText(), limite, fActivo.isSelected());
            if (editId == null) service.crear(dto); else service.actualizar(editId, dto);
            data.setAll(service.listar());
            limpiar();
        } catch (NumberFormatException nfe) {
            view.warn("Validacion", "El limite debe ser un numero entero.");
        } catch (Exception ex) { view.error("Error", ex.getMessage()); }
    }

    private void eliminar() {
        if (editId == null) { view.warn("Categorias", "Seleccione un registro."); return; }
        try { service.eliminar(editId); data.setAll(service.listar()); limpiar(); }
        catch (Exception ex) { view.error("Error", ex.getMessage()); }
    }

    private void limpiar() {
        editId = null;
        fCodigo.clear(); fNombre.clear(); fLimite.setText("1"); fActivo.setSelected(true);
        tabla.getSelectionModel().clearSelection();
    }
}
