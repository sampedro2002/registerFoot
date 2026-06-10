package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.TicketDTO;
import com.registerfoot.service.TicketService;
import com.registerfoot.ui.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Listado de tickets del dia con previsualizacion y reimpresion. */
@Component
public class TicketsView implements ModuleView {

    private static final DateTimeFormatter FH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TicketService ticketService;
    private final ViewManager view;

    private final ObservableList<TicketDTO> data = FXCollections.observableArrayList();
    private TableView<TicketDTO> tabla;
    private TextArea preview;
    private DatePicker fecha;

    public TicketsView(TicketService ticketService, ViewManager view) {
        this.ticketService = ticketService;
        this.view = view;
    }

    @Override public String nombre() { return "Tickets"; }
    @Override public String icono() { return "🎫"; }
    @Override public int orden() { return 60; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR, Rol.OPERADOR, Rol.AUDITOR);
    }

    @Override
    public Node crear() {
        fecha = new DatePicker(LocalDate.now());
        Button cargar = new Button("Cargar");
        cargar.setOnAction(e -> recargar());
        Button reimprimir = new Button("Reimprimir");
        reimprimir.getStyleClass().add("btn-primary");
        reimprimir.setOnAction(e -> reimprimir());

        tabla = new TableView<>(data);
        tabla.getColumns().addAll(
                UiTables.col("Numero", TicketDTO::numero),
                UiTables.col("Fecha/Hora", t -> t.fechaHora().format(FH)),
                UiTables.col("Empleado", TicketDTO::empleadoNombre),
                UiTables.col("Comida", TicketDTO::tipoComida),
                UiTables.col("Valor", t -> t.valor().toPlainString()),
                UiTables.col("Estado", TicketDTO::estado),
                UiTables.col("Reimpr.", t -> String.valueOf(t.reimpresiones())));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) preview.setText(ticketService.previsualizar(b.numero()));
        });
        VBox.setVgrow(tabla, Priority.ALWAYS);

        preview = new TextArea();
        preview.setEditable(false);
        preview.getStyleClass().add("ticket-preview");
        preview.setPrefRowCount(14);

        recargar();
        HBox barra = new HBox(8, new Label("Fecha:"), fecha, cargar, reimprimir);
        SplitPane split = new SplitPane(tabla, preview);
        split.setDividerPositions(0.62);
        VBox.setVgrow(split, Priority.ALWAYS);
        return new VBox(10, barra, split);
    }

    private void recargar() {
        data.setAll(ticketService.listarDelDia(fecha.getValue()));
    }

    private void reimprimir() {
        TicketDTO sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) { view.warn("Tickets", "Seleccione un ticket."); return; }
        try {
            boolean ok = ticketService.reimprimir(sel.numero());
            recargar();
            if (ok) view.info("Tickets", "Ticket reenviado a la impresora.");
            else view.warn("Tickets", "Reimpresion registrada pero la impresora fallo.");
        } catch (Exception ex) { view.error("Error", ex.getMessage()); }
    }
}
