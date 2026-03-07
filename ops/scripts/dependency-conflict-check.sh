#!/bin/bash

# 依赖冲突检查脚本
# 用于检查项目中的依赖冲突问题

set -e

echo "=========================================="
echo "开始依赖冲突检查"
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

echo "1. 检查Maven环境"
echo "----------------------------------------"
check_command "Maven版本检查" "mvn --version"

echo ""
echo "2. 重复依赖检查"
echo "----------------------------------------"
echo "生成依赖树..."
mvn dependency:tree -pl medicine-module-medication/medicine-module-medication-biz -q > dependency-tree.txt

echo "检查重复依赖..."
DUPLICATE_DEPS=$(grep -E "\[INFO\].*:.*:.*:.*:.*" dependency-tree.txt | sort | uniq -d | wc -l)
check_result "重复依赖检查" "$([ $DUPLICATE_DEPS -eq 0 ] && echo "无重复" || echo "发现$DUPLICATE_DEPS个重复")" "无重复"

if [ $DUPLICATE_DEPS -gt 0 ]; then
    echo "发现的重复依赖:"
    grep -E "\[INFO\].*:.*:.*:.*:.*" dependency-tree.txt | sort | uniq -d
fi

echo ""
echo "3. 版本冲突检查"
echo "----------------------------------------"
echo "检查版本冲突..."
VERSION_CONFLICTS=$(mvn dependency:tree -pl medicine-module-medication/medicine-module-medication-biz -q 2>&1 | grep -i "conflict" | wc -l)
check_result "版本冲突检查" "$([ $VERSION_CONFLICTS -eq 0 ] && echo "无冲突" || echo "发现$VERSION_CONFLICTS个冲突")" "无冲突"

if [ $VERSION_CONFLICTS -gt 0 ]; then
    echo "发现的版本冲突:"
    mvn dependency:tree -pl medicine-module-medication/medicine-module-medication-biz -q 2>&1 | grep -i "conflict"
fi

echo ""
echo "4. 传递依赖检查"
echo "----------------------------------------"
echo "分析传递依赖..."
mvn dependency:analyze -pl medicine-module-medication/medicine-module-medication-biz -q > dependency-analyze.txt

echo "检查未使用的依赖..."
UNUSED_DEPS=$(grep -c "Unused declared dependencies" dependency-analyze.txt || echo "0")
check_result "未使用依赖检查" "$([ $UNUSED_DEPS -eq 0 ] && echo "无未使用" || echo "发现$UNUSED_DEPS个未使用")" "无未使用" "true"

echo "检查未声明的依赖..."
UNDECLARED_DEPS=$(grep -c "Used undeclared dependencies" dependency-analyze.txt || echo "0")
check_result "未声明依赖检查" "$([ $UNDECLARED_DEPS -eq 0 ] && echo "无未声明" || echo "发现$UNDECLARED_DEPS个未声明")" "无未声明"

if [ $UNDECLARED_DEPS -gt 0 ]; then
    echo "发现的未声明依赖:"
    grep -A 10 "Used undeclared dependencies" dependency-analyze.txt
fi

echo ""
echo "5. 可选依赖检查"
echo "----------------------------------------"
echo "检查可选依赖..."
OPTIONAL_DEPS=$(grep -E "\[INFO\].*:.*:.*:.*:.*:optional" dependency-tree.txt | wc -l)
check_result "可选依赖检查" "$([ $OPTIONAL_DEPS -eq 0 ] && echo "无可选依赖" || echo "发现$OPTIONAL_DEPS个可选依赖")" "无可选依赖" "true"

if [ $OPTIONAL_DEPS -gt 0 ]; then
    echo "发现的可选依赖:"
    grep -E "\[INFO\].*:.*:.*:.*:.*:optional" dependency-tree.txt
fi

echo ""
echo "6. 依赖收敛检查"
echo "----------------------------------------"
echo "检查依赖收敛..."
CONVERGENCE_ISSUES=$(mvn enforcer:enforce -pl medicine-module-medication/medicine-module-medication-biz -q 2>&1 | grep -i "dependency convergence" | wc -l)
check_result "依赖收敛检查" "$([ $CONVERGENCE_ISSUES -eq 0 ] && echo "收敛正常" || echo "发现$CONVERGENCE_ISSUES个收敛问题")" "收敛正常"

