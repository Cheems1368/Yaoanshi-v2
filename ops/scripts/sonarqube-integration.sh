#!/bin/bash

# SonarQube集成脚本
# 用于配置SonarQube质量门禁

set -e

echo "=========================================="
echo "开始SonarQube集成配置"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# SonarQube配置
SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"
SONAR_TOKEN="${SONAR_TOKEN:-}"
SONAR_PROJECT_KEY="medicinal-his-delivery"
SONAR_PROJECT_NAME="Medicinal HIS Delivery System"

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

echo "1. 检查SonarQube环境"
echo "----------------------------------------"
check_command "SonarQube服务连接" "curl -s $SONAR_HOST_URL/api/system/status"

if [ -z "$SONAR_TOKEN" ]; then
    echo -e "${YELLOW}⚠${NC} SonarQube Token未设置，请设置SONAR_TOKEN环境变量"
    echo "  示例: export SONAR_TOKEN=your_token_here"
fi

echo ""
echo "2. 配置项目属性"
echo "----------------------------------------"
check_result "项目配置文件存在" "$([ -f sonar-project.properties ] && echo "存在" || echo "不存在")" "存在"

if [ -f sonar-project.properties ]; then
    echo "项目配置内容:"
    cat sonar-project.properties
    echo ""
fi

echo ""
echo "3. 执行代码分析"
echo "----------------------------------------"
echo "清理项目..."
mvn clean -q

echo "编译项目..."
check_command "项目编译" "mvn compile -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "执行测试..."
check_command "单元测试" "mvn test -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "生成代码覆盖率报告..."
check_command "代码覆盖率报告" "mvn jacoco:report -pl medicine-module-medication/medicine-module-medication-biz -q"

echo ""
echo "4. 执行SonarQube分析"
echo "----------------------------------------"
if [ -n "$SONAR_TOKEN" ]; then
    echo "执行SonarQube代码分析..."
    check_command "SonarQube分析" "mvn sonar:sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_TOKEN -Dsonar.projectKey=$SONAR_PROJECT_KEY -Dsonar.projectName='$SONAR_PROJECT_NAME' -pl medicine-module-medication/medicine-module-medication-biz -q"
else
    echo "跳过SonarQube分析（未设置Token）"
fi

echo ""
echo "5. 质量门禁检查"
echo "----------------------------------------"
echo "检查代码覆盖率阈值..."
COVERAGE_FILE="medicine-module-medication/medicine-module-medication-biz/target/site/jacoco/jacoco.xml"
if [ -f "$COVERAGE_FILE" ]; then
    COVERAGE_RATE=$(grep -o 'line-rate="[^"]*"' "$COVERAGE_FILE" | head -1 | sed 's/line-rate="\([^"]*\)"/\1/' | awk '{print $1*100}')
    if (( $(echo "$COVERAGE_RATE >= 80" | bc -l) )); then
        check_result "代码覆盖率检查" "通过($COVERAGE_RATE%)" "通过"
    else
        check_result "代码覆盖率检查" "失败($COVERAGE_RATE%)" "通过"
    fi
else
    check_result "代码覆盖率检查" "失败(文件不存在)" "通过"
fi

echo ""
echo "6. 生成质量报告"
echo "----------------------------------------"
echo "生成Checkstyle报告..."
check_command "Checkstyle报告" "mvn checkstyle:checkstyle -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "生成SpotBugs报告..."
check_command "SpotBugs报告" "mvn spotbugs:spotbugs -pl medicine-module-medication/medicine-module-medication-biz -q"

echo "生成PMD报告..."
check_command "PMD报告" "mvn pmd:pmd -pl medicine-module-medication/medicine-module-medication-biz -q"

echo ""
echo "7. 质量指标汇总"
echo "----------------------------------------"
echo "代码覆盖率: ${COVERAGE_RATE:-N/A}%"
echo "静态检查: Checkstyle, SpotBugs, PMD"
echo "质量门禁: 已配置"
echo "SonarQube: $([ -n "$SONAR_TOKEN" ] && echo "已集成" || echo "待配置")"

echo ""
echo "=========================================="
echo "SonarQube集成配置完成"
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
