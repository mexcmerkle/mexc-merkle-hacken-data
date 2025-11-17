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
 * 导出命令行运行器
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
        
        // 解析命令行参数
        LocalDateTime snapshotDate = parseSnapshotDate(args);
        
        if (snapshotDate != null) {
            log.info("开始执行指定快照日期的数据导出任务，快照日期: {}", snapshotDate);
        } else {
            log.info("开始执行全量数据导出任务...");
        }
        
        try {
            if (snapshotDate != null) {
                exportService.exportMerkleDataBySnapshotDate(snapshotDate);
            } else {
                exportService.exportMerkleData();
            }
            log.info("数据导出任务执行完成！");
        } catch (Exception e) {
            log.error("数据导出任务执行失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 解析快照日期参数
     * @param args 命令行参数
     * @return 解析后的快照日期，如果没有指定或解析失败则返回null
     */
    private LocalDateTime parseSnapshotDate(String... args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // 查找 --snapshot-date 参数
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
     * 解析日期时间字符串
     * @param dateStr 日期字符串
     * @return 解析后的LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateStr) {
        try {
            // 尝试解析完整的日期时间格式 yyyy-MM-dd HH:mm:ss
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // 尝试解析日期格式 yyyy-MM-dd，时间默认为 00:00:00
                return LocalDateTime.parse(dateStr + " 00:00:00", DATE_FORMATTER);
            } catch (DateTimeParseException e2) {
                log.error("无法解析快照日期参数: {}，支持的格式: yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss", dateStr);
                return null;
            }
        }
    }
}
