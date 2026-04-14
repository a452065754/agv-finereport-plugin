# AGV 数据采集插件 - 记忆文档

## 项目概述

这是一个帆软（FineReport）程序数据集插件，用于从 AGV 小车管理系统的 Web 面板实时采集数据。

**GitHub 地址**：（用户重装系统后填写）

---

## 技术栈

- **语言**：Java 8
- **目标平台**：帆软 FineReport 11.0
- **数据获取**：HTTP GET + 正则解析 HTML
- **核心类**：`AbstractTableData`

---

## 核心文件

| 文件 | 说明 |
|------|------|
| `AgvDataSource.java` | 主程序，继承 AbstractTableData |
| `agv-config.properties` | AGV 小车 URL 配置文件 |
| `compile_and_deploy.bat` | 一键编译部署脚本 |

---

## 快速开始

### 1. 克隆项目
```bash
git clone <项目地址>
cd <项目目录>
```

### 2. 编译部署
```bash
# 确保 JDK 8 可用，然后运行
compile_and_deploy.bat
```

### 3. 配置 AGV 地址
编辑 `agv-config.properties`：
```properties
XQC-1.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQC-1
XQE-122-1.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQE-122-1
XQE-122-2.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQE-122-2
```

### 4. 在帆软中使用
1. 打开帆软设计器
2. 新建数据集 → 程序数据集
3. 输入类名：`com.fr.data.AgvDataSource`
4. 确定

---

## 采集指标

| 字段 | 说明 |
|------|------|
| 车牌号 | AGV 标识 |
| 当前状态 | online/offline/manual |
| 剩余电量 | 电量百分比 |
| 正在执行任务 | 任务类型 / 任务ID |
| 任务状态 | 执行失败/执行中/已完成 |

---

## 已知限制

- **任务状态（jobstatus）**：由 JavaScript 动态加载，HTTP 请求无法获取原始值。代码通过检测 HTML 中的"（执行失败）"标记推断状态。

---

## 部署路径

编译后的 class 文件需部署到：
```
D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\com\fr\data\AgvDataSource.class
```

配置文件：
```
D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\agv-config.properties
```

---

## 修改记录

- 2026-04-14：初始版本，支持 3 个 AGV 小车的基本状态采集
