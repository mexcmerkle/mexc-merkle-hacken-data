package com.mexc.merkle.data.entity;

/**
 * File part information
 */
public class FilePartInfo {
    private final String fileName;
    private final String filePath;
    private final long rowCount;
    private final long fileSize;
    private final String md5;
    
    public FilePartInfo(String fileName, String filePath, long rowCount, long fileSize, String md5) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.rowCount = rowCount;
        this.fileSize = fileSize;
        this.md5 = md5;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public long getRowCount() {
        return rowCount;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public String getMd5() {
        return md5;
    }
}
