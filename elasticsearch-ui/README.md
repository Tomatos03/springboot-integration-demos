# Elasticsearch UI

Elasticsearch 商品搜索前端，基于 Vue 3 + TypeScript + Vite 构建。提供关键词高亮搜索、自动补全建议、分页浏览等功能。

## 项目结构

```
src/
├── api/                        # API 接口层
│   ├── search.ts               #   高亮搜索接口
│   ├── autocomplete.ts         #   自动补全接口
│   └── index.ts
├── stores/                     # Pinia 状态管理
│   └── search.ts               #   搜索状态（关键词、结果、分页、建议列表）
├── views/
│   └── SearchView.vue          # 搜索主页面（输入框 + 建议下拉 + 结果列表 + 分页）
├── utils/
│   └── request.ts              # Axios 封装（baseURL、拦截器、统一错误处理）
├── types/
│   └── api.ts                  # 接口类型定义（ProductView、AutocompleteItem 等）
├── router/
│   └── index.ts                # 路由配置
├── App.vue
└── main.ts
```

## 快速开始

### 前置条件

- Node.js 18+
- 后端服务运行在 `http://localhost:8083`（可在 `src/utils/request.ts` 中修改）

### 启动

```sh
npm install
npm run dev
```

### 构建

```sh
npm run build
```
