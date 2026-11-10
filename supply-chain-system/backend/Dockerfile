# ============ 构建阶段 (Maven + JDK 17) ============
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 配置国内 Maven 镜像加速下载
RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd"> \
    <mirrors><mirror><id>aliyun</id><mirrorOf>*</mirrorOf> \
    <url>https://maven.aliyun.com/repository/public</url></mirror></mirrors> \
    </settings>' > /root/.m2/settings.xml

# 先拷贝所有 POM 以利用 Docker 层缓存
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY purchase-service/pom.xml purchase-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml

# 预下载所有模块依赖（这一层只要 POM 不变就会缓存）
RUN mvn -B -e -q dependency:go-offline -pl common,gateway,user-service,product-service,purchase-service,inventory-service,order-service -am 2>&1 | tail -20

# 拷贝全部源码并一次性构建所有模块
COPY common/ common/
COPY gateway/ gateway/
COPY user-service/ user-service/
COPY product-service/ product-service/
COPY purchase-service/ purchase-service/
COPY inventory-service/ inventory-service/
COPY order-service/ order-service/

RUN mvn -B -e clean package -DskipTests

# ============ 运行阶段 (JRE 17 alpine) ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ARG APP_MODULE
COPY --from=builder /build/${APP_MODULE}/target/${APP_MODULE}.jar app.jar

ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata curl && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

EXPOSE 8080
ENTRYPOINT ["sh","-c","java -Xms256m -Xmx512m -jar app.jar --spring.profiles.active=docker"]
