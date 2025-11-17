package com.mxc.merkle.data.service.impl;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxc.merkle.data.entity.ExportData;
import com.mxc.merkle.data.entity.FinMerkleTreeLeafData;
import com.mxc.merkle.data.mapper.FinMerkleTreeLeafDataMapper;
import com.mxc.merkle.data.service.ExportService;
import com.mxc.merkle.data.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Export service implementation class
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {
    
    @Autowired
    private FinMerkleTreeLeafDataMapper merkleDataMapper;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${export.batch-size:1000}")
    private Integer batchSize;
    
    @Value("${export.output-dir:./exports}")
    private String outputDir;
    
    @Value("${export.file-prefix:mexc_merkle_data}")
    private String filePrefix;
    
    private static final String[] CURRENCY_PREFIXES = {"USDT:", "USDC:", "BTC:", "ETH:"};

    @Override
    public void exportMerkleDataBySnapshotDate(LocalDateTime snapshotDate) throws Exception {
        log.info("Starting to export Merkle data for snapshot date: {}", snapshotDate);
        exportMerkleDataInternal(snapshotDate);
    }
    
    /**
     * Internal export method
     * @param snapshotDate Snapshot date, export all data when null
     * @throws Exception Export exception
     */
    private void exportMerkleDataInternal(LocalDateTime snapshotDate) throws Exception {
        // Create export directory
        File exportDir = new File(outputDir);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        // Generate file name
        String snapshotStr = snapshotDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = filePrefix + "_" +snapshotStr + ".csv";
        String filePath = outputDir + File.separator + fileName;
        
        // Query total record count
        Long totalCount;
        totalCount = merkleDataMapper.countBySnapshotDate(snapshotDate);
        log.info("Total records for snapshot date {}: {}", snapshotDate, totalCount);

        if (totalCount == 0) {
            log.warn("No data to export");
            return;
        }
        
        // Batch export
        long processedCount = 0;
        
        // Create EasyExcel writer
        try (com.alibaba.excel.ExcelWriter excelWriter = EasyExcel.write(filePath, ExportData.class).build()) {
            com.alibaba.excel.write.metadata.WriteSheet writeSheet = EasyExcel.writerSheet("MerkleData").build();
            
            if (snapshotDate != null) {
                // Use ID range-based query to optimize performance
                processedCount = exportByIdRange(snapshotDate, excelWriter, writeSheet, totalCount);
            } else {
                // Full export uses traditional pagination
                processedCount = exportByOffset(excelWriter, writeSheet, totalCount);
            }
        }
        
        // Calculate file MD5
        File exportFile = new File(filePath);
        String md5 = MD5Util.calculateFileMD5(exportFile);
        
        log.info("Export completed!");
        log.info("File path: {}", filePath);
        log.info("Total records: {}", processedCount);
        log.info("File size: {} bytes", exportFile.length());
        log.info("File MD5: {}", md5);
    }
    
    /**
     * ID range-based export (optimize snapshot date query performance)
     * @param snapshotDate Snapshot date
     * @param excelWriter Excel writer
     * @param writeSheet Write sheet
     * @param totalCount Total record count
     * @return Processed record count
     */
    private long exportByIdRange(LocalDateTime snapshotDate, 
                                com.alibaba.excel.ExcelWriter excelWriter,
                                com.alibaba.excel.write.metadata.WriteSheet writeSheet,
                                Long totalCount) {
        
        // Query ID range
        Long minId = merkleDataMapper.findMinIdBySnapshotDate(snapshotDate);
        Long maxId = merkleDataMapper.findMaxIdBySnapshotDate(snapshotDate);
        
        if (minId == null || maxId == null) {
            log.warn("Unable to get ID range for snapshot date: {}", snapshotDate);
            return 0;
        }
        
        log.info("ID range for snapshot date {}: {} - {}", snapshotDate, minId, maxId);
        
        long processedCount = 0;
        Long currentMinId = minId;
        
        while (currentMinId <= maxId) {
            // Calculate current batch max ID
            Long currentMaxId = Math.min(currentMinId + batchSize - 1, maxId);
            
            // Query data based on ID range
            List<FinMerkleTreeLeafData> dataList = merkleDataMapper.selectBySnapshotDateAndIdRange(
                    snapshotDate, currentMinId, currentMaxId, batchSize);
            
            if (dataList.isEmpty()) {
                // If current range has no data, skip to next batch
                currentMinId = currentMaxId + 1;
                continue;
            }
            
            // Convert data
            List<ExportData> exportDataList = new ArrayList<>();
            for (FinMerkleTreeLeafData data : dataList) {
                ExportData exportData = convertToExportData(data);
                if (exportData != null) {
                    exportDataList.add(exportData);
                }
            }
            
            // Write data
            if (!exportDataList.isEmpty()) {
                excelWriter.write(exportDataList, writeSheet);
            }
            
            processedCount += dataList.size();
            
            // Update next batch start ID
            Long lastId = dataList.get(dataList.size() - 1).getId();
            currentMinId = lastId + 1;
            if (processedCount % 100000 == 0) {
                log.info("Processed {} / {} records (ID range: {} - {})",
                        processedCount, totalCount,
                        dataList.get(0).getId(), lastId);
            }
        }
        
        return processedCount;
    }
    
    /**
     * Offset-based export (full export)
     * @param excelWriter Excel writer
     * @param writeSheet Write sheet
     * @param totalCount Total record count
     * @return Processed record count
     */
    private long exportByOffset(com.alibaba.excel.ExcelWriter excelWriter,
                               com.alibaba.excel.write.metadata.WriteSheet writeSheet,
                               Long totalCount) {
        
        long processedCount = 0;
        long offset = 0;
        
        while (offset < totalCount) {
            // Query data with pagination
            List<FinMerkleTreeLeafData> dataList = merkleDataMapper.selectByPage(offset, batchSize);
            
            if (dataList.isEmpty()) {
                break;
            }
            
            // Convert data
            List<ExportData> exportDataList = new ArrayList<>();
            for (FinMerkleTreeLeafData data : dataList) {
                ExportData exportData = convertToExportData(data);
                if (exportData != null) {
                    exportDataList.add(exportData);
                }
            }
            
            // Write data
            if (!exportDataList.isEmpty()) {
                excelWriter.write(exportDataList, writeSheet);
            }
            
            processedCount += dataList.size();
            offset += batchSize;
            
            log.info("Processed {} / {} records", processedCount, totalCount);
        }
        
        return processedCount;
    }
    
    /**
     * Convert database record to export data
     * @param data Database record
     * @return Export data
     */
    private ExportData convertToExportData(FinMerkleTreeLeafData data) {
        try {
            // Parse balance_data JSON
            Map<String, BigDecimal> balanceMap = parseBalanceData(data.getBalanceData());
            
            // Aggregate amounts by currency
            BigDecimal usdtTotal = BigDecimal.ZERO;
            BigDecimal usdcTotal = BigDecimal.ZERO;
            BigDecimal btcTotal = BigDecimal.ZERO;
            BigDecimal ethTotal = BigDecimal.ZERO;
            
            for (Map.Entry<String, BigDecimal> entry : balanceMap.entrySet()) {
                String key = entry.getKey();
                BigDecimal value = entry.getValue();
                
                if (value == null) {
                    continue;
                }
                
                if (key.startsWith("USDT:")) {
                    usdtTotal = usdtTotal.add(value);
                } else if (key.startsWith("USDC:")) {
                    usdcTotal = usdcTotal.add(value);
                } else if (key.startsWith("BTC:")) {
                    btcTotal = btcTotal.add(value);
                } else if (key.startsWith("ETH:")) {
                    ethTotal = ethTotal.add(value);
                }
            }
            
            return ExportData.builder()
                    .memberId(MD5Util.calculateValueMD5(data.getMemberId()))
                    .usdt(usdtTotal)
                    .usdc(usdcTotal)
                    .btc(btcTotal)
                    .eth(ethTotal)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to convert data, memberId: {}, error: {}", data.getMemberId(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Parse balance_data JSON string
     * @param balanceDataJson JSON string
     * @return Parsed Map
     */
    private Map<String, BigDecimal> parseBalanceData(String balanceDataJson) {
        if (!StringUtils.hasText(balanceDataJson)) {
            return new HashMap<>();
        }
        
        try {
            TypeReference<Map<String, BigDecimal>> typeRef = new TypeReference<Map<String, BigDecimal>>() {};
            Map<String, BigDecimal> result = objectMapper.readValue(balanceDataJson, typeRef);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            log.error("Failed to parse balance_data: {}", balanceDataJson, e);
            return new HashMap<>();
        }
    }
}
