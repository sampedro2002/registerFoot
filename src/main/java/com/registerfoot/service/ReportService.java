package com.registerfoot.service;

import com.registerfoot.domain.entity.RegistroAlimentacion;
import com.registerfoot.dto.ReporteConsumoRow;
import com.registerfoot.exception.RegisterFootException;
import com.registerfoot.repository.RegistroAlimentacionRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera reportes de consumos en PDF (JasperReports) y Excel (Apache POI).
 * El template Jasper se compila en caliente desde classpath:reports.
 */
@Service
public class ReportService {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter H = DateTimeFormatter.ofPattern("HH:mm");

    private final RegistroAlimentacionRepository registroRepo;
    private final AuditoriaService auditoria;
    private JasperReport compilado; // cache del template compilado

    public ReportService(RegistroAlimentacionRepository registroRepo, AuditoriaService auditoria) {
        this.registroRepo = registroRepo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<ReporteConsumoRow> filas(LocalDate desde, LocalDate hasta) {
        return registroRepo.findByFechaBetween(desde, hasta).stream()
                .map(this::toRow).toList();
    }

    /** Reporte PDF de consumos en el rango dado. */
    public File generarPdf(LocalDate desde, LocalDate hasta, File destino) {
        List<ReporteConsumoRow> data = filas(desde, hasta);
        try {
            JasperReport report = template();
            Map<String, Object> params = new HashMap<>();
            params.put("DESDE", desde.format(F));
            params.put("HASTA", hasta.format(F));
            params.put("TOTAL", total(data));
            JasperPrint print = JasperFillManager.fillReport(
                    report, params, new JRBeanCollectionDataSource(data));
            JasperExportManager.exportReportToPdfFile(print, destino.getAbsolutePath());
            auditoria.ok("REPORTE", "Consumos", "PDF", destino.getName());
            return destino;
        } catch (JRException e) {
            throw new RegisterFootException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    /** Reporte Excel de consumos en el rango dado. */
    public File generarExcel(LocalDate desde, LocalDate hasta, File destino) {
        List<ReporteConsumoRow> data = filas(desde, hasta);
        try (Workbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(destino)) {
            Sheet sheet = wb.createSheet("Consumos");
            CellStyle header = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            header.setFont(bold);

            String[] cols = {"No. Ticket", "Fecha", "Hora", "Empleado", "Documento",
                    "Tipo Comida", "Concesion", "Valor"};
            Row h = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(header);
            }

            int r = 1;
            for (ReporteConsumoRow row : data) {
                Row fila = sheet.createRow(r++);
                fila.createCell(0).setCellValue(nv(row.getNumero()));
                fila.createCell(1).setCellValue(nv(row.getFecha()));
                fila.createCell(2).setCellValue(nv(row.getHora()));
                fila.createCell(3).setCellValue(nv(row.getEmpleado()));
                fila.createCell(4).setCellValue(nv(row.getDocumento()));
                fila.createCell(5).setCellValue(nv(row.getTipoComida()));
                fila.createCell(6).setCellValue(nv(row.getConcesion()));
                fila.createCell(7).setCellValue(row.getValor() == null ? 0 : row.getValor().doubleValue());
            }
            Row totalRow = sheet.createRow(r);
            totalRow.createCell(6).setCellValue("TOTAL");
            totalRow.createCell(7).setCellValue(total(data).doubleValue());

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            auditoria.ok("REPORTE", "Consumos", "EXCEL", destino.getName());
            return destino;
        } catch (Exception e) {
            throw new RegisterFootException("Error generando Excel: " + e.getMessage(), e);
        }
    }

    private synchronized JasperReport template() throws JRException {
        if (compilado == null) {
            try (InputStream is = new ClassPathResource("reports/consumos.jrxml").getInputStream()) {
                compilado = JasperCompileManager.compileReport(is);
            } catch (Exception e) {
                throw new JRException("No se pudo compilar la plantilla: " + e.getMessage(), e);
            }
        }
        return compilado;
    }

    private BigDecimal total(List<ReporteConsumoRow> data) {
        return data.stream().map(ReporteConsumoRow::getValor)
                .filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String nv(String s) { return s == null ? "" : s; }

    private ReporteConsumoRow toRow(RegistroAlimentacion r) {
        return new ReporteConsumoRow(
                "REG-" + r.getId(),
                r.getFecha().format(F),
                r.getHora().format(H),
                r.getEmpleado().getNombreCompleto(),
                r.getEmpleado().getDocumento(),
                r.getTipoComida().getNombre(),
                r.getConcesion().getNombre(),
                r.getValor());
    }
}
