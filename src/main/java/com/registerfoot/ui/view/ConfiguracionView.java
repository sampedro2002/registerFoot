package com.registerfoot.ui.view;

import com.registerfoot.config.AppProperties;
import com.registerfoot.domain.entity.DispositivoBiometrico;
import com.registerfoot.domain.entity.Impresora;
import com.registerfoot.domain.enums.Rol;
import com.registerfoot.dto.HorarioComidaDTO;
import com.registerfoot.repository.DispositivoBiometricoRepository;
import com.registerfoot.repository.ImpresoraRepository;
import com.registerfoot.service.HorarioComidaService;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Configuracion del sistema: parametros activos, impresoras, dispositivos
 * biometricos y horarios de comida. Solo Administrador.
 */
@Component
public class ConfiguracionView implements ModuleView {

    private final AppProperties props;
    private final ImpresoraRepository impresoraRepo;
    private final DispositivoBiometricoRepository dispositivoRepo;
    private final HorarioComidaService horarioService;

    public ConfiguracionView(AppProperties props, ImpresoraRepository impresoraRepo,
                             DispositivoBiometricoRepository dispositivoRepo,
                             HorarioComidaService horarioService) {
        this.props = props;
        this.impresoraRepo = impresoraRepo;
        this.dispositivoRepo = dispositivoRepo;
        this.horarioService = horarioService;
    }

    @Override public String nombre() { return "Configuracion"; }
    @Override public String icono() { return "⚙"; }
    @Override public int orden() { return 90; }
    @Override public Set<Rol> rolesPermitidos() { return Set.of(Rol.ADMINISTRADOR); }

    @Override
    public Node crear() {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(tabParametros(), tabImpresoras(), tabDispositivos(), tabHorarios());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabs;
    }

    private Tab tabParametros() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(8);
        int r = 0;
        g.addRow(r++, new Label("Proveedor biometrico:"), new Label(props.getBiometric().getProvider()));
        g.addRow(r++, new Label("Dispositivo (IP:puerto):"),
                new Label(props.getBiometric().getDeviceIp() + ":" + props.getBiometric().getDevicePort()));
        g.addRow(r++, new Label("Backend de impresion:"), new Label(props.getPrinting().getBackend()));
        g.addRow(r++, new Label("Impresora:"),
                new Label(props.getPrinting().getPrinterName().isBlank()
                        ? "(por defecto del SO)" : props.getPrinting().getPrinterName()));
        g.addRow(r++, new Label("Caracteres por linea:"),
                new Label(String.valueOf(props.getPrinting().getCharPerLine())));
        g.addRow(r++, new Label("Empresa:"), new Label(props.getPrinting().getCompanyName()));
        g.addRow(r, new Label("Maximo intentos login:"),
                new Label(String.valueOf(props.getSecurity().getMaxLoginAttempts())));
        Label nota = new Label("""
                Estos parametros se editan en application.yml (bloque registerfoot.*).
                Cambie 'provider' a ZKTECO / SUPREMA / ANVIZ / HIKVISION y 'backend' a
                ESC_POS / JAVA_PRINT para usar hardware real.""");
        nota.setStyle("-fx-text-fill:#64748b;");
        Tab t = new Tab("Parametros", new VBox(14, g, nota));
        return t;
    }

    private Tab tabImpresoras() {
        TableView<Impresora> tabla = new TableView<>(
                FXCollections.observableArrayList(impresoraRepo.findAll()));
        tabla.getColumns().addAll(
                UiTables.col("Nombre", Impresora::getNombre),
                UiTables.col("Tipo", Impresora::getTipo),
                UiTables.col("Destino", Impresora::getDestino),
                UiTables.col("Char/linea", i -> String.valueOf(i.getCharPorLinea())),
                UiTables.col("Defecto", i -> i.isPorDefecto() ? "Si" : "No"),
                UiTables.col("Activo", i -> i.isActivo() ? "Si" : "No"));
        return new Tab("Impresoras", tabla);
    }

    private Tab tabDispositivos() {
        TableView<DispositivoBiometrico> tabla = new TableView<>(
                FXCollections.observableArrayList(dispositivoRepo.findAll()));
        tabla.getColumns().addAll(
                UiTables.col("Nombre", DispositivoBiometrico::getNombre),
                UiTables.col("Marca", DispositivoBiometrico::getMarca),
                UiTables.col("IP", DispositivoBiometrico::getIp),
                UiTables.col("Puerto", d -> d.getPuerto() == null ? "" : String.valueOf(d.getPuerto())),
                UiTables.col("Activo", d -> d.isActivo() ? "Si" : "No"));
        return new Tab("Biometricos", tabla);
    }

    private Tab tabHorarios() {
        TableView<HorarioComidaDTO> tabla = new TableView<>(
                FXCollections.observableArrayList(horarioService.listar()));
        tabla.getColumns().addAll(
                UiTables.col("Tipo Comida", HorarioComidaDTO::tipoComidaNombre),
                UiTables.col("Inicio", h -> h.horaInicio().toString()),
                UiTables.col("Fin", h -> h.horaFin().toString()),
                UiTables.col("Activo", h -> h.activo() ? "Si" : "No"));
        return new Tab("Horarios", tabla);
    }
}
