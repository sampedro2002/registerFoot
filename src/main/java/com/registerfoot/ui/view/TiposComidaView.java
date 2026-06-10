package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.TipoComidaDTO;
import com.registerfoot.service.TipoComidaService;
import com.registerfoot.ui.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/** CRUD de tipos de comida. */
@Component
public class TiposComidaView implements ModuleView {

    private final TipoComidaService service;
    private final ViewManager view;

    private final ObservableList<TipoComidaDTO> data = FXCollections.observableArrayList();
    private TextField fCodigo, fNombre, fValor;
    private CheckBox fActivo;
    private TableView<TipoComidaDTO> tabla;
    private Long editId;

    public TiposComidaView(TipoComidaService service, ViewManager view) {
        this.service = service;
        this.view = view;
    }

    @Override public String nombre() { return "Tipos de Comida"; }
    @Override public String icono() { return "🍔"; }
    @Override public int orden() { return 40; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR);
    }

    @Override
    public Node crear() {
        tabla = new TableView<>(data);
        tabla.getColumns().addAll(
                UiTables.col("Codigo", TipoComidaDTO::codigo),
                UiTables.col("Nombre", TipoComidaDTO::nombre),
                UiTables.col("Valor", t -> t.valor() == null ? "" : t.valor().toPlainString()),
                UiTables.col("Activo", t -> t.activo() ? "Si" : "No"));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> cargar(b));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        fCodigo = new TextField(); fNombre = new TextField(); fValor = new TextField();
        fActivo = new CheckBox("Activo"); fActivo.setSelected(true);

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8); form.setPadding(new Insets(8, 0, 8, 0));
        form.addRow(0, new Label("Codigo"), fCodigo, new Label("Nombre"), fNombre);
        form.addRow(1, new Label("Valor"), fValor, fActivo);

        Button nuevo = new Button("Nuevo"); nuevo.setOnAction(e -> limpiar());
        Button guardar = new Button("Guardar"); guardar.getStyleClass().add("btn-primary");
        guardar.setOnAction(e -> guardar());
        Button eliminar = new Button("Inactivar"); eliminar.setOnAction(e -> eliminar());

        data.setAll(service.listar());
        return new VBox(10, tabla, new Separator(), form, new HBox(8, nuevo, guardar, eliminar));
    }

    private void cargar(TipoComidaDTO d) {
        if (d == null) return;
        editId = d.id();
        fCodigo.setText(d.codigo()); fNombre.setText(d.nombre());
        fValor.setText(d.valor() == null ? "0" : d.valor().toPlainString());
        fActivo.setSelected(d.activo());
    }

    private void guardar() {
        try {
            BigDecimal valor = new BigDecimal(fValor.getText().isBlank() ? "0" : fValor.getText().trim());
            TipoComidaDTO dto = new TipoComidaDTO(editId, fCodigo.getText(), fNombre.getText(),
                    valor, fActivo.isSelected());
            if (editId == null) service.crear(dto); else service.actualizar(editId, dto);
            data.setAll(service.listar());
            limpiar();
        } catch (NumberFormatException nfe) {
            view.warn("Validacion", "El valor debe ser numerico.");
        } catch (Exception ex) { view.error("Error", ex.getMessage()); }
    }

    private void eliminar() {
        if (editId == null) { view.warn("Tipos de Comida", "Seleccione un registro."); return; }
        try { service.eliminar(editId); data.setAll(service.listar()); limpiar(); }
        catch (Exception ex) { view.error("Error", ex.getMessage()); }
    }

    private void limpiar() {
        editId = null;
        fCodigo.clear(); fNombre.clear(); fValor.clear(); fActivo.setSelected(true);
        tabla.getSelectionModel().clearSelection();
    }
}
