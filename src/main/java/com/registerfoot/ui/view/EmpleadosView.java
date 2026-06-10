package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.EstadoEmpleado;
import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.CategoriaPersonalDTO;
import com.registerfoot.dto.ConcesionDTO;
import com.registerfoot.dto.EmpleadoDTO;
import com.registerfoot.service.CategoriaPersonalService;
import com.registerfoot.service.ConcesionService;
import com.registerfoot.service.EmpleadoService;
import com.registerfoot.ui.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Function;

/** CRUD de empleados con busqueda rapida. */
@Component
public class EmpleadosView implements ModuleView {

    private final EmpleadoService empleadoService;
    private final ConcesionService concesionService;
    private final CategoriaPersonalService categoriaService;
    private final ViewManager view;

    private final ObservableList<EmpleadoDTO> data = FXCollections.observableArrayList();
    private TableView<EmpleadoDTO> tabla;
    private TextField fCodigo, fDoc, fNombres, fApellidos, fCargo;
    private ComboBox<ConcesionDTO> fConcesion;
    private ComboBox<CategoriaPersonalDTO> fCategoria;
    private ComboBox<EstadoEmpleado> fEstado;
    private Long editId;

    public EmpleadosView(EmpleadoService empleadoService, ConcesionService concesionService,
                         CategoriaPersonalService categoriaService, ViewManager view) {
        this.empleadoService = empleadoService;
        this.concesionService = concesionService;
        this.categoriaService = categoriaService;
        this.view = view;
    }

    @Override public String nombre() { return "Empleados"; }
    @Override public String icono() { return "👥"; }
    @Override public int orden() { return 20; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR);
    }

    @Override
    public Node crear() {
        TextField buscar = new TextField();
        buscar.setPromptText("Buscar por documento, nombre o codigo...");
        buscar.textProperty().addListener((o, a, b) ->
                data.setAll(empleadoService.buscar(b, 0, 100)));
        HBox.setHgrow(buscar, Priority.ALWAYS);

        tabla = new TableView<>(data);
        tabla.getColumns().addAll(
                col("Codigo Bio", EmpleadoDTO::codigoBiometrico),
                col("Documento", EmpleadoDTO::documento),
                col("Nombres", EmpleadoDTO::nombres),
                col("Apellidos", EmpleadoDTO::apellidos),
                col("Cargo", EmpleadoDTO::cargo),
                col("Concesion", EmpleadoDTO::concesionNombre),
                col("Categoria", EmpleadoDTO::categoriaNombre),
                col("Estado", e -> e.estado() == null ? "" : e.estado().name()));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> cargarForm(b));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // Formulario
        fCodigo = new TextField(); fDoc = new TextField();
        fNombres = new TextField(); fApellidos = new TextField(); fCargo = new TextField();
        fConcesion = new ComboBox<>(FXCollections.observableArrayList(concesionService.listarActivas()));
        fConcesion.setConverter(new javafx.util.StringConverter<>() {
            public String toString(ConcesionDTO c) { return c == null ? "" : c.nombre(); }
            public ConcesionDTO fromString(String s) { return null; }
        });
        fCategoria = new ComboBox<>(FXCollections.observableArrayList(categoriaService.listarActivas()));
        fCategoria.setConverter(new javafx.util.StringConverter<>() {
            public String toString(CategoriaPersonalDTO c) {
                return c == null ? "" : c.nombre() + " (límite " + c.limiteDiario() + ")";
            }
            public CategoriaPersonalDTO fromString(String s) { return null; }
        });
        fEstado = new ComboBox<>(FXCollections.observableArrayList(EstadoEmpleado.values()));
        fEstado.getSelectionModel().select(EstadoEmpleado.ACTIVO);

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8); form.setPadding(new Insets(8, 0, 8, 0));
        form.addRow(0, new Label("Codigo Bio"), fCodigo, new Label("Documento"), fDoc);
        form.addRow(1, new Label("Nombres"), fNombres, new Label("Apellidos"), fApellidos);
        form.addRow(2, new Label("Cargo"), fCargo, new Label("Concesion"), fConcesion);
        form.addRow(3, new Label("Categoria"), fCategoria, new Label("Estado"), fEstado);

        Button nuevo = new Button("Nuevo");
        nuevo.setOnAction(e -> limpiar());
        Button guardar = new Button("Guardar");
        guardar.getStyleClass().add("btn-primary");
        guardar.setOnAction(e -> guardar());
        Button eliminar = new Button("Inactivar");
        eliminar.setOnAction(e -> eliminar());
        HBox acciones = new HBox(8, nuevo, guardar, eliminar);

        data.setAll(empleadoService.listar());

        VBox box = new VBox(10, new HBox(8, buscar), tabla, new Separator(), form, acciones);
        return box;
    }

    private void cargarForm(EmpleadoDTO d) {
        if (d == null) return;
        editId = d.id();
        fCodigo.setText(d.codigoBiometrico());
        fDoc.setText(d.documento());
        fNombres.setText(d.nombres());
        fApellidos.setText(d.apellidos());
        fCargo.setText(d.cargo());
        fEstado.getSelectionModel().select(d.estado());
        fConcesion.getItems().stream().filter(c -> c.id().equals(d.concesionId()))
                .findFirst().ifPresent(c -> fConcesion.getSelectionModel().select(c));
        fCategoria.getSelectionModel().clearSelection();
        if (d.categoriaId() != null) {
            fCategoria.getItems().stream().filter(c -> c.id().equals(d.categoriaId()))
                    .findFirst().ifPresent(c -> fCategoria.getSelectionModel().select(c));
        }
    }

    private void guardar() {
        try {
            ConcesionDTO c = fConcesion.getValue();
            if (c == null) { view.warn("Validacion", "Seleccione una concesion."); return; }
            CategoriaPersonalDTO cat = fCategoria.getValue();
            EmpleadoDTO dto = new EmpleadoDTO(editId, fCodigo.getText(), fDoc.getText(),
                    fNombres.getText(), fApellidos.getText(), fCargo.getText(),
                    c.id(), c.nombre(),
                    cat == null ? null : cat.id(), cat == null ? null : cat.nombre(),
                    fEstado.getValue());
            if (editId == null) empleadoService.crear(dto);
            else empleadoService.actualizar(editId, dto);
            data.setAll(empleadoService.listar());
            limpiar();
            view.info("Empleados", "Empleado guardado correctamente.");
        } catch (Exception ex) {
            view.error("Error", ex.getMessage());
        }
    }

    private void eliminar() {
        if (editId == null) { view.warn("Empleados", "Seleccione un empleado."); return; }
        try {
            empleadoService.eliminar(editId);
            data.setAll(empleadoService.listar());
            limpiar();
        } catch (Exception ex) {
            view.error("Error", ex.getMessage());
        }
    }

    private void limpiar() {
        editId = null;
        fCodigo.clear(); fDoc.clear(); fNombres.clear(); fApellidos.clear(); fCargo.clear();
        fConcesion.getSelectionModel().clearSelection();
        fCategoria.getSelectionModel().clearSelection();
        fEstado.getSelectionModel().select(EstadoEmpleado.ACTIVO);
        tabla.getSelectionModel().clearSelection();
    }

    private TableColumn<EmpleadoDTO, String> col(String titulo, Function<EmpleadoDTO, String> ext) {
        TableColumn<EmpleadoDTO, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(ext.apply(cd.getValue())));
        return c;
    }
}
