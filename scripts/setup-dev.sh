#!/bin/bash
# 本地开发环境快速启动脚本
# 用法: bash scripts/setup-dev.sh

set -e

echo "========================================"
echo " ai-quality-control 开发环境初始化"
echo "========================================"

# 1. 创建文件上传目录
echo "[1/4] 创建文件上传目录..."
mkdir -p /tmp/quality-control/uploads/concession
mkdir -p /tmp/quality-control/uploads/rejudgment
mkdir -p /tmp/quality-control/uploads/temp
echo "  ✓ 上传目录: /tmp/quality-control/uploads/"

# 2. 检查 SSH 隧道
echo "[2/4] 检查数据库连接..."
if ! nc -z 127.0.0.1 3307 2>/dev/null; then
    echo "  ⚠ MySQL 端口 3307 未监听，请先建立 SSH 隧道："
    echo "    ssh -L 3307:127.0.0.1:3306 -L 6380:127.0.0.1:6379 -N -f ubuntu@<SERVER_IP>"
    echo "  继续脚本，跳过数据库初始化..."
else
    echo "  ✓ MySQL 隧道 (127.0.0.1:3307) 已连接"

    # 3. 初始化数据库
    echo "[3/4] 初始化数据库..."
    if [ -z "$DB_PASSWORD" ]; then
        echo "  请输入数据库密码（或设置 DB_PASSWORD 环境变量）："
        read -s DB_PASSWORD
    fi

    mysql -u "${DB_USER:-root}" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 \
        < backend/scripts/init-schema.sql && echo "  ✓ 表结构初始化完成"
    mysql -u "${DB_USER:-root}" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 \
        < backend/scripts/init-dict-data.sql && echo "  ✓ 字典数据初始化完成"
    mysql -u "${DB_USER:-root}" -p"$DB_PASSWORD" -h 127.0.0.1 -P 3307 \
        < backend/scripts/init-test-data.sql && echo "  ✓ 测试数据初始化完成"
fi

# 4. 安装前端依赖
echo "[4/4] 安装前端依赖..."
if command -v npm &>/dev/null; then
    cd frontend && npm install && cd ..
    echo "  ✓ 前端依赖安装完成"
else
    echo "  ⚠ npm 未找到，请手动执行: cd frontend && npm install"
fi

echo ""
echo "========================================"
echo " 启动命令："
echo "  后端: cd backend && mvn spring-boot:run -Dspring.profiles.active=dev"
echo "  前端: cd frontend && npm run dev"
echo "  访问: http://localhost:5173"
echo "  API文档: http://localhost:8080/doc.html"
echo "  默认账号: admin / Admin123456"
echo "========================================"
