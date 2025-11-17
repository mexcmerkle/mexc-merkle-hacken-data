# MXC Merkle Data Export Tool

## 项目简介

这是一个用于导出MXC Merkle Tree叶子节点数据到CSV文件的命令行工具。该工具能够从数据库中读取`fin_merkle_tree_leaf_data`表的数据，解析JSON格式的资产信息，并按币种聚合后导出为CSV格式。

## 技术栈

- Java 8
- Spring Boot 2.7.18
- MyBatis
- MySQL
- EasyExcel
- Jackson
- Maven

## 功能特性

- ✅ 分批处理数据，防止内存溢出(OOM)
- ✅ 使用Jackson解析JSON格式的balance_data字段
- ✅ 按币种前缀(USDT、USDC、BTC、ETH)聚合金额
- ✅ 使用EasyExcel流式写入CSV文件
- ✅ 自动计算导出文件的MD5值
- ✅ 详细的日志输出和进度显示
- ✅ 命令行方式执行，支持一键运行
- ✅ 支持按快照日期过滤导出数据
- ✅ 灵活的日期格式支持（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）

## 项目结构

```
mxc-merkle-hacken-data/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/mxc/merkle/
│       │       ├── MerkleDataExportApplication.java    # 主启动类
│       │       ├── entity/                             # 实体类
│       │       │   ├── FinMerkleTreeLeafData.java     # 数据库实体
│       │       │   └── ExportData.java                # 导出数据DTO
│       │       ├── mapper/                             # MyBatis映射
│       │       │   └── FinMerkleTreeLeafDataMapper.java
│       │       ├── service/                            # 业务服务
│       │       │   ├── ExportService.java
│       │       │   └── impl/ExportServiceImpl.java
│       │       ├── runner/                             # 命令行运行器
│       │       │   └── ExportCommandLineRunner.java
│       │       └── util/                               # 工具类
│       │           └── MD5Util.java
│       └── resources/
│           ├── application.yml                         # 配置文件
│           └── mapper/                                 # MyBatis XML
│               └── FinMerkleTreeLeafDataMapper.xml
├── exports/                                            # 导出文件目录
├── pom.xml                                            # Maven配置
└── README.md                                          # 项目说明
```

## 配置说明

### 数据库配置

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 导出配置

可以在 `application.yml` 中调整导出参数：

```yaml
export:
  batch-size: 1000          # 批处理大小
  output-dir: ./exports     # 导出文件目录
  file-prefix: merkle_data  # CSV文件名前缀
```

## 使用方法

### 1. 编译项目

```bash
mvn clean package
```

### 2. 运行导出

#### 导出所有数据
```bash
java -jar target/mxc-merkle-hacken-data-1.0.0.jar
```

#### 导出指定快照日期的数据
```bash
# 指定日期（时间默认为00:00:00）
java -jar target/mxc-merkle-hacken-data-1.0.0.jar --snapshot-date 2024-11-16

# 指定完整的日期时间
java -jar target/mxc-merkle-hacken-data-1.0.0.jar --spring.config.name=application-export --snapshot-date "2024-11-16 10:30:00"
```

### 3. 查看结果

程序执行完成后会输出：
- 导出文件路径
- 总记录数
- 文件大小
- 文件MD5值

## CSV输出格式

导出的CSV文件包含以下5列：

| 列名 | 说明 | 数据来源 |
|------|------|----------|
| memberId | 用户ID | 直接取自member_id字段 |
| USDT | USDT总金额 | 从balance_data中聚合所有USDT:*的值 |
| USDC | USDC总金额 | 从balance_data中聚合所有USDC:*的值 |
| BTC | BTC总金额 | 从balance_data中聚合所有BTC:*的值 |
| ETH | ETH总金额 | 从balance_data中聚合所有ETH:*的值 |

### balance_data解析示例

```json
{
  "USDT:ERC20": "100.50",
  "USDT:TRC20": "200.25", 
  "USDC:ERC20": "300.75",
  "BTC:NATIVE": "0.001",
  "ETH:NATIVE": "0.5"
}
```

解析后聚合结果：
- USDT: 300.75 (100.50 + 200.25)
- USDC: 300.75
- BTC: 0.001
- ETH: 0.5

## 性能特性

- **内存安全**: 采用分批查询+流式写入，避免大数据量导致的OOM
- **高效处理**: 默认每批处理1000条记录，可根据服务器配置调整
- **查询优化**: 
  - 全量导出使用传统分页查询（LIMIT OFFSET）
  - 快照日期导出使用基于ID范围的查询，避免大偏移量慢查询问题
- **进度监控**: 实时显示处理进度和剩余数量
- **错误处理**: 完善的异常处理机制，单条数据解析失败不影响整体导出

## 日志输出示例

```
2024-11-16 22:20:00 [main] INFO  c.m.m.r.ExportCommandLineRunner - === MXC Merkle Data Export Tool ===
2024-11-16 22:20:00 [main] INFO  c.m.m.r.ExportCommandLineRunner - 开始执行数据导出任务...
2024-11-16 22:20:01 [main] INFO  c.m.m.s.i.ExportServiceImpl - 开始导出Merkle数据...
2024-11-16 22:20:01 [main] INFO  c.m.m.s.i.ExportServiceImpl - 总记录数: 15272384
2024-11-16 22:20:02 [main] INFO  c.m.m.s.i.ExportServiceImpl - 已处理 1000 / 15272384 条记录
2024-11-16 22:20:03 [main] INFO  c.m.m.s.i.ExportServiceImpl - 已处理 2000 / 15272384 条记录
...
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - 导出完成！
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - 文件路径: ./exports/merkle_data_20241116_222000.csv
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - 总记录数: 15272384
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - 文件大小: 1234567890 bytes
2024-11-16 22:25:30 [main] INFO  c.m.m.s.i.ExportServiceImpl - 文件MD5: a1b2c3d4e5f67890abcdef1234567890
```

## 注意事项

1. **数据库权限**: 确保数据库用户有读取`fin_merkle_tree_leaf_data`表的权限
2. **磁盘空间**: 确保有足够的磁盘空间存储导出文件
3. **内存配置**: 对于超大数据量，可适当调整JVM内存参数
4. **网络稳定**: 确保数据库连接稳定，避免长时间导出过程中断线

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库连接配置
   - 确认数据库服务是否正常运行
   - 验证用户名密码是否正确

2. **内存不足**
   - 减小batch-size配置
   - 增加JVM内存参数：`java -Xmx2g -jar xxx.jar`

3. **文件写入失败**
   - 检查导出目录是否有写入权限
   - 确认磁盘空间是否充足

## 开发者信息

- 开发语言: Java 8
- 框架: Spring Boot + MyBatis
- 构建工具: Maven
- 数据库: MySQL
- 导出格式: CSV (使用EasyExcel)
