package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.ConcesionDTO;
import com.registerfoot.service.ConcesionService;
import com.registerfoot.ui.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

import java.util.Set;

/** CRUD de concesiones. */
@Component
public class ConcesionesView implements ModuleView {

    private final ConcesionService service;
    private final ViewManager view;

    private final ObservableList<ConcesionDTO> data = FXCollections.observableArrayList();
    private TextField fCodigo, fNombre, fNit, fContacto, fTelefono;
    private CheckBox fActivo;
    private TableView<ConcesionDTO> tabla;
    private Long editId;

    public ConcesionesView(ConcesionService service, ViewManager view) {
        this.service = service;
        this.view = view;
    }

    @Override public String nombre() { return "Concesiones"; }
    @Override public String icono() { return "🏢"; }
    @Override public int orden() { return 30; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR);
    }

    @Override
    public Node crear() {
        tabla = new TableView<>(data);
        tabla.getColumns().addAll(
                UiTables.col("Codigo", ConcesionDTO::codigo),
                UiTables.col("Nombre", ConcesionDTO::nombre),
                UiTables.col("NIT", ConcesionDTO::nit),
                UiTables.col("Contacto", ConcesionDTO::contacto),
                UiTables.col("Telefono", ConcesionDTO::telefono),
                UiTables.col("Activo", c -> c.activo() ? "Si" : "No"));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> cargar(b));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        fCodigo = new TextField(); fNombre = new TextField(); fNit = new TextField();
        fContacto = new TextField(); fTelefono = new TextField();
        fActivo = new CheckBox("Activo"); fActivo.setSelected(true);

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8); form.setPadding(new Insets(8, 0, 8, 0));
        form.addRow(0, new Label("Codigo"), fCodigo, new Label("Nombre"), fNombre);
        form.addRow(1, new Label("NIT"), fNit, new Label("Contacto"), fContacto);
        form.addRow(2, new Label("Telefono"), fTelefono, fActivo);

        Button nuevo = new Button("Nuevo"); nuevo.setOnAction(e -> limpiar());
        Button guardar = new Button("Guardar"); guardar.getStyleClass().add("btn-primary");
        guardar.setOnAction(e -> guardar());
        Button eliminar = new Button("Inactivar"); eliminar.setOnAction(e -> eliminar());

        data.setAll(service.listar());
        return new VBox(10, tabla, new Separator(), form, new HBox(8, nuevo, guardar, eliminar));
    }

    private void cargar(ConcesionDTO d) {
        if (d == null) return;
        editId = d.id();
        fCodigo.setText(d.codigo()); fNombre.setText(d.nombre()); fNit.setText(d.nit());
        fContacto.setText(d.contacto()); fTelefono.setText(d.telefono());
        fActivo.setSelected(d.activo());
    }

    private void guardar() {
        try {
            ConcesionDTO dto = new ConcesionDTO(editId, fCodigo.getText(), fNombre.getText(),
                    fNit.getText(), fContacto.getText(), fTelefono.getText(), fActivo.isSelected());
            if (editId == null) service.crear(dto); else service.actualizar(editId, dto);
            data.setAll(service.listar());
            limpiar();
        } catch (Exception ex) { view.error("Error", ex.getMessage()); }
    }

    private void eliminar() {
        if (editId == null) { view.warn("Concesiones", "Seleccione un registro."); return; }
        try { service.eliminar(editId); data.setAll(service.listar()); limpiar(); }
        catch (Exception ex) { view.error("Error", ex.getMessage()); }
    }

    private void limpiar() {
        editId = null;
        fCodigo.clear(); fNombre.clear(); fNit.clear(); fContacto.clear(); fTelefono.clear();
        fActivo.setSelected(true);
        tabla.getSelectionModel().clearSelection();
    }
}
