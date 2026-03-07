#!/bin/bash

# OWASP依赖检查脚本
# 用于检查项目依赖的安全漏洞

set -e

echo "=========================================="
echo "开始OWASP依赖安全检查"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查结果统计
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
WARNING_CHECKS=0

# 检查函数
check_result() {
    local check_name="$1"
    local result="$2"
    local expected="$3"
    local is_warning="${4:-false}"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ "$result" = "$expected" ]; then
        echo -e "${GREEN}✓${NC} $check_name: 通过"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        if [ "$is_warning" = "true" ]; then
            echo -e "${YELLOW}⚠${NC} $check_name: 警告"
            WARNING_CHECKS=$((WARNING_CHECKS + 1))
        else
            echo -e "${RED}✗${NC} $check_name: 失败"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
        fi
    fi
}

# 检查函数 - 命令执行
check_command() {
    local check_name="$1"
    local command="$2"
    local is_warning="${3:-false}"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} $check_name: 通过"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        if [ "$is_warning" = "true" ]; then
            echo -e "${YELLOW}⚠${NC} $check_name: 警告"
            WARNING_CHECKS=$((WARNING_CHECKS + 1))
        else
            echo -e "${RED}✗${NC} $check_name: 失败"
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
        fi
    fi
}

echo "1. 检查OWASP环境"
echo "----------------------------------------"
check_command "Maven环境检查" "mvn --version"
check_command "Java环境检查" "java -version"

echo ""
echo "2. 已知漏洞检查"
echo "----------------------------------------"
echo "执行OWASP依赖检查..."
mvn org.owasp:dependency-check-maven:check -pl medicine-module-medication/medicine-module-medication-biz -q

# 检查报告文件
REPORT_FILE="medicine-module-medication/medicine-module-medication-biz/target/dependency-check-report.html"
if [ -f "$REPORT_FILE" ]; then
    check_result "OWASP报告生成" "成功" "成功"
    
    # 检查高优先级漏洞
    HIGH_VULNERABILITIES=$(grep -c "High" "$REPORT_FILE" || echo "0")
    if [ $HIGH_VULNERABILITIES -eq 0 ]; then
        check_result "高优先级漏洞检查" "无漏洞" "无漏洞"
    else
        check_result "高优先级漏洞检查" "发现$HIGH_VULNERABILITIES个" "无漏洞"
    fi
    
    # 检查中优先级漏洞
    MEDIUM_VULNERABILITIES=$(grep -c "Medium" "$REPORT_FILE" || echo "0")
    if [ $MEDIUM_VULNERABILITIES -eq 0 ]; then
        check_result "中优先级漏洞检查" "无漏洞" "无漏洞"
    else
        check_result "中优先级漏洞检查" "发现$MEDIUM_VULNERABILITIES个" "无漏洞" "true"
    fi
    
    # 检查低优先级漏洞
    LOW_VULNERABILITIES=$(grep -c "Low" "$REPORT_FILE" || echo "0")
    if [ $LOW_VULNERABILITIES -eq 0 ]; then
        check_result "低优先级漏洞检查" "无漏洞" "无漏洞"
    else
        check_result "低优先级漏洞检查" "发现$LOW_VULNERABILITIES个" "无漏洞" "true"
    fi
else
    check_result "OWASP报告生成" "失败" "成功"
fi

echo ""
echo "3. 许可证检查"
echo "----------------------------------------"
echo "检查依赖许可证..."
LICENSE_FILE="medicine-module-medication/medicine-module-medication-biz/target/licenses.txt"
if [ -f "$LICENSE_FILE" ]; then
    check_result "许可证文件生成" "成功" "成功"
    
    # 检查GPL许可证
    GPL_LICENSES=$(grep -i "gpl" "$LICENSE_FILE" | wc -l || echo "0")
    if [ $GPL_LICENSES -eq 0 ]; then
        check_result "GPL许可证检查" "无GPL依赖" "无GPL依赖"
    else
        check_result "GPL许可证检查" "发现$GPL_LICENSES个GPL依赖" "无GPL依赖" "true"
    fi
    
    # 检查AGPL许可证
    AGPL_LICENSES=$(grep -i "agpl" "$LICENSE_FILE" | wc -l || echo "0")
    if [ $AGPL_LICENSES -eq 0 ]; then
        check_result "AGPL许可证检查" "无AGPL依赖" "无AGPL依赖"
    else
        check_result "AGPL许可证检查" "发现$AGPL_LICENSES个AGPL依赖" "无AGPL依赖" "true"
    fi
else
    check_result "许可证文件生成" "失败" "成功"
fi

