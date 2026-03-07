#!/usr/bin/env python3
"""
密码生成工具
用于生成BCrypt加密的密码
"""

import bcrypt
import sys

def generate_bcrypt_password(password, strength=10):
    """生成BCrypt加密的密码
    
    Args:
        password: 明文密码
        strength: BCrypt强度，默认为10（与后端配置一致）
    """
    salt = bcrypt.gensalt(rounds=strength)
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def main():
    if len(sys.argv) != 2:
        print("用法: python generate_password.py <明文密码>")
        print("示例: python generate_password.py 123456")
        sys.exit(1)
    
    plain_password = sys.argv[1]
    hashed_password = generate_bcrypt_password(plain_password)
    
    print(f"明文密码: {plain_password}")
    print(f"BCrypt加密: {hashed_password}")
    print(f"密码长度: {len(hashed_password)} 字符")
    
    # 生成SQL更新语句
    print("\nSQL更新语句:")
    print(f"UPDATE system_users SET password = '{hashed_password}' WHERE username = 'your_username';")

if __name__ == "__main__":
    main()
