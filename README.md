# wealth-plan

A股智能分析平台初始工程（MVP 第一阶段）：
- Backend: Spring Boot 4.x（Java）
- Web: React + Vite + Ant Design
- 首个业务闭环：盯盘提醒规则创建与价格触发评估

## 目录
- `docs/requirements-and-architecture.md` 需求与架构方案
- `backend/` 后端服务（规则 API）
- `web/` Web 客户端（Ant Design 表单）

## 本地运行

### Backend
```bash
cd backend
mvn test
mvn spring-boot:run
```

### Web
```bash
cd web
npm install
npm run dev
```

打开 `http://localhost:5173`，创建盯盘规则。
