# AGV 小车面板数据采集插件

## 一、目的

从 AGV 小车管理系统的 Web 面板页面实时采集数据，用于帆软（FineReport）报表展示。

### 采集指标
| 指标名称 | 说明 |
|---------|------|
| 当前状态 | AGV 小车当前状态（online/offline/manual等） |
| 剩余电量 | 电量百分比 |
| 正在执行任务 | 当前执行的任务类型和任务ID |
| 任务状态 | 执行失败/执行中/已完成等 |

---

## 二、思路

### 2.1 技术方案

使用帆软 **程序数据集**（Custom TableData）实现。Java 类继承 `AbstractTableData`，在 `getValueAt()` 方法中返回数据。

```
用户请求报表 → 帆软调用 AgvDataSource → HTTP 请求抓取 AGV 面板 → 解析 HTML → 返回数据
```

### 2.2 核心类

- **AgvDataSource.java** - 主程序，继承 AbstractTableData
- **agv-config.properties** - 配置文件，管理各 AGV 的 URL

### 2.3 数据格式

输出为四列数据：
```
| 车牌号 | 指标名称 | 指标值 | 采集时间 |
|--------|----------|--------|----------|
| XQC-1 | 当前状态 | offline | 2026-04-14 15:41:00 |
| XQC-1 | 剩余电量 | 63 | 2026-04-14 15:41:00 |
| XQC-1 | 正在执行任务 | 货架下架 / O|40 | 2026-04-14 15:41:00 |
| XQC-1 | 任务状态 | 执行失败 | 2026-04-14 15:41:00 |
```

---

## 三、部署

### 3.1 文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| AgvDataSource.class | `D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\com\fr\data\` | 编译后的 Java 类 |
| agv-config.properties | `D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\` | 配置文件 |

### 3.2 编译步骤

```bash
# 1. 进入源码目录
cd C:\Users\xudongdong

# 2. 编译（需要 JDK 8）
"C:\Program Files\Java\jdk1.8.0_xxx\bin\javac" -source 8 -target 8 -cp "D:\FineReport_11.0\webapps\webroot\WEB-INF\lib\*" -d . AgvDataSource.java

# 3. 复制到帆软目录
copy com\fr\data\AgvDataSource.class "D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\com\fr\data\"
```

### 3.3 配置文件格式

创建 `agv-config.properties`：

```properties
# AGV 小车 URL 配置
# 格式: 车牌号.url=URL地址

XQC-1.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQC-1
XQE-122-1.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQE-122-1
XQE-122-2.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=XQE-122-2
```

### 3.4 在帆软中使用

1. 打开帆软设计器
2. 新建数据集 → 程序数据集
3. 输入类名：`com.fr.data.AgvDataSource`
4. 确定，拖拽到报表中使用

---

## 四、修改

### 4.1 修改 AGV 列表

编辑 `agv-config.properties`，添加/删除/修改 URL：

```properties
# 添加新车
NEW-CAR.url=http://192.168.0.9:10001/devicePanel/detail?deviceId=NEW-CAR

# 删除某车：删除对应行
# XQC-1.url=...
```

修改后 **重启帆软设计器** 生效。

### 4.2 修改采集指标

编辑 `AgvDataSource.java`：

1. 修改列名（在 `COLUMN_NAMES` 数组中）
2. 修改 `fetchCarData()` 方法中的数据采集逻辑
3. 重新编译部署

### 4.3 修改正则表达式

如果 AGV 面板 HTML 结构变化，需要修改正则表达式：

```java
// 例如修改状态匹配
private static final Pattern STATUS_PATTERN = Pattern.compile("当前状态:\\s*【([^】]+)】");

// 修改后重新编译
```

---

## 五、已知限制

### 5.1 动态内容

以下字段通过 JavaScript 动态加载，HTTP 请求无法获取：
- jobstatus（任务状态的具体值如"开始搬运"）

**解决方案**：通过检测 HTML 中的"（执行失败）"标记或任务是否存在来推断状态。

### 5.2 内网访问

AGV 面板在内网（192.168.x.x），部署帆软的机器必须能访问该网络。

---

## 六、调试

### 6.1 查看日志

日志位置：`D:\FineReport_11.0\logs\fanruan.log`

### 6.2 添加调试输出

在代码中添加 `System.out.println()` 输出，然后查看日志：

```java
System.out.println("[DEBUG] 车牌号: " + carName + ", 状态: " + status);
```

### 6.3 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 类加载失败 | class 文件版本不兼容 | 使用 JDK 8 编译 |
| HTTP 连接失败 | 网络不通/AGV 面板未启动 | 检查网络和 URL |
| 数据为空 | 正则表达式不匹配 | 检查 HTML 结构和正则 |

---

## 七、文件结构

```
C:\Users\xudongdong\
├── AgvDataSource.java          # 源代码
├── agv-config.properties        # 配置文件模板
├── compile_and_deploy.bat      # 一键编译部署脚本
└── com\fr\data\
    └── AgvDataSource.class     # 编译后的类

D:\FineReport_11.0\
└── webapps\webroot\WEB-INF\
    ├── classes\
    │   ├── agv-config.properties
    │   └── com\fr\data\
    │       └── AgvDataSource.class
    └── lib\                     # 帆软 JAR 包
```

---

## 八、技术参考

- 帆软程序数据集：`AbstractTableData` 类
- JDK 版本：必须使用 JDK 8（帆软设计器兼容）
- HTTP 请求：使用 Java 内置 `HttpURLConnection`
- HTML 解析：正则表达式 + 字符串处理
