# excel-to-pdf 可选容器化：Maven 构建 + LibreOffice(含 CJK 字体) 运行镜像
#
# 构建上下文需为仓库根目录（docker compose 已配置好 context: ../..）。
# 镜像较大（LibreOffice 本体 ~1GB 级），仅作为「不想在宿主装 LibreOffice」时的备选。

# --- 阶段一：构建可执行 jar ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# 复制整个仓库：-pl -am 需要解析 reactor 结构
COPY . .

# 只构建 excel-to-pdf 及其兄弟依赖（fesode-excel），跳过测试（部分依赖外部中间件）
RUN mvn -q -pl excel-to-pdf -am -DskipTests package

# --- 阶段二：LibreOffice 运行环境 ---
FROM debian:bookworm-slim

# libreoffice-calc 提供 soffice；fonts-noto-cjk 保证 PDF 中文不乱码；openjdk 17 运行 jar
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libreoffice-calc \
        fonts-noto-cjk \
        openjdk-17-jre-headless \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/excel-to-pdf/target/excel-to-pdf-1.0-SNAPSHOT.jar /app/app.jar

WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
