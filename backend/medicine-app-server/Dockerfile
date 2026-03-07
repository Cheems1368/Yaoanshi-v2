## 1. 基础镜像修改
## 原 openjdk 镜像已弃用。根据你的注释建议，替换为官方推荐的 Eclipse Temurin (基于 OpenJDK)
## 使用 11-jre 版本，体积适中且运行稳定
FROM eclipse-temurin:11-jre

## 2. 目录优化
## WORKDIR 指令如果目录不存在会自动创建，不需要单独写 RUN mkdir
WORKDIR /medicine-app-server

## 3. 复制文件
## 将后端项目的 Jar 文件，复制到镜像中
COPY ./target/medicine-app-server.jar app.jar

## 4. 环境设置
## 设置 TZ 时区
ENV TZ=Asia/Shanghai
## 设置 JAVA_OPTS 环境变量
ENV JAVA_OPTS="-Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
## 应用参数
ENV ARGS=""

## 5. 端口与启动
## 暴露后端项目的 48080 端口
EXPOSE 48081

## 启动后端项目
## 注意：使用 shell 格式 (不带 []) 才能让变量 ${JAVA_OPTS} 正确展开
CMD java ${JAVA_OPTS} -jar app.jar $ARGS