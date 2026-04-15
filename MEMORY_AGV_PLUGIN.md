# AGV 数据采集插件 - 开发者记忆文档

## 项目信息

- **GitHub**：`https://github.com/a452065754/agv-finereport-plugin.git`
- **本地源码**：`C:\Users\xudongdong\`
- **本地帆软**：`D:\FineReport_11.0\webapps\webroot\`
- **服务器帆软**：`D:\Tomcat\webapps\webroot\`

## 核心文件

| 文件 | 说明 |
|------|------|
| `AgvDataSource.java` | 主程序，继承 AbstractTableData |
| `agv-config.properties` | AGV 小车 URL 配置文件 |
| `compile8.ps1` | JDK 8 编译脚本 |
| `AGV数据采集插件说明文档.md` | 用户使用文档 |

## 采集指标

| 字段 | 说明 |
|------|------|
| 车牌号 | AGV 唯一标识 |
| 当前状态 | online / offline / manual |
| 剩余电量 | 电量百分比 |
| 正在执行任务 | 任务类型 / 任务ID |
| 任务状态 | 执行失败 / 执行中 / 已完成 |

---

## 踩坑记录

### JDK 版本问题（最重要）

**现象**：`UnsupportedClassVersionError: class file version 68.0 only recognizes up to 52.0`

**原因**：帆软 FineReport 只支持 JDK 8（字节码版本 52.0）。本机系统 JDK 24，编译出来的 class 版本是 68.0，帆软加载不了。

**教训**：`-source 8 -target 8` 对 JDK 11 及以上无效！JDK 11/17/21/24 总是生成自身版本对应的字节码，不会因为 -source/-target 降级。

**解决方案**：必须安装 JDK 8，用 JDK 8 的 javac 编译。

- JDK 8 下载：[Adoptium Temurin 8u432](https://adoptium.net/temurin/releases/?version=8&os=windows)
- 本机 JDK 8 安装路径：`C:\Users\xudongdong\jdk8u432-b06`
- 编译脚本：`C:\Users\xudongdong\compile8.ps1`（用 Start-Process 避免 Invoke-Expression 安全策略拦截）
- 字节码版本验证：class 文件头两个字节是 `0xCA 0xFE`（魔数），后面两个字节是主版本号（52 = JDK 8）

### classpath 加载问题

**现象**：配置文件路径写成绝对路径，部署到其他机器就找不到文件。

**解决方案**：用 `Class.getResourceAsStream("agv-config.properties")` 从 classpath 加载，配置文件放在 `WEB-INF/classes/` 目录下。

---

## 部署路径

**本地帆软**：
```
D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\com\fr\data\AgvDataSource.class
D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\agv-config.properties
```

**服务器帆软**：
```
D:\Tomcat\webapps\webroot\WEB-INF\classes\com\fr\data\AgvDataSource.class
D:\Tomcat\webapps\webroot\WEB-INF\classes\agv-config.properties
```

---

## GitHub 更新流程

```cmd
# 1. 查看改动
git status

# 2. 添加要提交的文件
git add AgvDataSource.java AGV数据采集插件说明文档.md MEMORY_AGV_PLUGIN.md

# 3. 提交（写清楚改动内容）
git commit -m "fix: 修复内容描述"

# 4. 推送到 GitHub
git push origin main
```

---

## 修改记录

- 2026-04-14：初始版本，支持 3 个 AGV 小车基本状态采集，修复 JDK 版本兼容问题
- 2026-04-15：更新完整使用文档（含服务器部署步骤），MEMORY 文档补充 JDK 版本踩坑记录
