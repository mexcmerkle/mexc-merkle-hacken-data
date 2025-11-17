package com.mxc.merkle.data.runner;

import com.mxc.merkle.data.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Export command line runner
 */
@Slf4j
@Component
public class ExportCommandLineRunner implements CommandLineRunner {
    
    @Autowired
    private ExportService exportService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== MXC Merkle Data Export Tool ===");
        
        // Parse command line arguments
        LocalDateTime snapshotDate = parseSnapshotDate(args);
        
        if (snapshotDate != null) {
            log.info("Starting data export task for specified snapshot date: {}", snapshotDate);
        } else {
            log.info("Starting full data export task...");
        }
        
        try {
            if (snapshotDate != null) {
                exportService.exportMerkleDataBySnapshotDate(snapshotDate);
                log.info("Data export task completed successfully!");
            } else {
                log.error("Data export task completed failed! snapshotDate is null");
            }
        } catch (Exception e) {
            log.error("Data export task failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Parse snapshot date parameter
     * @param args Command line arguments
     * @return Parsed snapshot date, return null if not specified or parsing failed
     */
    private LocalDateTime parseSnapshotDate(String... args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // Find --snapshot-date parameter
        for (int i = 0; i < args.length; i++) {
            if ("--snapshot-date".equals(args[i]) && i + 1 < args.length) {
                String dateStr = args[i + 1];
                if (StringUtils.hasText(dateStr)) {
                    return parseDateTime(dateStr);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Parse date time string
     * @param dateStr Date string
     * @return Parsed LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateStr) {
        try {
            // Try to parse full date time format yyyy-MM-dd HH:mm:ss
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Try to parse date format yyyy-MM-dd, time defaults to 00:00:00
                return LocalDateTime.parse(dateStr + " 00:00:00", DATE_FORMATTER);
            } catch (DateTimeParseException e2) {
                log.error("Unable to parse snapshot date parameter: {}, supported formats: yyyy-MM-dd or yyyy-MM-dd HH:mm:ss", dateStr);
                return null;
            }
        }
    }
}
