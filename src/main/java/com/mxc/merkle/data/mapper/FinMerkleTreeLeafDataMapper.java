package com.mxc.merkle.data.mapper;

import com.mxc.merkle.data.entity.FinMerkleTreeLeafData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * merkle tree叶子节点数据Mapper
 */
@Mapper
public interface FinMerkleTreeLeafDataMapper {
    
    /**
     * 查询总记录数
     * @return 总记录数
     */
    Long countAll();
    
    /**
     * 根据快照日期查询总记录数
     * @param snapshotDate 快照日期
     * @return 总记录数
     */
    Long countBySnapshotDate(@Param("snapshotDate") LocalDateTime snapshotDate);
    
    /**
     * 分页查询数据
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 数据列表
     */
    List<FinMerkleTreeLeafData> selectByPage(@Param("offset") Long offset, @Param("limit") Integer limit);
    
    /**
     * 根据快照日期分页查询数据
     * @param snapshotDate 快照日期
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 数据列表
     */
    List<FinMerkleTreeLeafData> selectBySnapshotDateAndPage(@Param("snapshotDate") LocalDateTime snapshotDate, 
                                                            @Param("offset") Long offset, 
                                                            @Param("limit") Integer limit);
    
    /**
     * 根据快照日期查询最小ID
     * @param snapshotDate 快照日期
     * @return 最小ID
     */
    Long findMinIdBySnapshotDate(@Param("snapshotDate") LocalDateTime snapshotDate);
    
    /**
     * 根据快照日期查询最大ID
     * @param snapshotDate 快照日期
     * @return 最大ID
     */
    Long findMaxIdBySnapshotDate(@Param("snapshotDate") LocalDateTime snapshotDate);
    
    /**
     * 根据快照日期和ID范围查询数据
     * @param snapshotDate 快照日期
     * @param minId 最小ID（包含）
     * @param maxId 最大ID（包含）
     * @param limit 限制数量
     * @return 数据列表
     */
    List<FinMerkleTreeLeafData> selectBySnapshotDateAndIdRange(@Param("snapshotDate") LocalDateTime snapshotDate,
                                                               @Param("minId") Long minId,
                                                               @Param("maxId") Long maxId,
                                                               @Param("limit") Integer limit);
}
