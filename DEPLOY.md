# AWP 部署与公网访问（迭代一）

目标：本地部署后端（同时托管前端），并通过内网穿透让**不同网络的设备用网址访问**。

架构：后端 Spring Boot 单端口 `8080` 同时提供前端页面（UI）与 `/api` 接口，前端是同源调用，无跨域。对外只需穿透一个端口。

---

## 一、本地部署（构建 + 运行）

前置：Node、Maven、JDK17+，MySQL 的 `awp` 库在运行（密码写在 `src/main/resources/application-local.yml`，不入库）。

一键脚本（在 `AWP-backend` 目录）：

```powershell
.\deploy-local.ps1
```

脚本做四件事：构建前端 → 把 `dist` 拷进后端 `static/` → 打包 jar → `java -jar` 启动。
启动后本机访问 <http://localhost:8080> 应能看到登录页，用 `alice / 123456` 登录。

> 改了前端代码后需要重新跑脚本（重新构建+打包）才会生效。

---

## 二、内网穿透（cpolar，让外网访问）

> 为什么不用 IP 直连：本机是内网地址（192.168.x.x），家庭宽带是运营商大局域网(CGNAT)，没有独立公网 IPv4，外部无法主动连入；因此用"隧道"由客户端主动外连。

1. 注册账号：<https://www.cpolar.com/>
2. 下载 Windows 客户端并解压（得到 `cpolar.exe`）。
3. 在 cpolar 后台「验证」页复制 authtoken，配置一次：
   ```powershell
   .\cpolar.exe authtoken <你的token>
   ```
4. 启动隧道（指向本地 8080）：
   ```powershell
   .\cpolar.exe http 8080
   ```
5. 终端会显示一个公网网址，形如：
   ```
   https://xxxxxxxx.r3.cpolar.cn
   ```
   把这个网址发给任何设备（任意网络）即可访问 AWP。`/api` 会自动随同一域名走，无需额外配置。

注意：
- **免费版网址每次重启会变**；想要固定网址需要 cpolar 的保留二级域名/付费套餐。
- 你本机挂着代理(VPN，TUN 网卡 198.18.0.1)。cpolar 是主动外连，通常不受影响；若隧道连不上，临时关闭代理再试。
- 隧道运行期间，本地 `java -jar`（8080）必须一直开着。

---

## 三、防火墙说明

**用 cpolar 隧道：Windows 防火墙无需任何改动**——隧道客户端是出站连接，默认放行，不需要开放入站端口、不需要改路由器。

仅当你想让**同一局域网**的设备直连（`http://192.168.1.84:8080`）时，才需要放行入站 8080（管理员 PowerShell）：

```powershell
New-NetFirewallRule -DisplayName "AWP 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

端口转发 + 公网 IP 的方案在国内 CGNAT 下基本不可行，已放弃。

---

## 四、排错

| 现象 | 处理 |
| --- | --- |
| 启动报 `Port 8080 was already in use` | 关掉占用进程：`Get-NetTCPConnection -LocalPort 8080` 查 PID 后 `taskkill /F /PID <pid>` |
| 页面打开是 404/白屏 | 确认 `deploy-local.ps1` 跑完（`static/` 下有 `index.html`），重新打包 |
| 接口 401/连不上数据库 | 确认 MySQL 在跑、`application-local.yml` 密码正确 |
| cpolar 网址打不开 | 确认本地 8080 在跑；隧道仍连着；必要时关代理重试 |
