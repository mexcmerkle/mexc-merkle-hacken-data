#!/bin/bash

# MXC Merkle Data Export Tool 启动脚本

echo "=== MXC Merkle Data Export Tool ==="
echo "正在启动数据导出工具..."

# 检查jar文件是否存在
JAR_FILE="target/mxc-merkle-hacken-data-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 找不到jar文件 $JAR_FILE"
    echo "请先运行: mvn clean package"
    exit 1
fi

# 检查配置文件是否存在
CONFIG_FILE="src/main/resources/application.yml"
if [ ! -f "$CONFIG_FILE" ]; then
    echo "错误: 找不到配置文件 $CONFIG_FILE"
    echo "请参考 application-example.yml 创建配置文件"
    exit 1
fi

# 创建导出目录
mkdir -p exports

# 设置JVM参数
JVM_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"

echo "JVM参数: $JVM_OPTS"
echo "开始执行导出任务..."
echo ""

# 运行程序
java $JVM_OPTS -jar "$JAR_FILE"

echo ""
echo "导出任务完成！"
