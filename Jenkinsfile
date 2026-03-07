pipeline {
    agent any
    
    environment {
        // 项目配置
        PROJECT_NAME = 'medicinal-his-delivery'
        JAVA_VERSION = '11'
        MAVEN_VERSION = '3.8.6'
        
        // 质量门禁阈值
        CODE_COVERAGE_THRESHOLD = '80'
        SPOTBUGS_THRESHOLD = 'Low'
        PMD_THRESHOLD = 'Low'
        
        // 构建产物
        ARTIFACT_DIR = 'target'
        REPORTS_DIR = 'reports'
    }
    
    options {
        // 构建历史保留
        buildDiscarder(logRotator(numToKeepStr: '10'))
        
        // 超时设置
        timeout(time: 30, unit: 'MINUTES')
        
        // 并发构建
        disableConcurrentBuilds()
        
        // 参数化构建
        parameters {
            choice(
                name: 'BUILD_PROFILE',
                choices: ['default', 'skip-tests', 'performance'],
                description: '构建配置'
            )
            booleanParam(
                name: 'SKIP_STATIC_CHECKS',
                defaultValue: false,
                description: '跳过静态检查'
            )
            booleanParam(
                name: 'SKIP_INTEGRATION_TESTS',
                defaultValue: false,
                description: '跳过集成测试'
            )
        }
    }
    
    stages {
        stage('环境准备') {
            steps {
                script {
                    echo "=========================================="
                    echo "开始质量门禁构建流程"
                    echo "=========================================="
                    echo "项目: ${PROJECT_NAME}"
                    echo "Java版本: ${JAVA_VERSION}"
                    echo "Maven版本: ${MAVEN_VERSION}"
                    echo "构建配置: ${params.BUILD_PROFILE}"
                    echo "=========================================="
                }
                
                // 清理工作空间
                cleanWs()
                
                // 检查工具版本
                sh '''
                    echo "检查Java版本..."
                    java -version
                    
                    echo "检查Maven版本..."
                    mvn --version
                    
                    echo "检查Git版本..."
                    git --version
                '''
            }
        }
        
        stage('代码检出') {
            steps {
                script {
                    echo "=========================================="
                    echo "代码检出阶段"
                    echo "=========================================="
                }
                
                // 检出代码
                checkout scm
                
                // 显示提交信息
                sh '''
                    echo "当前分支: $(git branch --show-current)"
                    echo "最新提交: $(git log -1 --pretty=format:"%h - %an, %ar : %s")"
                    echo "提交时间: $(git log -1 --pretty=format:"%ci")"
                '''
            }
        }
        
        stage('依赖下载') {
            steps {
                script {
                    echo "=========================================="
                    echo "依赖下载阶段"
                    echo "=========================================="
                }
                
                // 下载依赖
                dir('backend') {
                    sh '''
                        echo "下载项目依赖..."
                        mvn dependency:resolve -q
                        
                        echo "下载测试依赖..."
                        mvn dependency:resolve-sources -q
                        
                        echo "验证依赖..."
                        mvn dependency:analyze -q
                    '''
                }
            }
        }
        
        stage('编译检查') {
            steps {
                script {
                    echo "=========================================="
                    echo "编译检查阶段"
                    echo "=========================================="
                }
                
                // 清理编译
                dir('backend') {
                    sh 'mvn clean -q'
                    
                    // 编译检查
                    sh '''
                        echo "编译项目..."
                        mvn compile -q
                        
                        echo "编译测试代码..."
                        mvn test-compile -q
                        
                        echo "验证编译结果..."
                        find . -name "*.class" | wc -l
                    '''
                }
            }
        }
        
        stage('静态检查') {
            when {
                not { params.SKIP_STATIC_CHECKS }
            }
            parallel {
                stage('Checkstyle检查') {
                    steps {
                        script {
                            echo "=========================================="
                            echo "Checkstyle代码规范检查"
                            echo "=========================================="
                        }
                        
                        dir('backend') {
                            sh '''
                                echo "执行Checkstyle检查..."
                                mvn checkstyle:check -pl medicine-module-medication/medicine-module-medication-biz -q
                                
                                echo "生成Checkstyle报告..."
                                mvn checkstyle:checkstyle -pl medicine-module-medication/medicine-module-medication-biz -q
                            '''
                        }
                        
                        // 发布Checkstyle报告
                        publishHTML([
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'backend/medicine-module-medication/medicine-module-medication-biz/target/site',
                            reportFiles: 'checkstyle.html',
                            reportName: 'Checkstyle报告'
                        ])
                    }
                }
                
                stage('SpotBugs检查') {
                    steps {
                        script {
                            echo "=========================================="
                            echo "SpotBugs代码质量检查"
                            echo "=========================================="
                        }
                        
                        dir('backend') {
                            sh '''
                                echo "执行SpotBugs检查..."
                                mvn spotbugs:check -pl medicine-module-medication/medicine-module-medication-biz -q
                                
                                echo "生成SpotBugs报告..."
                                mvn spotbugs:spotbugs -pl medicine-module-medication/medicine-module-medication-biz -q
                            '''
                        }
                        
                        // 发布SpotBugs报告
                        publishHTML([
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'backend/medicine-module-medication/medicine-module-medication-biz/target/site',
                            reportFiles: 'spotbugs.html',
                            reportName: 'SpotBugs报告'
                        ])
                    }
                }
                
                stage('PMD检查') {
                    steps {
                        script {
                            echo "=========================================="
                            echo "PMD代码质量分析"
                            echo "=========================================="
                        }
                        
                        dir('backend') {
                            sh '''
                                echo "执行PMD检查..."
                                mvn pmd:check -pl medicine-module-medication/medicine-module-medication-biz -q
                                
                                echo "生成PMD报告..."
                                mvn pmd:pmd -pl medicine-module-medication/medicine-module-medication-biz -q
                            '''
                        }
                        
                        // 发布PMD报告
                        publishHTML([
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'backend/medicine-module-medication/medicine-module-medication-biz/target/site',
                            reportFiles: 'pmd.html',
                            reportName: 'PMD报告'
                        ])
                    }
                }
                
                stage('Enforcer检查') {
                    steps {
                        script {
                            echo "=========================================="
                            echo "Maven Enforcer规则检查"
                            echo "=========================================="
                        }
                        
                        dir('backend') {
                            sh '''
                                echo "执行Enforcer检查..."
                                mvn enforcer:enforce -pl medicine-module-medication/medicine-module-medication-biz -q
                            '''
                        }
                    }
                }
            }
        }
        
        stage('单元测试') {
            steps {
                script {
                    echo "=========================================="
                    echo "单元测试阶段"
                    echo "=========================================="
                }
                
                dir('backend') {
                    sh '''
                        echo "执行单元测试..."
                        mvn test -pl medicine-module-medication/medicine-module-medication-biz -q
                        
                        echo "生成测试报告..."
                        mvn surefire-report:report -pl medicine-module-medication/medicine-module-medication-biz -q
                    '''
                }
                
                // 发布测试报告
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'backend/medicine-module-medication/medicine-module-medication-biz/target/site',
                    reportFiles: 'surefire-report.html',
                    reportName: '单元测试报告'
                ])
            }
        }
        
        stage('代码覆盖率') {
            steps {
                script {
                    echo "=========================================="
                    echo "代码覆盖率检查"
                    echo "=========================================="
                }
                
                dir('backend') {
                    sh '''
                        echo "生成代码覆盖率报告..."
                        mvn jacoco:report -pl medicine-module-medication/medicine-module-medication-biz -q
                        
                        echo "检查代码覆盖率阈值..."
                        mvn jacoco:check -pl medicine-module-medication/medicine-module-medication-biz -q
                    '''
                }
                
                // 发布代码覆盖率报告
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'backend/medicine-module-medication/medicine-module-medication-biz/target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: '代码覆盖率报告'
                ])
            }
        }
        
        stage('集成测试') {
            when {
                not { params.SKIP_INTEGRATION_TESTS }
            }
            steps {
                script {
                    echo "=========================================="
                    echo "集成测试阶段"
                    echo "=========================================="
                }
                
                dir('backend') {
                    sh '''
                        echo "执行集成测试..."
                        mvn verify -pl medicine-module-medication/medicine-module-medication-biz -q
                        
                        echo "生成集成测试报告..."
                        mvn failsafe-report:report -pl medicine-module-medication/medicine-module-medication-biz -q
                    '''
                }
                
                // 发布集成测试报告
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'backend/medicine-module-medication/medicine-module-medication-biz/target/site',
                    reportFiles: 'failsafe-report.html',
                    reportName: '集成测试报告'
                ])
            }
        }
        
        stage('安全扫描') {
            steps {
                script {
                    echo "=========================================="
                    echo "安全扫描阶段"
                    echo "=========================================="
                }
                
                dir('backend') {
                    sh '''
                        echo "执行OWASP依赖检查..."
                        mvn org.owasp:dependency-check-maven:check -pl medicine-module-medication/medicine-module-medication-biz -q
                    '''
                }
                
                // 发布安全扫描报告
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'backend/medicine-module-medication/medicine-module-medication-biz/target',
                    reportFiles: 'dependency-check-report.html',
                    reportName: '安全扫描报告'
                ])
            }
        }
        
        stage('构建产物') {
            steps {
                script {
                    echo "=========================================="
                    echo "构建产物阶段"
                    echo "=========================================="
                }
                
                dir('backend') {
                    sh '''
                        echo "打包项目..."
                        mvn package -pl medicine-module-medication/medicine-module-medication-biz -q
                        
                        echo "生成源码包..."
                        mvn source:jar -pl medicine-module-medication/medicine-module-medication-biz -q
                        
                        echo "生成Javadoc..."
                        mvn javadoc:jar -pl medicine-module-medication/medicine-module-medication-biz -q
                        
                        echo "列出构建产物..."
                        find medicine-module-medication/medicine-module-medication-biz/target -name "*.jar" -type f
                    '''
                }
                
                // 归档构建产物
                archiveArtifacts artifacts: 'backend/medicine-module-medication/medicine-module-medication-biz/target/*.jar', fingerprint: true
            }
        }
        
        stage('质量门禁') {
            steps {
                script {
                    echo "=========================================="
                    echo "质量门禁检查"
                    echo "=========================================="
                    
                    // 检查代码覆盖率
                    def coverageFile = 'backend/medicine-module-medication/medicine-module-medication-biz/target/site/jacoco/index.html'
                    if (fileExists(coverageFile)) {
                        echo "代码覆盖率检查通过"
                    } else {
                        error "代码覆盖率检查失败"
                    }
                    
                    // 检查静态检查结果
                    def checkstyleFile = 'backend/medicine-module-medication/medicine-module-medication-biz/target/site/checkstyle.html'
                    if (fileExists(checkstyleFile)) {
                        echo "Checkstyle检查通过"
                    } else {
                        error "Checkstyle检查失败"
                    }
                    
                    // 检查测试结果
                    def testFile = 'backend/medicine-module-medication/medicine-module-medication-biz/target/site/surefire-report.html'
                    if (fileExists(testFile)) {
                        echo "单元测试检查通过"
                    } else {
                        error "单元测试检查失败"
                    }
                    
                    echo "所有质量门禁检查通过！"
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "=========================================="
                echo "构建完成"
                echo "=========================================="
                echo "构建状态: ${currentBuild.result ?: 'SUCCESS'}"
                echo "构建时间: ${currentBuild.durationString}"
                echo "构建编号: ${currentBuild.number}"
                echo "=========================================="
            }
            
            // 清理工作空间
            cleanWs()
        }
        
        success {
            script {
                echo "构建成功！所有质量门禁检查通过。"
            }
        }
        
        failure {
            script {
                echo "构建失败！请检查质量门禁检查结果。"
            }
        }
        
        unstable {
            script {
                echo "构建不稳定！存在质量门禁警告。"
            }
        }
    }
}
