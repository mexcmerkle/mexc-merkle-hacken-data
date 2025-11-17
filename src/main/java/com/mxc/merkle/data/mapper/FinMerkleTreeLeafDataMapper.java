package com.mxc.merkle.data.mapper;

import com.mxc.merkle.data.entity.FinMerkleTreeLeafData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Merkle tree leaf node data Mapper
 */
@Mapper
public interface FinMerkleTreeLeafDataMapper {
    
    /**
     * Query total record count
     * @return Total record count
     */
    Long countAll();
    
    /**
     * Query total record count by snapshot date
     * @param snapshotDate Snapshot date
     * @return Total record count
     */
    Long countBySnapshotDate(@Param("snapshotDate") LocalDateTime snapshotDate);
    
    /**
     * Query data with pagination
     * @param offset Offset
     * @param limit Limit count
     * @return Data list
     */
    List<FinMerkleTreeLeafData> selectByPage(@Param("offset") Long offset, @Param("limit") Integer limit);
    
    /**
     * Query data by snapshot date with pagination
     * @param snapshotDate Snapshot date
     * @param offset Offset
     * @param limit Limit count
     * @return Data list
     */
    List<FinMerkleTreeLeafData> selectBySnapshotDateAndPage(@Param("snapshotDate") LocalDateTime snapshotDate, 
                                                            @Param("offset") Long offset, 
                                                            @Param("limit") Integer limit);
    
    /**
     * Find minimum ID by snapshot date
     * @param snapshotDate Snapshot date
     * @return Minimum ID
     */
    Long findMinIdBySnapshotDate(@Param("snapshotDate") LocalDateTime snapshotDate);
    
    /**
     * Find maximum ID by snapshot date
     * @param snapshotDate Snapshot date
     * @return Maximum ID
     */
    Long findMaxIdBySnapshotDate(@Param("snapshotDate") LocalDateTime snapshotDate);
    
    /**
     * Query data by snapshot date and ID range
     * @param snapshotDate Snapshot date
     * @param minId Minimum ID (inclusive)
     * @param maxId Maximum ID (inclusive)
     * @param limit Limit count
     * @return Data list
     */
    List<FinMerkleTreeLeafData> selectBySnapshotDateAndIdRange(@Param("snapshotDate") LocalDateTime snapshotDate,
                                                               @Param("minId") Long minId,
                                                               @Param("maxId") Long maxId,
                                                               @Param("limit") Integer limit);
}
