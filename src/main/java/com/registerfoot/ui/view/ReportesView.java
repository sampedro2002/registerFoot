package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.ReporteConsumoRow;
import com.registerfoot.service.ReportService;
import com.registerfoot.ui.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.util.Set;

/** Generacion y exportacion de reportes PDF (Jasper) y Excel (POI). */
@Component
public class ReportesView implements ModuleView {

    private final ReportService reportService;
    private final ViewManager view;

    private final ObservableList<ReporteConsumoRow> data = FXCollections.observableArrayList();
    private DatePicker desde, hasta;

    public ReportesView(ReportService reportService, ViewManager view) {
        this.reportService = reportService;
        this.view = view;
    }

    @Override public String nombre() { return "Reportes"; }
    @Override public String icono() { return "📊"; }
    @Override public int orden() { return 70; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR, Rol.AUDITOR);
    }

    @Override
    public Node crear() {
        desde = new DatePicker(LocalDate.now().withDayOfMonth(1));
        hasta = new DatePicker(LocalDate.now());

        Button previsualizar = new Button("Previsualizar");
        previsualizar.setOnAction(e -> data.setAll(reportService.filas(desde.getValue(), hasta.getValue())));
        Button pdf = new Button("Exportar PDF");
        pdf.getStyleClass().add("btn-primary");
        pdf.setOnAction(e -> exportar(true));
        Button excel = new Button("Exportar Excel");
        excel.setOnAction(e -> exportar(false));

        HBox barra = new HBox(8, new Label("Desde:"), desde, new Label("Hasta:"), hasta,
                previsualizar, pdf, excel);

        TableView<ReporteConsumoRow> tabla = new TableView<>(data);
        tabla.getColumns().addAll(
                UiTables.col("Ticket", ReporteConsumoRow::getNumero),
                UiTables.col("Fecha", ReporteConsumoRow::getFecha),
                UiTables.col("Hora", ReporteConsumoRow::getHora),
                UiTables.col("Empleado", ReporteConsumoRow::getEmpleado),
                UiTables.col("Documento", ReporteConsumoRow::getDocumento),
                UiTables.col("Comida", ReporteConsumoRow::getTipoComida),
                UiTables.col("Concesion", ReporteConsumoRow::getConcesion),
                UiTables.col("Valor", r -> r.getValor() == null ? "" : r.getValor().toPlainString()));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        return new VBox(10, barra, tabla);
    }

    private void exportar(boolean pdf) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("consumos." + (pdf ? "pdf" : "xlsx"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                pdf ? "PDF" : "Excel", pdf ? "*.pdf" : "*.xlsx"));
        File destino = fc.showSaveDialog(view.getStage());
        if (destino == null) return;
        try {
            if (pdf) reportService.generarPdf(desde.getValue(), hasta.getValue(), destino);
            else reportService.generarExcel(desde.getValue(), hasta.getValue(), destino);
            view.info("Reportes", "Reporte generado en:\n" + destino.getAbsolutePath());
        } catch (Exception ex) {
            view.error("Error", ex.getMessage());
        }
    }
}
