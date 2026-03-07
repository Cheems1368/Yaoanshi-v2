#!/usr/bin/env python3
"""
医疗管理系统 Web预览启动脚本
"""

import http.server
import socketserver
import webbrowser
import os
import sys
from pathlib import Path

def main():
    # 设置端口
    PORT = 8000
    
    # 切换到web-preview目录
    web_preview_dir = Path("web-preview")
    if not web_preview_dir.exists():
        print("错误: web-preview目录不存在!")
        print("请确保项目结构正确。")
        sys.exit(1)
    
    os.chdir(web_preview_dir)
    
    # 检查index.html是否存在
    if not Path("index.html").exists():
        print("错误: index.html文件不存在!")
        sys.exit(1)
    
    # 创建HTTP服务器
    Handler = http.server.SimpleHTTPRequestHandler
    
    try:
        with socketserver.TCPServer(("", PORT), Handler) as httpd:
            print(f"🚀 医疗管理系统 Web预览已启动!")
            print(f"📱 访问地址: http://localhost:{PORT}")
            print(f"📁 服务目录: {os.getcwd()}")
            print(f"🔄 按 Ctrl+C 停止服务器")
            print("-" * 50)
            
            # 自动打开浏览器
            webbrowser.open(f"http://localhost:{PORT}")
            
            # 启动服务器
            httpd.serve_forever()
            
    except KeyboardInterrupt:
        print("\n🛑 服务器已停止")
    except OSError as e:
        if e.errno == 48:  # Address already in use
            print(f"❌ 端口 {PORT} 已被占用!")
            print(f"请尝试使用其他端口或关闭占用该端口的程序")
        else:
            print(f"❌ 启动服务器失败: {e}")
    except Exception as e:
        print(f"❌ 未知错误: {e}")

if __name__ == "__main__":
    main()
