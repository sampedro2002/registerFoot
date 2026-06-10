package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.DashboardStatsDTO;
import com.registerfoot.service.DashboardService;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;

@Component
public class DashboardView implements ModuleView {

    private final DashboardService dashboardService;

    public DashboardView(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override public String nombre() { return "Dashboard"; }
    @Override public String icono() { return "▤"; }
    @Override public int orden() { return 10; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR, Rol.OPERADOR, Rol.AUDITOR);
    }

    @Override
    public Node crear() {
        DashboardStatsDTO s = dashboardService.estadisticas();
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        HBox kpis = new HBox(16,
                kpi("Consumos hoy", String.valueOf(s.consumosHoy())),
                kpi("Tickets hoy", String.valueOf(s.ticketsHoy())),
                kpi("Valor hoy", money.format(s.valorHoy())),
                kpi("Empleados activos", String.valueOf(s.empleadosActivos())));

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Consumos ultimos 7 dias");
        chart.setLegendVisible(false);
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        s.ultimosDias().forEach(d -> serie.getData().add(new XYChart.Data<>(d.fecha(), d.consumos())));
        chart.getData().add(serie);
        chart.setPrefHeight(320);

        VBox box = new VBox(18, kpis, chart);
        return box;
    }

    private Node kpi(String label, String value) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");
        card.setPadding(new Insets(18));
        HBox.setHgrow(card, Priority.ALWAYS);
        Label v = new Label(value);
        v.getStyleClass().add("kpi-value");
        Label l = new Label(label);
        l.getStyleClass().add("kpi-label");
        card.getChildren().addAll(v, l);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }
}
