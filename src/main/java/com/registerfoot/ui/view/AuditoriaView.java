package com.registerfoot.ui.view;

import com.registerfoot.domain.entity.Auditoria;
import com.registerfoot.domain.enums.Rol;
import com.registerfoot.service.AuditoriaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/** Consulta de la bitacora de auditoria (solo lectura). */
@Component
public class AuditoriaView implements ModuleView {

    private static final DateTimeFormatter FH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AuditoriaService service;
    private final ObservableList<Auditoria> data = FXCollections.observableArrayList();
    private DatePicker desde, hasta;

    public AuditoriaView(AuditoriaService service) { this.service = service; }

    @Override public String nombre() { return "Auditoria"; }
    @Override public String icono() { return "🛡"; }
    @Override public int orden() { return 80; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR, Rol.AUDITOR);
    }

    @Override
    public Node crear() {
        desde = new DatePicker(LocalDate.now().minusDays(7));
        hasta = new DatePicker(LocalDate.now());
        Button cargar = new Button("Consultar");
        cargar.getStyleClass().add("btn-primary");
        cargar.setOnAction(e -> recargar());

        TableView<Auditoria> tabla = new TableView<>(data);
        tabla.getColumns().addAll(
                UiTables.col("Fecha/Hora", a -> a.getFechaHora() == null ? "" : a.getFechaHora().format(FH)),
                UiTables.col("Usuario", Auditoria::getUsuario),
                UiTables.col("Accion", Auditoria::getAccion),
                UiTables.col("Entidad", Auditoria::getEntidad),
                UiTables.col("Id", Auditoria::getEntidadId),
                UiTables.col("Resultado", Auditoria::getResultado),
                UiTables.col("Detalle", Auditoria::getDetalle));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        recargar();
        return new VBox(10, new HBox(8, new Label("Desde:"), desde,
                new Label("Hasta:"), hasta, cargar), tabla);
    }

    private void recargar() {
        var page = service.consultar(
                desde.getValue().atStartOfDay(),
                hasta.getValue().atTime(23, 59, 59),
                PageRequest.of(0, 500));
        data.setAll(page.getContent());
    }
}
