# Elasticsearch Docker 部署

本目录提供 Docker Compose 一键启动 Elasticsearch + Kibana。

## 快速启动

```bash
cd docker
docker-compose up -d
```

## 服务地址

- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601

## 关闭

```bash
docker-compose down
```

## 说明

- 单节点模式（开发环境）
- 关闭安全认证（xpack.security.enabled=false）
- 1GB 堆内存
- 数据持久化到 `es-data` 卷