echo ""
echo "7. 循环依赖检查"
echo "----------------------------------------"
echo "检查循环依赖..."
CIRCULAR_DEPS=$(mvn dependency:tree -pl medicine-module-medication/medicine-module-medication-biz -q 2>&1 | grep -i "circular" | wc -l)
check_result "循环依赖检查" "$([ $CIRCULAR_DEPS -eq 0 ] && echo "无循环依赖" || echo "发现$CIRCULAR_DEPS个循环依赖")" "无循环依赖"

echo ""
echo "8. 安全漏洞检查"
echo "----------------------------------------"
echo "OWASP依赖检查..."
OWASP_ISSUES=$(mvn org.owasp:dependency-check-maven:check -pl medicine-module-medication/medicine-module-medication-biz -q 2>&1 | grep -i "vulnerability" | wc -l)
check_result "安全漏洞检查" "$([ $OWASP_ISSUES -eq 0 ] && echo "无安全漏洞" || echo "发现$OWASP_ISSUES个安全漏洞")" "无安全漏洞"

echo ""
echo "9. 依赖版本一致性检查"
echo "----------------------------------------"
echo "检查Spring Boot版本一致性..."
SPRING_BOOT_VERSIONS=$(grep -E "spring-boot" dependency-tree.txt | grep -oE "[0-9]+\.[0-9]+\.[0-9]+" | sort | uniq | wc -l)
check_result "Spring Boot版本一致性" "$([ $SPRING_BOOT_VERSIONS -le 1 ] && echo "版本一致" || echo "发现$SPRING_BOOT_VERSIONS个不同版本")" "版本一致"

echo "检查Jackson版本一致性..."
JACKSON_VERSIONS=$(grep -E "jackson" dependency-tree.txt | grep -oE "[0-9]+\.[0-9]+\.[0-9]+" | sort | uniq | wc -l)
check_result "Jackson版本一致性" "$([ $JACKSON_VERSIONS -le 1 ] && echo "版本一致" || echo "发现$JACKSON_VERSIONS个不同版本")" "版本一致" "true"

echo ""
echo "10. 依赖大小检查"
echo "----------------------------------------"
echo "检查依赖数量..."
DEP_COUNT=$(grep -E "\[INFO\].*:.*:.*:.*:.*" dependency-tree.txt | wc -l)
if [ $DEP_COUNT -lt 50 ]; then
    check_result "依赖数量检查" "合理($DEP_COUNT个)" "合理"
elif [ $DEP_COUNT -lt 100 ]; then
    check_result "依赖数量检查" "较多($DEP_COUNT个)" "合理" "true"
else
    check_result "依赖数量检查" "过多($DEP_COUNT个)" "合理"
fi

echo ""
echo "11. 生成依赖报告"
echo "----------------------------------------"
echo "生成详细依赖报告..."
mvn dependency:tree -pl medicine-module-medication/medicine-module-medication-biz -Dverbose=true -q > dependency-report.txt
echo "依赖报告已生成: dependency-report.txt"

echo ""
echo "=========================================="
echo "依赖冲突检查完成"
echo "=========================================="
echo "总检查项: $TOTAL_CHECKS"
echo -e "通过: ${GREEN}$PASSED_CHECKS${NC}"
echo -e "警告: ${YELLOW}$WARNING_CHECKS${NC}"
echo -e "失败: ${RED}$FAILED_CHECKS${NC}"

# 清理临时文件
rm -f dependency-tree.txt dependency-analyze.txt dependency-report.txt

if [ $FAILED_CHECKS -eq 0 ]; then
    if [ $WARNING_CHECKS -eq 0 ]; then
        echo -e "${GREEN}所有检查通过！${NC}"
        exit 0
    else
        echo -e "${YELLOW}检查完成，存在 $WARNING_CHECKS 个警告！${NC}"
        exit 0
    fi
else
    echo -e "${RED}存在 $FAILED_CHECKS 个检查失败！${NC}"
    exit 1
fi
