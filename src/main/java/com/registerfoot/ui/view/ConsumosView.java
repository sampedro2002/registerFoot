package com.registerfoot.ui.view;

import com.registerfoot.biometric.BiometricProvider;
import com.registerfoot.biometric.MockBiometricProvider;
import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.ConsumoResultadoDTO;
import com.registerfoot.service.ConsumoService;
import com.registerfoot.service.TicketService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Punto de consumo. Demuestra el flujo biometrico completo: se suscribe al
 * {@link BiometricProvider} y, ante cada evento, invoca {@link ConsumoService}
 * para validar, registrar, generar e imprimir el ticket. Con el provider MOCK
 * se puede simular un lector escribiendo el codigo o usando los botones rapidos.
 */
@Component
public class ConsumosView implements ModuleView {

    private final BiometricProvider biometric;
    private final ConsumoService consumoService;
    private final TicketService ticketService;

    private final AtomicBoolean suscrito = new AtomicBoolean(false);
    private Label resultado;
    private TextArea preview;

    public ConsumosView(BiometricProvider biometric, ConsumoService consumoService,
                        TicketService ticketService) {
        this.biometric = biometric;
        this.consumoService = consumoService;
        this.ticketService = ticketService;
    }

    @Override public String nombre() { return "Consumos"; }
    @Override public String icono() { return "🍽"; }
    @Override public int orden() { return 50; }
    @Override public Set<Rol> rolesPermitidos() {
        return Set.of(Rol.ADMINISTRADOR, Rol.SUPERVISOR, Rol.OPERADOR);
    }

    @Override
    public Node crear() {
        // Suscripcion unica al lector: cada evento dispara el flujo de consumo.
        if (suscrito.compareAndSet(false, true)) {
            biometric.suscribir(event ->
                    procesar(event.codigoBiometrico(), "BIOMETRICO"));
        }

        TextField codigo = new TextField();
        codigo.setPromptText("Codigo biometrico (ej: BIO-1001)");
        Button identificar = new Button("Identificar");
        identificar.getStyleClass().add("btn-primary");
        identificar.setOnAction(e -> simular(codigo.getText()));
        codigo.setOnAction(e -> simular(codigo.getText()));

        HBox quick = new HBox(8);
        for (String c : new String[]{"BIO-1001", "BIO-1002", "BIO-1003", "BIO-1004", "BIO-1005"}) {
            Button b = new Button(c);
            b.setOnAction(e -> simular(c));
            quick.getChildren().add(b);
        }

        HBox entrada = new HBox(8, codigo, identificar);
        HBox.setHgrow(codigo, Priority.ALWAYS);

        resultado = new Label("Esperando identificacion...");
        preview = new TextArea();
        preview.setEditable(false);
        preview.getStyleClass().add("ticket-preview");
        preview.setPrefRowCount(18);

        Label info = new Label("Lector activo: " + biometric.marca()
                + (biometric.estaActivo() ? " (conectado)" : " (sin conexion)"));
        info.setStyle("-fx-text-fill:#64748b;");

        VBox box = new VBox(12, info, entrada, new Label("Empleados de prueba:"),
                quick, new Separator(), resultado, preview);
        box.setPadding(new Insets(4));
        VBox.setVgrow(preview, Priority.ALWAYS);
        return box;
    }

    /** Simula el lector: si es MOCK usa el provider; si no, procesa directo. */
    private void simular(String codigo) {
        if (codigo == null || codigo.isBlank()) return;
        if (biometric instanceof MockBiometricProvider mock) {
            mock.simular(codigo.trim());      // emite evento -> listener -> procesar
        } else {
            procesar(codigo.trim(), "MANUAL");
        }
    }

    private void procesar(String codigo, String origen) {
        // El flujo de negocio corre fuera del hilo de UI.
        ConsumoResultadoDTO r = consumoService.procesar(codigo, origen);
        Platform.runLater(() -> mostrar(r));
    }

    private void mostrar(ConsumoResultadoDTO r) {
        if (r.aprobado()) {
            resultado.setText("✔ APROBADO - " + r.empleadoNombre()
                    + " - Ticket " + r.ticket().numero());
            resultado.getStyleClass().setAll("consume-ok");
            preview.setText(ticketService.previsualizar(r.ticket().numero()));
        } else {
            resultado.setText("X RECHAZADO - " + r.motivo());
            resultado.getStyleClass().setAll("consume-bad");
            preview.clear();
        }
    }
}
