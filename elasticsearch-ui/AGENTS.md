# AGENTS.md

## 项目概述

elasticsearch-ui 是 Spring Boot + Elasticsearch 集成演示项目的前端部分，基于 Vue 3 + TypeScript + Vite。

- **包管理器**: pnpm
- **前端框架**: Vue 3.5
- **构建工具**: Vite 7.0
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios

## 命令

```bash
# 开发服务器
pnpm dev

# 构建生产版本
pnpm build

# 预览构建结果
pnpm preview

# 类型检查
pnpm type-check

# 代码格式化
pnpm format
```

## 运行

```bash
pnpm dev
```

访问 http://localhost:5173

## 约束

- 项目统一使用 `src/utils/request.ts` 中的 `http` 工具类发送请求
- 业务异常和请求异常已在工具类中统一处理，不需要额外捕获
- API 请求示例：
  ```ts
  import http from '@/utils/request'

  // GET 请求
  const data = await http.get<User[]>('/api/users')

  // POST 请求
  const result = await http.post('/api/users', { name: 'Tom' })

  // PUT 请求
  await http.put('/api/users/1', { name: 'Tom' })

  // DELETE 请求
  await http.delete('/api/users/1')
  ```