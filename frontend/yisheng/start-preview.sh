#!/bin/bash

echo "========================================"
echo "   医疗管理系统 Web预览启动器"
echo "========================================"
echo

# 检查Python是否安装
if ! command -v python3 &> /dev/null; then
    echo "❌ 错误: 未检测到Python3!"
    echo "请先安装Python 3.x"
    echo "Ubuntu/Debian: sudo apt install python3"
    echo "CentOS/RHEL: sudo yum install python3"
    echo "macOS: brew install python3"
    exit 1
fi

# 检查web-preview目录是否存在
if [ ! -d "web-preview" ]; then
    echo "❌ 错误: web-preview目录不存在!"
    echo "请确保项目结构正确。"
    exit 1
fi

# 检查index.html是否存在
if [ ! -f "web-preview/index.html" ]; then
    echo "❌ 错误: index.html文件不存在!"
    exit 1
fi

echo "🚀 正在启动Web预览服务器..."
echo "📱 访问地址: http://localhost:8000"
echo "📁 服务目录: web-preview"
echo "🔄 按 Ctrl+C 停止服务器"
echo

# 切换到web-preview目录并启动服务器
cd web-preview
python3 -m http.server 8000

echo
echo "🛑 服务器已停止"
