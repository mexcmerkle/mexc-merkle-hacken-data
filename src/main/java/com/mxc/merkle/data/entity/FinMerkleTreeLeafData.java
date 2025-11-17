package com.mxc.merkle.data.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * merkle tree叶子节点数据实体类
 */
@Getter
@Setter
public class FinMerkleTreeLeafData {
    
    /**
     * 自增id
     */
    private Long id;
    
    /**
     * 记录hashId
     */
    private String hashId;
    
    /**
     * 记录Id
     */
    private String recordId;
    
    /**
     * 用户id
     */
    private String memberId;
    
    /**
     * 用户Uid
     */
    private String digitalId;
    
    /**
     * 默克尔路径
     */
    private String merklePath;
    
    /**
     * 资产信息(json)
     */
    private String balanceData;
    
    /**
     * 快照日期
     */
    private LocalDateTime snapshotDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 扩展信息
     */
    private String extendsInfo;
}
