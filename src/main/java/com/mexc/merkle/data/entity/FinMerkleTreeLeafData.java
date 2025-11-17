package com.mexc.merkle.data.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Merkle tree leaf node data entity class
 */
@Getter
@Setter
public class FinMerkleTreeLeafData {
    
    /**
     * Auto-increment ID
     */
    private Long id;
    
    /**
     * Record hash ID
     */
    private String hashId;
    
    /**
     * Record ID
     */
    private String recordId;
    
    /**
     * Member ID
     */
    private String memberId;
    
    /**
     * Digital ID
     */
    private String digitalId;
    
    /**
     * Merkle path
     */
    private String merklePath;
    
    /**
     * Balance data (JSON format)
     */
    private String balanceData;
    
    /**
     * Snapshot date
     */
    private LocalDateTime snapshotDate;
    
    /**
     * Create time
     */
    private LocalDateTime createTime;
    
    /**
     * Update time
     */
    private LocalDateTime updateTime;
    
    /**
     * Extended information
     */
    private String extendsInfo;
}
