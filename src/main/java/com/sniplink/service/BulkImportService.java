package com.sniplink.service;

import com.sniplink.dto.ShortenRequest;
import com.sniplink.dto.URLResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class BulkImportService {

    @Autowired
    private URLService urlService;

    public ByteArrayOutputStream processBulkImport(MultipartFile file) throws IOException {
        List<BulkResult> results = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                String url = record.get("url");
                String customSlug = record.size() > 1 ? record.get(1) : "";
                
                BulkResult result = new BulkResult();
                result.setOriginalUrl(url);
                result.setCustomSlug(customSlug);

                try {
                    ShortenRequest request = new ShortenRequest();
                    request.setUrl(url);
                    if (customSlug != null && !customSlug.isEmpty()) {
                        request.setCustomSlug(customSlug);
                    }

                    URLResponse urlResponse = urlService.shortenURL(request);
                    result.setShortCode(urlResponse.getShortCode());
                    result.setShortUrl(urlResponse.getShortUrl());
                    result.setStatus("SUCCESS");
                    result.setMessage("URL shortened successfully");
                } catch (Exception e) {
                    result.setStatus("FAILED");
                    result.setMessage(e.getMessage());
                }

                results.add(result);
            }
        }

        return generateResultCSV(results);
    }

    private ByteArrayOutputStream generateResultCSV(List<BulkResult> results) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                     "Original URL", "Custom Slug", "Short Code", "Short URL", "Status", "Message"))) {

            for (BulkResult result : results) {
                csvPrinter.printRecord(
                        result.getOriginalUrl(),
                        result.getCustomSlug(),
                        result.getShortCode(),
                        result.getShortUrl(),
                        result.getStatus(),
                        result.getMessage()
                );
            }
            csvPrinter.flush();
        }
        return outputStream;
    }

    private static class BulkResult {
        private String originalUrl;
        private String customSlug;
        private String shortCode;
        private String shortUrl;
        private String status;
        private String message;

        public String getOriginalUrl() {
            return originalUrl;
        }

        public void setOriginalUrl(String originalUrl) {
            this.originalUrl = originalUrl;
        }

        public String getCustomSlug() {
            return customSlug;
        }

        public void setCustomSlug(String customSlug) {
            this.customSlug = customSlug;
        }

        public String getShortCode() {
            return shortCode;
        }

        public void setShortCode(String shortCode) {
            this.shortCode = shortCode;
        }

        public String getShortUrl() {
            return shortUrl;
        }

        public void setShortUrl(String shortUrl) {
            this.shortUrl = shortUrl;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
