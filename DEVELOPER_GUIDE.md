# PromptCraft 开发者指南

## 项目概述

PromptCraft是一个Minecraft Fabric模组，通过集成SiliconFlow AI API实现AI驱动的游戏指令生成功能。

## 技术栈

- **Minecraft版本**: 1.21.4
- **Fabric Loader**: 0.16.9+
- **Fabric API**: 0.110.5+
- **Java版本**: 21+
- **构建工具**: Gradle 8.10.2
- **HTTP客户端**: OkHttp 4.12.0
- **JSON处理**: Gson 2.10.1

## 架构设计

### 模块划分

1. **核心模块** (`src/main/java/com/promptcraft/`)
   - `PromptCraft.java`: 主模组类，负责初始化
   - `config/`: 配置管理系统
   - `network/`: 服务器端网络处理
   - `api/`: API客户端实现
   - `util/`: 工具类和错误处理

2. **客户端模块** (`src/client/java/com/promptcraft/client/`)
   - `PromptCraftClient.java`: 客户端初始化
   - `gui/`: 用户界面实现
   - `api/`: 客户端API管理
   - `network/`: 客户端网络处理
   - `util/`: 客户端工具类

### 设计模式

- **单例模式**: ApiManager, ConfigManager
- **观察者模式**: 配置变更通知
- **策略模式**: 不同API格式的处理
- **工厂模式**: GUI组件创建

## 核心功能实现

### 1. API集成

```java
// API客户端实现
public class SiliconFlowClient {
    // 支持OpenAI和Google两种API格式
    // 异步HTTP请求处理
    // 自动重试机制
    // 错误处理和恢复
}
```

### 2. 配置系统

```java
// 配置管理
public class ConfigManager {
    // JSON配置文件读写
    // 配置验证和默认值
    // 热重载支持
    // 客户端/服务器端同步
}
```

### 3. GUI系统

```java
// 主界面
public class PromptCraftScreen extends Screen {
    // 响应式布局
    // 多语言支持
    // 实时状态更新
    // 用户交互处理
}
```

### 4. 网络通信

```java
// 网络处理
public class NetworkHandler {
    // 客户端-服务器通信
    // 数据包序列化
    // 权限验证
    // 错误处理
}
```

## 开发环境设置

### 1. 环境要求

- Java 21 JDK
- IntelliJ IDEA (推荐) 或 Eclipse
- Git

### 2. 项目设置

```bash
# 克隆项目
git clone <repository-url>
cd PromptCraft

# 生成IDE项目文件
./gradlew genEclipseRuns  # Eclipse
./gradlew genIntellijRuns # IntelliJ

# 生成Minecraft源码
./gradlew genSources
```

### 3. 运行和调试

```bash
# 运行客户端
./gradlew runClient

# 运行服务器
./gradlew runServer

# 构建模组
./gradlew build
```

## 代码规范

### 1. 命名规范

- **类名**: PascalCase (如 `PromptCraftScreen`)
- **方法名**: camelCase (如 `generateCommand`)
- **常量**: UPPER_SNAKE_CASE (如 `MOD_ID`)
- **包名**: 小写，用点分隔 (如 `com.promptcraft.client.gui`)

### 2. 注释规范

```java
/**
 * 类的功能描述
 * 
 * @author 作者名
 * @since 版本号
 */
public class ExampleClass {
    
    /**
     * 方法功能描述
     * 
     * @param param 参数描述
     * @return 返回值描述
     */
    public String exampleMethod(String param) {
        // 实现逻辑
    }
}
```

### 3. 错误处理

```java
try {
    // 可能出错的代码
} catch (SpecificException e) {
    // 使用ErrorHandler统一处理
    ErrorHandler.handleApiError("operation", e, player);
}
```

## 测试指南

### 1. 单元测试

```java
// 使用TestHelper进行功能测试
public class TestExample {
    public void testConfiguration() {
        boolean result = TestHelper.validateConfiguration();
        assert result : "Configuration validation failed";
    }
}
```

### 2. 集成测试

```bash
# 运行完整测试套件
./gradlew test

# 运行带测试标志的客户端
./gradlew runClient -Dprompcraft.runTests=true
```

### 3. 性能测试

```java
// 使用PerformanceMonitor监控性能
PerformanceMonitor.startTiming("api_call");
// 执行操作
PerformanceMonitor.endTiming("api_call");
```

## 扩展开发

### 1. 添加新的API提供商

1. 在`SiliconFlowClient`中添加新的API格式支持
2. 更新配置系统以支持新的参数
3. 添加相应的错误处理
4. 更新文档和测试

### 2. 添加新的GUI组件

1. 继承`Screen`类创建新界面
2. 实现本地化支持
3. 添加键盘和鼠标事件处理
4. 集成到主界面导航中

### 3. 添加新的指令类型

1. 在`NetworkHandler`中添加新的处理逻辑
2. 更新权限检查系统
3. 添加相应的安全验证
4. 更新用户界面

## 调试技巧

### 1. 日志调试

```java
// 使用模组日志系统
PromptCraft.LOGGER.debug("Debug message: {}", value);
PromptCraft.LOGGER.info("Info message");
PromptCraft.LOGGER.warn("Warning message");
PromptCraft.LOGGER.error("Error message", exception);
```

### 2. 性能调试

```java
// 监控内存使用
PerformanceMonitor.logMemoryUsage("context");

// 检查高内存使用
if (PerformanceMonitor.isMemoryUsageHigh()) {
    // 采取优化措施
}
```

### 3. 网络调试

- 使用Wireshark监控网络流量
- 检查数据包格式和内容
- 验证API调用的正确性

## 发布流程

### 1. 版本管理

1. 更新`gradle.properties`中的版本号
2. 更新`fabric.mod.json`中的版本信息
3. 创建Git标签

### 2. 构建发布

```bash
# 清理并构建
./gradlew clean build

# 检查构建产物
ls build/libs/
```

### 3. 测试验证

1. 在不同环境中测试模组
2. 验证所有功能正常工作
3. 检查性能和内存使用

## 贡献指南

### 1. 提交代码

1. Fork项目仓库
2. 创建功能分支
3. 编写代码和测试
4. 提交Pull Request

### 2. 报告问题

1. 使用GitHub Issues
2. 提供详细的错误信息
3. 包含重现步骤
4. 附上相关日志

### 3. 文档更新

1. 更新相关文档
2. 添加新功能说明
3. 更新API文档
4. 翻译多语言版本

## 常见问题

### Q: 如何添加新的语言支持？

A: 在`src/main/resources/assets/promptcraft/lang/`目录下添加新的语言文件，格式为`语言代码.json`。

### Q: 如何自定义API端点？

A: 在配置界面中修改Base URL，或直接编辑配置文件中的`baseUrl`字段。

### Q: 如何调试API调用问题？

A: 启用详细日志记录，检查网络连接，验证API密钥和模型ID的正确性。

## 联系方式

- GitHub Issues: 报告问题和功能请求
- 开发者邮箱: [开发者邮箱]
- 项目主页: [项目链接]
