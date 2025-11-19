package com.mexc.merkle.data.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.mexc.merkle.data.entity.ExportData;
import com.mexc.merkle.data.entity.FilePartInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * File part manager for handling file splitting
 */
@Slf4j
public class FilePartManager {
    private int currentPartNumber = 1;
    private long currentFileRowCount = 0;
    private ExcelWriter currentWriter;
    private WriteSheet currentSheet;
    private final List<FilePartInfo> partFiles = new ArrayList<>();
    private final String baseFileName;
    private final String snapshotStr;
    private final String outputDir;
    private final Long maxRowsPerFile;
    
    public FilePartManager(String baseFileName, String snapshotStr, String outputDir, Long maxRowsPerFile) {
        this.baseFileName = baseFileName;
        this.snapshotStr = snapshotStr;
        this.outputDir = outputDir;
        this.maxRowsPerFile = maxRowsPerFile;
    }
    
    public void initializeFirstFile() throws Exception {
        createNewFile();
    }
    
    public boolean needNewFile(int newRowsCount) {
        return currentFileRowCount + newRowsCount > maxRowsPerFile;
    }
    
    public void writeData(List<ExportData> exportDataList) {
        if (!exportDataList.isEmpty() && currentWriter != null) {
            currentWriter.write(exportDataList, currentSheet);
            currentFileRowCount += exportDataList.size();
        }
    }
    
    public void switchToNewFile() throws Exception {
        closeCurrentFile();
        currentPartNumber++;
        createNewFile();
    }
    
    private void createNewFile() throws Exception {
        String fileName = String.format("%s_%s_part%03d.csv", baseFileName, snapshotStr, currentPartNumber);
        String filePath = outputDir + File.separator + fileName;
        
        currentWriter = EasyExcel.write(filePath, ExportData.class).build();
        currentSheet = EasyExcel.writerSheet("MerkleData").build();
        currentFileRowCount = 0;
        
        log.info("Created new file part {}: {}", currentPartNumber, fileName);
    }
    
    private void closeCurrentFile() throws Exception {
        if (currentWriter != null) {
            currentWriter.close();
            
            // Calculate MD5 for the completed file
            String fileName = String.format("%s_%s_part%03d.csv", baseFileName, snapshotStr, currentPartNumber);
            String filePath = outputDir + File.separator + fileName;
            File file = new File(filePath);
            
            if (file.exists()) {
                String md5 = MD5Util.calculateFileMD5(file);
                FilePartInfo partInfo = new FilePartInfo(fileName, filePath, currentFileRowCount, file.length(), md5);
                partFiles.add(partInfo);
                
                log.info("Completed file part {}: {} rows, {} bytes, MD5: {}", 
                        currentPartNumber, currentFileRowCount, file.length(), md5);
            }
            
            currentWriter = null;
            currentSheet = null;
        }
    }
    
    public void closeAll() throws Exception {
        closeCurrentFile();
    }
    
    public void printSummary(long totalProcessedCount, BigDecimal totalUsdt, BigDecimal totalUsdc, 
                            BigDecimal totalBtc, BigDecimal totalEth) {
        log.info("=== EXPORT SUMMARY ===");
        log.info("Snapshot Date: {}", snapshotStr);
        log.info("Total Records: {}", totalProcessedCount);
        log.info("Total Files: {}", partFiles.size());
        log.info("Files Generated:");
        
        long totalSize = 0;
        for (FilePartInfo partInfo : partFiles) {
            totalSize += partInfo.getFileSize();
            log.info("  - {} ({} rows, {} bytes, MD5: {})", 
                    partInfo.getFileName(), 
                    partInfo.getRowCount(), 
                    partInfo.getFileSize(), 
                    partInfo.getMd5());
        }
        
        log.info("Total Size: {} bytes ({} MB)", totalSize, totalSize / 1024 / 1024);
        log.info("=== TOTAL AMOUNTS FOR ALL USERS ===");
        log.info("USDT: {}", totalUsdt);
        log.info("USDC: {}", totalUsdc);
        log.info("BTC: {}", totalBtc);
        log.info("ETH: {}", totalEth);
        log.info("==========================================");
    }
    
    public List<FilePartInfo> getPartFiles() {
        return new ArrayList<>(partFiles);
    }
}
