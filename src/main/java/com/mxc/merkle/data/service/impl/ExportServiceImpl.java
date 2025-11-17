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
 * 导出服务实现类
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
    
    @Value("${export.file-prefix:merkle_data}")
    private String filePrefix;
    
    private static final String[] CURRENCY_PREFIXES = {"USDT:", "USDC:", "BTC:", "ETH:"};
    
    @Override
    public void exportMerkleData() throws Exception {
        log.info("开始导出所有Merkle数据...");
        exportMerkleDataInternal(null);
    }
    
    @Override
    public void exportMerkleDataBySnapshotDate(LocalDateTime snapshotDate) throws Exception {
        log.info("开始导出快照日期 {} 的Merkle数据...", snapshotDate);
        exportMerkleDataInternal(snapshotDate);
    }
    
    /**
     * 内部导出方法
     * @param snapshotDate 快照日期，为null时导出所有数据
     * @throws Exception 导出异常
     */
    private void exportMerkleDataInternal(LocalDateTime snapshotDate) throws Exception {
        // 创建导出目录
        File exportDir = new File(outputDir);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        // 生成文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = filePrefix + "_" + timestamp;
        if (snapshotDate != null) {
            String snapshotStr = snapshotDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            fileName += "_" + snapshotStr;
        }
        fileName += ".csv";
        String filePath = outputDir + File.separator + fileName;
        
        // 查询总记录数
        Long totalCount;
        if (snapshotDate != null) {
            totalCount = merkleDataMapper.countBySnapshotDate(snapshotDate);
            log.info("快照日期 {} 的总记录数: {}", snapshotDate, totalCount);
        } else {
            totalCount = merkleDataMapper.countAll();
            log.info("总记录数: {}", totalCount);
        }
        
        if (totalCount == 0) {
            log.warn("没有数据需要导出");
            return;
        }
        
        // 分批导出
        long processedCount = 0;
        
        // 创建EasyExcel写入器
        try (com.alibaba.excel.ExcelWriter excelWriter = EasyExcel.write(filePath, ExportData.class).build()) {
            com.alibaba.excel.write.metadata.WriteSheet writeSheet = EasyExcel.writerSheet("MerkleData").build();
            
            if (snapshotDate != null) {
                // 使用基于ID范围的查询优化性能
                processedCount = exportByIdRange(snapshotDate, excelWriter, writeSheet, totalCount);
            } else {
                // 全量导出使用传统分页
                processedCount = exportByOffset(excelWriter, writeSheet, totalCount);
            }
        }
        
        // 计算文件MD5
        File exportFile = new File(filePath);
        String md5 = MD5Util.calculateFileMD5(exportFile);
        
        log.info("导出完成！");
        log.info("文件路径: {}", filePath);
        log.info("总记录数: {}", processedCount);
        log.info("文件大小: {} bytes", exportFile.length());
        log.info("文件MD5: {}", md5);
    }
    
    /**
     * 基于ID范围的导出（优化快照日期查询性能）
     * @param snapshotDate 快照日期
     * @param excelWriter Excel写入器
     * @param writeSheet 写入Sheet
     * @param totalCount 总记录数
     * @return 已处理记录数
     */
    private long exportByIdRange(LocalDateTime snapshotDate, 
                                com.alibaba.excel.ExcelWriter excelWriter,
                                com.alibaba.excel.write.metadata.WriteSheet writeSheet,
                                Long totalCount) {
        
        // 查询ID范围
        Long minId = merkleDataMapper.findMinIdBySnapshotDate(snapshotDate);
        Long maxId = merkleDataMapper.findMaxIdBySnapshotDate(snapshotDate);
        
        if (minId == null || maxId == null) {
            log.warn("无法获取快照日期 {} 的ID范围", snapshotDate);
            return 0;
        }
        
        log.info("快照日期 {} 的ID范围: {} - {}", snapshotDate, minId, maxId);
        
        long processedCount = 0;
        Long currentMinId = minId;
        
        while (currentMinId <= maxId) {
            // 计算当前批次的最大ID
            Long currentMaxId = Math.min(currentMinId + batchSize - 1, maxId);
            
            // 基于ID范围查询数据
            List<FinMerkleTreeLeafData> dataList = merkleDataMapper.selectBySnapshotDateAndIdRange(
                    snapshotDate, currentMinId, currentMaxId, batchSize);
            
            if (dataList.isEmpty()) {
                // 如果当前范围没有数据，跳到下一个批次
                currentMinId = currentMaxId + 1;
                continue;
            }
            
            // 转换数据
            List<ExportData> exportDataList = new ArrayList<>();
            for (FinMerkleTreeLeafData data : dataList) {
                ExportData exportData = convertToExportData(data);
                if (exportData != null) {
                    exportDataList.add(exportData);
                }
            }
            
            // 写入数据
            if (!exportDataList.isEmpty()) {
                excelWriter.write(exportDataList, writeSheet);
            }
            
            processedCount += dataList.size();
            
            // 更新下一批次的起始ID
            Long lastId = dataList.get(dataList.size() - 1).getId();
            currentMinId = lastId + 1;
            if (processedCount % 100000 == 0) {
                log.info("已处理 {} / {} 条记录 (ID范围: {} - {})",
                        processedCount, totalCount,
                        dataList.get(0).getId(), lastId);
            }
        }
        
        return processedCount;
    }
    
    /**
     * 基于偏移量的导出（全量导出）
     * @param excelWriter Excel写入器
     * @param writeSheet 写入Sheet
     * @param totalCount 总记录数
     * @return 已处理记录数
     */
    private long exportByOffset(com.alibaba.excel.ExcelWriter excelWriter,
                               com.alibaba.excel.write.metadata.WriteSheet writeSheet,
                               Long totalCount) {
        
        long processedCount = 0;
        long offset = 0;
        
        while (offset < totalCount) {
            // 分页查询数据
            List<FinMerkleTreeLeafData> dataList = merkleDataMapper.selectByPage(offset, batchSize);
            
            if (dataList.isEmpty()) {
                break;
            }
            
            // 转换数据
            List<ExportData> exportDataList = new ArrayList<>();
            for (FinMerkleTreeLeafData data : dataList) {
                ExportData exportData = convertToExportData(data);
                if (exportData != null) {
                    exportDataList.add(exportData);
                }
            }
            
            // 写入数据
            if (!exportDataList.isEmpty()) {
                excelWriter.write(exportDataList, writeSheet);
            }
            
            processedCount += dataList.size();
            offset += batchSize;
            
            log.info("已处理 {} / {} 条记录", processedCount, totalCount);
        }
        
        return processedCount;
    }
    
    /**
     * 转换数据库记录为导出数据
     * @param data 数据库记录
     * @return 导出数据
     */
    private ExportData convertToExportData(FinMerkleTreeLeafData data) {
        try {
            // 解析balance_data JSON
            Map<String, BigDecimal> balanceMap = parseBalanceData(data.getBalanceData());
            
            // 聚合各币种金额
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
            log.error("转换数据失败, memberId: {}, error: {}", data.getMemberId(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 解析balance_data JSON字符串
     * @param balanceDataJson JSON字符串
     * @return 解析后的Map
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
            log.error("解析balance_data失败: {}", balanceDataJson, e);
            return new HashMap<>();
        }
    }
}
