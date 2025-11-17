package com.mxc.merkle.data.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * CSV export data transfer object
 */
@Getter
@Setter
@Builder
public class ExportData {
    
    /**
     * Member ID
     */
    @ExcelProperty("memberId")
    private String memberId;
    
    /**
     * Total USDT amount
     */
    @ExcelProperty("USDT")
    private BigDecimal usdt;
    
    /**
     * Total USDC amount
     */
    @ExcelProperty("USDC")
    private BigDecimal usdc;
    
    /**
     * Total BTC amount
     */
    @ExcelProperty("BTC")
    private BigDecimal btc;
    
    /**
     * Total ETH amount
     */
    @ExcelProperty("ETH")
    private BigDecimal eth;
}
