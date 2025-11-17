package com.mxc.merkle.data.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * CSV导出数据传输对象
 */
@Getter
@Setter
@Builder
public class ExportData {
    
    /**
     * 用户ID
     */
    @ExcelProperty("memberId")
    private String memberId;
    
    /**
     * USDT总金额
     */
    @ExcelProperty("USDT")
    private BigDecimal usdt;
    
    /**
     * USDC总金额
     */
    @ExcelProperty("USDC")
    private BigDecimal usdc;
    
    /**
     * BTC总金额
     */
    @ExcelProperty("BTC")
    private BigDecimal btc;
    
    /**
     * ETH总金额
     */
    @ExcelProperty("ETH")
    private BigDecimal eth;
}
