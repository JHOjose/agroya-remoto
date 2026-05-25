package com.agroya.service;

import java.io.InputStream;
import java.util.Map;

public interface ReportService {
    byte[] generateProductInventoryReport() throws Exception;
}