echo ""
echo "4. 依赖更新检查"
echo "----------------------------------------"
echo "检查过时依赖..."
OUTDATED_DEPS=$(mvn versions:display-dependency-updates -pl medicine-module-medication/medicine-module-medication-biz -q 2>&1 | grep -c "newer version available" || echo "0")
if [ $OUTDATED_DEPS -eq 0 ]; then
    check_result "过时依赖检查" "无过时依赖" "无过时依赖"
else
    check_result "过时依赖检查" "发现$OUTDATED_DEPS个过时依赖" "无过时依赖" "true"
fi

echo ""
echo "5. 安全配置检查"
echo "----------------------------------------"
echo "检查安全配置..."

# 检查HTTPS配置
HTTPS_CONFIG=$(grep -r "https://" medicine-module-medication/medicine-module-medication-biz/src/ | wc -l || echo "0")
if [ $HTTPS_CONFIG -gt 0 ]; then
    check_result "HTTPS配置检查" "已配置HTTPS" "已配置HTTPS"
else
    check_result "HTTPS配置检查" "未配置HTTPS" "已配置HTTPS" "true"
fi

# 检查敏感信息泄露
SENSITIVE_INFO=$(grep -r -i "password\|secret\|key\|token" medicine-module-medication/medicine-module-medication-biz/src/ | grep -v "//" | wc -l || echo "0")
if [ $SENSITIVE_INFO -eq 0 ]; then
    check_result "敏感信息检查" "无敏感信息泄露" "无敏感信息泄露"
else
    check_result "敏感信息检查" "发现$SENSITIVE_INFO个敏感信息" "无敏感信息泄露" "true"
fi

echo ""
echo "6. 依赖分析"
echo "----------------------------------------"
echo "分析依赖关系..."
mvn dependency:analyze -pl medicine-module-medication/medicine-module-medication-biz -q > dependency-analysis.txt

# 检查未使用的依赖
UNUSED_DEPS=$(grep -c "Unused declared dependencies" dependency-analysis.txt || echo "0")
if [ $UNUSED_DEPS -eq 0 ]; then
    check_result "未使用依赖检查" "无未使用依赖" "无未使用依赖"
else
    check_result "未使用依赖检查" "发现$UNUSED_DEPS个未使用依赖" "无未使用依赖" "true"
fi

# 检查未声明的依赖
UNDECLARED_DEPS=$(grep -c "Used undeclared dependencies" dependency-analysis.txt || echo "0")
if [ $UNDECLARED_DEPS -eq 0 ]; then
    check_result "未声明依赖检查" "无未声明依赖" "无未声明依赖"
else
    check_result "未声明依赖检查" "发现$UNDECLARED_DEPS个未声明依赖" "无未声明依赖"
fi

echo ""
echo "7. 生成安全报告"
echo "----------------------------------------"
echo "生成安全报告..."
SECURITY_REPORT="security-report.txt"
cat > "$SECURITY_REPORT" << EOF
OWASP依赖安全检查报告
生成时间: $(date)
项目: medicine-module-medication-biz

漏洞统计:
- 高优先级漏洞: $HIGH_VULNERABILITIES
- 中优先级漏洞: $MEDIUM_VULNERABILITIES
- 低优先级漏洞: $LOW_VULNERABILITIES

许可证统计:
- GPL许可证: $GPL_LICENSES
- AGPL许可证: $AGPL_LICENSES

依赖统计:
- 过时依赖: $OUTDATED_DEPS
- 未使用依赖: $UNUSED_DEPS
- 未声明依赖: $UNDECLARED_DEPS

安全配置:
- HTTPS配置: $([ $HTTPS_CONFIG -gt 0 ] && echo "已配置" || echo "未配置")
- 敏感信息: $([ $SENSITIVE_INFO -eq 0 ] && echo "无泄露" || echo "存在泄露")
EOF

check_result "安全报告生成" "成功" "成功"
echo "安全报告已生成: $SECURITY_REPORT"

echo ""
echo "=========================================="
echo "OWASP依赖安全检查完成"
echo "=========================================="
echo "总检查项: $TOTAL_CHECKS"
echo -e "通过: ${GREEN}$PASSED_CHECKS${NC}"
echo -e "警告: ${YELLOW}$WARNING_CHECKS${NC}"
echo -e "失败: ${RED}$FAILED_CHECKS${NC}"

# 清理临时文件
rm -f dependency-analysis.txt

if [ $FAILED_CHECKS -eq 0 ]; then
    if [ $WARNING_CHECKS -eq 0 ]; then
        echo -e "${GREEN}所有安全检查通过！${NC}"
        exit 0
    else
        echo -e "${YELLOW}安全检查完成，存在 $WARNING_CHECKS 个警告！${NC}"
        exit 0
    fi
else
    echo -e "${RED}存在 $FAILED_CHECKS 个安全检查失败！${NC}"
    exit 1
fi
