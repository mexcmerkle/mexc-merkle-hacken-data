package com.mxc.merkle.data.service;

import java.time.LocalDateTime;

/**
 * 导出服务接口
 */
public interface ExportService {
    
    /**
     * 导出Merkle数据到CSV文件
     * @throws Exception 导出异常
     */
    void exportMerkleData() throws Exception;
    
    /**
     * 导出指定快照日期的Merkle数据到CSV文件
     * @param snapshotDate 快照日期
     * @throws Exception 导出异常
     */
    void exportMerkleDataBySnapshotDate(LocalDateTime snapshotDate) throws Exception;
}
