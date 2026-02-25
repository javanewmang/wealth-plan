# wealth-plan

A股智能分析平台初始工程（MVP 第一阶段）：
- Backend: Spring Boot 4.x（Java 25）
- Web: React + Vite + Ant Design
- 首个业务闭环：盯盘提醒规则创建、价格触发评估、指令建规则

## 目录
- `docs/requirements-and-architecture.md` 需求与架构方案
- `backend/` 后端服务（规则 API + WebSocket）
- `web/` Web 客户端（Ant Design 表单）

## 已实现能力（当前）
- 手动创建盯盘规则（触发类型/阈值/渠道/静默时长）
- 规则编辑与删除
- OpenClaw 指令创建规则（自然语言 -> 规则）
- 行情触发评估（测试入口）
- 最近提醒历史查询（用于回看触发记录）
- PostgreSQL 持久化（规则与提醒历史）
- WebSocket 实时提醒推送（`/topic/alerts`）

## API 概览
- `POST /api/watch-rules` 创建规则
- `PUT /api/watch-rules/{id}` 编辑规则
- `GET /api/watch-rules` 查询规则
- `DELETE /api/watch-rules/{id}` 删除规则
- `POST /api/watch-rules/evaluate` 行情触发评估
- `GET /api/watch-rules/alerts?limit=20` 最近提醒历史
- `POST /api/agent/commands` 指令创建规则（OpenClaw入口）

## WebSocket
- 握手地址：`/ws`（SockJS）
- 订阅主题：`/topic/alerts`

## 本地运行

### Backend
```bash
cd backend
mvn test
mvn spring-boot:run
```

可通过以下配置修改默认行为：
- `app.cors.allowed-origin`：前端跨域来源
- `spring.datasource.url`：PostgreSQL 连接串（默认 `jdbc:postgresql://localhost:5432/wealth_plan`）
- `spring.datasource.username` / `spring.datasource.password`

### Web
```bash
cd web
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

打开 `http://localhost:5173`，可直接创建/编辑/删除规则并进行触发评估，实时查看WebSocket提醒。
