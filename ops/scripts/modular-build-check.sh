#!/bin/bash

# 模块化构建检查脚本
# 用于验证项目的模块化构建正确性

set -e

echo "=========================================="
echo "开始模块化构建检查"
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

# 检查函数
check_result() {
    local check_name="$1"
    local result="$2"
    local expected="$3"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ "$result" = "$expected" ]; then
        echo -e "${GREEN}✓${NC} $check_name: 通过"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}✗${NC} $check_name: 失败"
        echo "  期望: $expected"
        echo "  实际: $result"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
}

# 检查函数 - 命令执行
check_command() {
    local check_name="$1"
    local command="$2"
    
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} $check_name: 通过"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}✗${NC} $check_name: 失败"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
}

echo "1. 检查Maven环境"
echo "----------------------------------------"
check_command "Maven版本检查" "mvn --version"
check_command "Java版本检查" "java -version"

echo ""
echo "2. 检查项目结构"
echo "----------------------------------------"
check_result "根POM文件存在" "$([ -f pom.xml ] && echo "存在" || echo "不存在")" "存在"
check_result "模块目录存在" "$([ -d medicine-module-medication ] && echo "存在" || echo "不存在")" "存在"
check_result "API模块存在" "$([ -d medicine-module-medication/medicine-module-medication-api ] && echo "存在" || echo "不存在")" "存在"
check_result "BIZ模块存在" "$([ -d medicine-module-medication/medicine-module-medication-biz ] && echo "存在" || echo "不存在")" "存在"

echo ""
echo "3. 单模块构建检查"
echo "----------------------------------------"
echo "清理项目..."
mvn clean -q

echo "编译API模块..."
check_command "API模块编译" "mvn compile -pl medicine-module-medication/medicine-module-medication-api -q"

echo "编译BIZ模块..."
check_command "BIZ模块编译" "mvn compile -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "测试API模块..."
check_command "API模块测试" "mvn test -pl medicine-module-medication/medicine-module-medication-api -q"

echo "测试BIZ模块..."
check_command "BIZ模块测试" "mvn test -pl medicine-module-medication/medicine-module-medication-biz -q"

echo ""
echo "4. 依赖模块构建检查"
echo "----------------------------------------"
echo "编译整个medication模块..."
check_command "medication模块编译" "mvn compile -pl medicine-module-medication -q"

echo "测试整个medication模块..."
check_command "medication模块测试" "mvn test -pl medicine-module-medication -q"

echo "打包整个medication模块..."
check_command "medication模块打包" "mvn package -pl medicine-module-medication -q"

echo ""
echo "5. 测试跳过构建检查"
echo "----------------------------------------"
echo "跳过测试编译..."
check_command "跳过测试编译" "mvn compile -DskipTests=true -q"

echo "跳过测试打包..."
check_command "跳过测试打包" "mvn package -DskipTests=true -q"

echo "使用profile跳过测试..."
check_command "Profile跳过测试" "mvn package -Pskip-all-tests -q"

echo ""
echo "6. 源码打包检查"
echo "----------------------------------------"
echo "生成源码包..."
check_command "源码包生成" "mvn source:jar -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "检查源码包文件..."
SOURCE_JAR=$(find . -name "*sources.jar" | head -1)
if [ -n "$SOURCE_JAR" ]; then
    check_result "源码包文件存在" "存在" "存在"
    check_result "源码包大小合理" "$([ $(stat -f%z "$SOURCE_JAR" 2>/dev/null || stat -c%s "$SOURCE_JAR" 2>/dev/null || echo "0") -gt 1000 ] && echo "合理" || echo "不合理")" "合理"
else
    check_result "源码包文件存在" "不存在" "存在"
fi

echo ""
echo "7. 依赖关系检查"
echo "----------------------------------------"
echo "检查依赖树..."
check_command "依赖树生成" "mvn dependency:tree -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "检查依赖分析..."
check_command "依赖分析" "mvn dependency:analyze -pl medicine-module-medication/medicine-module-medication-biz -q"

echo ""
echo "8. 质量门禁检查"
echo "----------------------------------------"
echo "Checkstyle检查..."
check_command "Checkstyle检查" "mvn checkstyle:check -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "SpotBugs检查..."
check_command "SpotBugs检查" "mvn spotbugs:check -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "PMD检查..."
check_command "PMD检查" "mvn pmd:check -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "Enforcer检查..."
check_command "Enforcer检查" "mvn enforcer:enforce -pl medicine-module-medication/medicine-module-medication-biz -q"

echo ""
echo "9. 代码覆盖率检查"
echo "----------------------------------------"
echo "JaCoCo检查..."
check_command "JaCoCo检查" "mvn jacoco:check -pl medicine-module-medication/medicine-module-medication-biz -q"

echo ""
echo "10. OWASP依赖检查"
echo "----------------------------------------"
echo "OWASP依赖检查..."
check_command "OWASP依赖检查" "mvn org.owasp:dependency-check-maven:check -pl medicine-module-medication/medicine-module-medication-biz -q"

echo ""
echo "=========================================="
echo "模块化构建检查完成"
echo "=========================================="
echo "总检查项: $TOTAL_CHECKS"
echo -e "通过: ${GREEN}$PASSED_CHECKS${NC}"
echo -e "失败: ${RED}$FAILED_CHECKS${NC}"

if [ $FAILED_CHECKS -eq 0 ]; then
    echo -e "${GREEN}所有检查通过！${NC}"
    exit 0
else
    echo -e "${RED}存在 $FAILED_CHECKS 个检查失败！${NC}"
    exit 1
fi
