# MXC Merkle Data Export Tool

## Project Overview

This is a command-line tool for exporting MXC Merkle Tree leaf node data to CSV files. It aggregates data by cryptocurrency and exports in CSV format.

## Technology Stack

- Java 8
- Spring Boot 2.7.18
- MyBatis
- MySQL
- EasyExcel
- Jackson
- Maven

## Features

- ✅ Batch processing to prevent Out of Memory (OOM) issues
- ✅ Parse JSON format balance_data field using Jackson
- ✅ Aggregate amounts by cryptocurrency prefixes (USDT, USDC, BTC, ETH)
- ✅ Stream writing CSV files using EasyExcel
- ✅ Automatically calculate MD5 hash of exported files
- ✅ Detailed logging and progress display
- ✅ Command-line execution with one-click operation
- ✅ Support filtering export data by snapshot date
- ✅ Flexible date format support (yyyy-MM-dd or yyyy-MM-dd HH:mm:ss)

## Project Structure

```
mxc-merkle-hacken-data/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/mxc/merkle/
│       │       ├── MerkleDataExportApplication.java    # Main application class
│       │       ├── entity/                             # Entity classes
│       │       │   ├── FinMerkleTreeLeafData.java     # Database entity
│       │       │   └── ExportData.java                # Export data DTO
│       │       ├── mapper/                             # MyBatis mappers
│       │       │   └── FinMerkleTreeLeafDataMapper.java
│       │       ├── service/                            # Business services
│       │       │   ├── ExportService.java
│       │       │   └── impl/ExportServiceImpl.java
│       │       ├── runner/                             # Command line runner
│       │       │   └── ExportCommandLineRunner.java
│       │       └── util/                               # Utility classes
│       │           └── MD5Util.java
│       └── resources/
│           ├── application.yml                         # Configuration file
│           └── mapper/                                 # MyBatis XML
│               └── FinMerkleTreeLeafDataMapper.xml
├── exports/                                            # Export files directory
├── pom.xml                                            # Maven configuration
└── README.md                                          # Project documentation
```

## Configuration

### Database Configuration

Modify the database connection information in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### Export Configuration

You can adjust export parameters in `application.yml`:

```yaml
export:
  batch-size: 1000          # Batch processing size
  output-dir: ./exports     # Export files directory
  file-prefix: merkle_data  # CSV file name prefix
```

## Usage

### 1. Compile Project

```bash
mvn clean package
```

### 2. Run Export

#### Export All Data
```bash
java -jar target/mxc-merkle-hacken-data-1.0.0.jar
```

#### Export Data for Specific Snapshot Date
```bash
# Specify date (time defaults to 00:00:00)
java -jar target/mxc-merkle-hacken-data-1.0.0.jar --snapshot-date 2024-11-16

# Specify complete date and time
java -jar target/mxc-merkle-hacken-data-1.0.0.jar --spring.config.name=application-export --snapshot-date "2024-11-16 10:30:00"
```

### 3. View Results

After program execution, it will output:
- Export file path
- Total record count
- File size
- File MD5 hash

## CSV Output Format

The exported CSV file contains the following 5 columns:

| Column Name | Description | Data Source |
|-------------|-------------|-------------|
| memberId | User ID | Directly from member_id field |
| USDT | Total USDT amount | Aggregated from all USDT:* values in balance_data |
| USDC | Total USDC amount | Aggregated from all USDC:* values in balance_data |
| BTC | Total BTC amount | Aggregated from all BTC:* values in balance_data |
| ETH | Total ETH amount | Aggregated from all ETH:* values in balance_data |

### balance_data Parsing Example

```json
{
  "USDT:ERC20": "100.50",
  "USDT:TRC20": "200.25", 
  "USDC:ERC20": "300.75",
  "BTC:NATIVE": "0.001",
  "ETH:NATIVE": "0.5"
}
```

Aggregated results after parsing:
- USDT: 300.75 (100.50 + 200.25)
- USDC: 300.75
- BTC: 0.001
- ETH: 0.5

## Performance Features

- **Memory Safe**: Uses batch querying + stream writing to avoid OOM with large datasets
- **Efficient Processing**: Default batch size of 1000 records, adjustable based on server configuration
- **Query Optimization**:
    - Full export uses traditional pagination (LIMIT OFFSET)
    - Snapshot date export uses ID range-based queries to avoid slow queries with large offsets
- **Progress Monitoring**: Real-time display of processing progress and remaining count
- **Error Handling**: Comprehensive exception handling mechanism, single record parsing failure doesn't affect overall export

## Log Output Example

```
2024-11-16 22:20:00 [main] INFO  c.m.m.r.ExportCommandLineRunner - === MXC Merkle Data Export Tool ===
2024-11-16 22:20:00 [main] INFO  c.m.m.r.ExportCommandLineRunner - Starting data export task...
2024-11-16 22:20:01 [main] INFO  c.m.m.s.i.ExportServiceImpl - Starting Merkle data export...
2024-11-16 22:20:01 [main] INFO  c.m.m.s.i.ExportServiceImpl - Total records: 15272384
2024-11-16 22:20:02 [main] INFO  c.m.m.s.i.ExportServiceImpl - Processed 1000 / 15272384 records
2024-11-16 22:20:03 [main] INFO  c.m.m.s.i.ExportServiceImpl - Processed 2000 / 15272384 records
...
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - Export completed!
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - File path: ./exports/merkle_data_20241116_222000.csv
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - Total records: 15272384
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - File size: 1234567890 bytes
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - File MD5: a1b2c3d4e5f67890abcdef1234567890
```

## Important Notes

1. **Disk Space**: Ensure sufficient disk space to store export files
2. **Memory Configuration**: For very large datasets, adjust JVM memory parameters appropriately
3. **Network Stability**: Ensure stable database connection to avoid disconnection during long export processes

## Troubleshooting

### Common Issues

1. **Database Connection Failure**
    - Check database connection configuration
    - Confirm database service is running normally
    - Verify username and password are correct

2. **Out of Memory**
    - Reduce batch-size configuration
    - Increase JVM memory parameters: `java -Xmx2g -jar xxx.jar`

3. **File Write Failure**
    - Check if export directory has write permissions
    - Confirm sufficient disk space

## Developer Information

- Development Language: Java 8
- Framework: Spring Boot + MyBatis
- Build Tool: Maven
- Database: MySQL
- Export Format: CSV (using EasyExcel)
