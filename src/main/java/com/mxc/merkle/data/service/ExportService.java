package com.mxc.merkle.data.service;

import java.time.LocalDateTime;

/**
 * Export service interface
 */
public interface ExportService {
    
    /**
     * Export Merkle data for specified snapshot date to CSV file
     * @param snapshotDate Snapshot date
     * @throws Exception Export exception
     */
    void exportMerkleDataBySnapshotDate(LocalDateTime snapshotDate) throws Exception;
}
