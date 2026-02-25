# wealth-plan

A股智能分析平台初始工程（MVP 第一阶段）：
- Backend: Spring Boot 4.x（Java）
- Web: React + Vite + Ant Design
- 首个业务闭环：盯盘提醒规则创建、价格触发评估、指令建规则

## 目录
- `docs/requirements-and-architecture.md` 需求与架构方案
- `backend/` 后端服务（规则 API）
- `web/` Web 客户端（Ant Design 表单）

## API 概览
- `POST /api/watch-rules` 创建规则
- `GET /api/watch-rules` 查询规则
- `DELETE /api/watch-rules/{id}` 删除规则
- `POST /api/watch-rules/evaluate` 行情触发评估
- `POST /api/agent/commands` 指令创建规则（OpenClaw入口）

## 本地运行

### Backend
```bash
cd backend
mvn test
mvn spring-boot:run
```

可通过 `app.cors.allowed-origin` 配置前端来源。

### Web
```bash
cd web
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

打开 `http://localhost:5173`，创建盯盘规则。
