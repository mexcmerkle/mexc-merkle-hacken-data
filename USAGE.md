# 快速使用指南

## 1. 配置数据库连接

复制配置文件模板并修改数据库连接信息：

```bash
cp application-example.yml src/main/resources/application.yml
```

然后编辑 `src/main/resources/application.yml` 文件，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database_name
    username: your_username
    password: your_password
```

## 2. 编译项目

```bash
mvn clean package
```

## 3. 运行导出

### 方式一：使用启动脚本（推荐）

```bash
./run.sh
```

### 方式二：直接运行jar

#### 导出所有数据
```bash
java -jar target/mexc-merkle-hacken-data-1.0.0.jar
```

#### 导出指定快照日期的数据
```bash
# 指定日期（时间默认为00:00:00）
java -jar target/mexc-merkle-hacken-data-1.0.0.jar --snapshot-date 2024-11-16

# 指定完整的日期时间
java -jar target/mexc-merkle-hacken-data-1.0.0.jar --snapshot-date "2024-11-16 10:30:00"
```

### 方式三：带JVM参数运行

#### 导出所有数据
```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar target/mexc-merkle-hacken-data-1.0.0.jar
```

#### 导出指定快照日期的数据
```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar target/mexc-merkle-hacken-data-1.0.0.jar --snapshot-date 2024-11-16
```

## 4. 快照日期参数说明

### 支持的日期格式
- `yyyy-MM-dd`：指定日期，时间默认为 00:00:00
- `yyyy-MM-dd HH:mm:ss`：指定完整的日期时间

### 示例
```bash
# 导出2024年11月16日的所有数据
java -jar target/mexc-merkle-hacken-data-1.0.0.jar --snapshot-date 2024-11-16

# 导出2024年11月16日10:30:00的数据
java -jar target/mexc-merkle-hacken-data-1.0.0.jar --snapshot-date "2024-11-16 10:30:00"
```

### 文件命名规则
- 全量导出：`merkle_data_yyyyMMdd_HHmmss.csv`
- 按日期导出：`merkle_data_yyyyMMdd_HHmmss_yyyyMMdd.csv`

## 5. 查看导出结果

导出完成后，CSV文件将保存在 `exports/` 目录下。

程序会输出：
- 导出文件路径
- 总记录数
- 文件大小
- 文件MD5值

## 6. 配置调优

### 内存配置
- 小数据量（< 100万条）：`-Xmx1g`
- 中等数据量（100万-1000万条）：`-Xmx2g`
- 大数据量（> 1000万条）：`-Xmx4g` 或更高

### 批处理大小
在 `application.yml` 中调整：
```yaml
export:
  batch-size: 1000  # 可调整为 500-2000
```

## 7. 故障排除

### 数据库连接失败
- 检查数据库服务是否启动
- 验证连接信息是否正确
- 确认网络连通性

### 内存不足
- 增加JVM内存参数
- 减小batch-size配置
- 检查系统可用内存

### 文件写入失败
- 检查exports目录权限
- 确认磁盘空间充足
- 验证文件路径是否正确

## 8. CSV输出格式

| 列名 | 说明 | 示例 |
|------|------|------|
| memberId | 用户ID | user123456 |
| USDT | USDT总金额 | 1000.50 |
| USDC | USDC总金额 | 500.25 |
| BTC | BTC总金额 | 0.001 |
| ETH | ETH总金额 | 0.5 |
