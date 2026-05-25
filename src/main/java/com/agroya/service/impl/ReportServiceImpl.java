package com.agroya.service.impl;

import com.agroya.model.Product;
import com.agroya.repository.ProductRepository;
import com.agroya.service.ReportService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ProductRepository productRepository;

    @Override
    public byte[] generateProductInventoryReport() throws Exception {
        List<Product> products = productRepository.findAll();
        
        List<Map<String, Object>> data = products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("nombre", p.getNombre());
            map.put("categoriaNombre", p.getCategoria() != null ? p.getCategoria().getName() : "N/A");
            map.put("precio", p.getPrecio());
            map.put("stock", p.getStock());
            map.put("unidad", p.getUnidad());
            return map;
        }).collect(Collectors.toList());

        InputStream reportStream = getClass().getResourceAsStream("/reports/inventory_report.jrxml");
        if (reportStream == null) {
            throw new RuntimeException("No se pudo encontrar el archivo del reporte");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "AgroYa System");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
