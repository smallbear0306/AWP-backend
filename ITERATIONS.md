# AWP 迭代记录

> 全栈记账网页（前端 AWP-frontend / 后端 AWP-backend）的迭代历程。

---

## 迭代一：公网可访问（本地部署 + 内网穿透）✅ 已完成（2026-06-03）

**核心目标**：让其他设备在不同网络（非局域网）下，通过网址访问到当前运行的前后端。

**产出**
- 架构：后端 Spring Boot 单端口 `8080` 同时托管前端页面与 `/api` 接口（同源、无跨域），对外只需穿透一个端口。
  - `SpaForwardController`：SPA history 路由兜底（刷新 `/home` 等返回 `index.html`）。
  - 前端构建产物拷入 `src/main/resources/static/`（gitignore，不入库）。
- `deploy-local.ps1`：一键 构建前端 → 拷入 static → 打包 jar → 运行。
- `DEPLOY.md`：cpolar 内网穿透步骤 + 防火墙说明 + 排错。
- 穿透方案：cpolar（用户处于运营商 CGNAT 内网且挂 VPN，IP 直连不可行）。

**验收结果**：通过 cpolar 公网网址（`https://<随机>.cpolar.top`）实测——UI 首页、SPA 刷新兜底、`/api` 登录、鉴权取统计数据全部正常返回。目标达成。

**遗留/后续**：不同客户端（手机/平板）的前端 UI 自适应展示，留待后续迭代。免费版 cpolar 网址重启会变，如需固定网址可配保留二级域名 + 开机自启。

---
