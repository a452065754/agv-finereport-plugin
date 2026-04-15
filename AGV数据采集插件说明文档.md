# AGV 小车面板数据采集插件

## 一、目的

从 AGV 小车管理系统的 Web 面板页面实时采集数据，用于帆软（FineReport）报表展示。

### 采集指标
| 指标名称 | 说明 |
|---------|------|
| 车牌号 | AGV 唯一标识 |
| 当前状态 | online / offline / manual 等 |
| 剩余电量 | 电量百分比 |
| 正在执行任务 | 任务类型 / 任务ID |
| 任务状态 | 执行失败 / 执行中 / 已完成 |

---

## 二、技术方案

### 2.1 架构

```
用户请求报表 → 帆软调用 AgvDataSource → HTTP 请求抓取 AGV 面板 → 正则解析 HTML → 返回数据
```

### 2.2 核心文件

| 文件 | 说明 |
|------|------|
| `AgvDataSource.java` | 主程序，继承 `AbstractTableData` |
| `agv-config.properties` | AGV 小车 URL 配置文件 |
| `compile8.ps1` | JDK 8 编译脚本（Windows） |

### 2.3 数据格式

```
| 车牌号 | 指标名称 | 指标值 | 采集时间 |
|--------|----------|--------|----------|
| XQC-1 | 当前状态 | offline | 2026-04-14 15:41:00 |
| XQC-1 | 剩余电量 | 63 | 2026-04-14 15:41:00 |
```

---

## 三、部署

### 3.1 文件清单

| 文件 | 目标路径 | 说明 |
|------|---------|------|
| `AgvDataSource.class` | `帆软\WEB-INF\classes\com\fr\data\` | 编译后的 Java 类 |
| `agv-config.properties` | `帆软\WEB-INF\classes\` | 配置文件 |

**本地帆软路径**：`D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\`
**服务器帆软路径**：`D:\Tomcat\webapps\webroot\WEB-INF\classes\`

### 3.2 编译要求

⚠️ **JDK 版本必须一致！**

- 帆软 FineReport 只支持 **JDK 8**（字节码版本 52.0）
- 如果系统 JDK 版本高于 8（如 JDK 11/17/21/24），用 `-source 8 -target 8` 编译**无效**，JDK 仍会生成高版本字节码
- **解决方案**：安装 JDK 8，用 JDK 8 的 `javac` 编译

推荐下载：[Adoptium Temurin 8u432](https://adoptium.net/temurin/releases/?version=8&os=windows)

### 3.3 编译步骤（Windows）

**方式一：使用编译脚本（推荐）**
```powershell
# 1. 确保 JDK 8 解压到 C:\Users\xudongdong\jdk8u432-b06
# 2. 直接运行编译脚本
.\compile8.ps1
```

**方式二：手动编译**
```cmd
# 进入源码目录
cd C:\Users\xudongdong

# 用 JDK 8 编译（路径替换为实际 JDK 8 安装位置）
C:\Users\xudongdong\jdk8u432-b06\bin\javac -encoding UTF-8 -cp "D:\FineReport_11.0\webapps\webroot\WEB-INF\lib\*" -d . AgvDataSource.java
```

### 3.4 部署到本地帆软

```cmd
# 复制 class 文件
copy C:\Users\xudongdong\com\fr\data\AgvDataSource.class D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\com\fr\data\

# 复制配置文件
copy C:\Users\xudongdong\agv-config.properties D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\
```

**重启帆软设计器**后生效。

### 3.5 部署到服务器帆软

```cmd
# 克隆代码
git clone https://github.com/a452065754/agv-finereport-plugin.git
cd agv-finereport-plugin

# 服务器编译（如服务器没有 JDK 8，先下载安装）
<JDK8路径>\bin\javac -encoding UTF-8 -cp "D:\Tomcat\webapps\webroot\WEB-INF\lib\*" -d . AgvDataSource.java

# 复制到帆软目录
copy com\fr\data\AgvDataSource.class D:\Tomcat\webapps\webroot\WEB-INF\classes\com\fr\data\
copy agv-config.properties D:\Tomcat\webapps\webroot\WEB-INF\classes\
```

**重启服务器帆软服务（Tomcat）**后生效。

### 3.6 配置文件格式

```properties
# AGV 小车 URL 配置
# 格式: 车牌号.url=URL地址

XQC-1.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQC-1
XQE-122-1.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQE-122-1
XQE-122-2.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQE-122-2
```

### 3.7 在帆软中使用

1. 打开帆软设计器
2. 新建数据集 → 程序数据集
3. 输入类名：`com.fr.data.AgvDataSource`
4. 确定，拖拽到报表中使用

---

## 四、修改

### 4.1 添加/删除 AGV

编辑 `agv-config.properties`，添加或删除对应行：

```properties
# 添加新车
NEW-CAR.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=NEW-CAR

# 删除某车：直接删除对应行
# XQC-1.url=...
```

**本地**：重启帆软设计器生效
**服务器**：重启帆软服务生效

### 4.2 修改正则表达式

如果 AGV 面板 HTML 结构变化，修改 `AgvDataSource.java` 中的正则表达式，重新编译部署。

---

## 五、常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| `UnsupportedClassVersionError: class file version 68.0 only recognizes up to 52.0` | JDK 版本不兼容，帆软只认 JDK 8 | 安装 JDK 8，用 JDK 8 编译 |
| 类加载失败 | class 未放到正确目录 | 确认 `WEB-INF\classes\com\fr\data\` 路径 |
| 配置文件找不到 | 配置未放在 `WEB-INF\classes\` 下 | 使用 classpath 方式加载 `getResourceAsStream` |
| HTTP 连接失败 | 网络不通 / AGV 面板未启动 | 检查网络和 URL |
| 数据为空 | 正则表达式不匹配 | 检查 HTML 结构和正则 |

### 5.1 已知限制

- **任务状态（jobstatus）**：由 JavaScript 动态渲染，HTTP 请求无法获取原始值。代码通过检测 HTML 中的"（执行失败）"标记推断状态。
- **内网访问**：AGV 面板在内网（192.168.x.x），部署帆软的机器必须能访问该网络。

---

## 六、调试

### 6.1 查看日志

日志位置：`帆软安装目录\logs\fanruan.log`

### 6.2 添加调试输出

```java
System.out.println("[DEBUG] 车牌号: " + carName);
```

---

## 七、文件结构

```
本地开发：
C:\Users\xudongdong\
├── AgvDataSource.java
├── agv-config.properties
├── compile8.ps1              ← JDK 8 编译脚本
├── MEMORY_AGV_PLUGIN.md       ← 开发者记忆文档
└── com\fr\data\
    └── AgvDataSource.class

本地帆软部署：
D:\FineReport_11.0\
└── webapps\webroot\WEB-INF\
    ├── classes\
    │   ├── agv-config.properties
    │   └── com\fr\data\
    │       └── AgvDataSource.class

服务器帆软部署：
D:\Tomcat\
└── webapps\webroot\WEB-INF\
    ├── classes\
    │   ├── agv-config.properties
    │   └── com\fr\data\
    │       └── AgvDataSource.class
```

---

## 八、技术参考

- 帆软程序数据集：`AbstractTableData` 类
- JDK 版本：必须使用 JDK 8（字节码版本 52.0）
- HTTP 请求：Java 内置 `HttpURLConnection`
- HTML 解析：正则表达式 + 字符串处理
- 配置文件加载：`Class.getResourceAsStream()`（classpath 方式，无绝对路径）
