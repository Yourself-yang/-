# 基于JavaWeb的在线招聘求职平台（仿Boss直聘）

## 项目简介
武汉学院《软件设计开发综合实训W》课程设计项目，前后端分离架构。

## 技术栈
- 前端：Vue 3 + Element Plus + Vite + Axios
- 后端：Spring Boot 2.7 + MyBatis-Plus + H2数据库
- 架构：前后端分离，按微服务领域模块化（可拆分）

## 目录结构
```
boss-recruit/
├── backend/                 # Spring Boot后端
│   ├── pom.xml
│   └── src/main/java/com/boss/recruit/
│       ├── RecruitApplication.java
│       ├── common/Result.java
│       ├── config/         # CORS、JWT配置
│       ├── controller/    # Auth/Job/Resume/Chat/Match
│       ├── entity/        # User/Job/Resume/Message
│       └── mapper/
├── frontend/                # Vue3前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/
│       ├── api/
│       └── views/          # Login/Home/JobDetail/Publish/Resume/Chat/Match
├── screenshots/             # 7张运行截图
└── docs/                    # 课程设计报告 + 答辩讲稿
```

## 本地运行
### 后端
```bash
cd backend
mvn spring-boot:run
# 启动在 http://localhost:9090
```

### 前端
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

## 演示账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 求职者 | zhangsan | 123456 |
| HR | hr_zhang | 123456 |

## 核心功能
1. 登录注册（求职者/HR双角色）
2. 首页岗位列表与搜索
3. 岗位详情与简历投递
4. HR发布岗位
5. 我的投递记录
6. 在线对话沟通
7. 智能匹配助手（基于技能标签算法）
