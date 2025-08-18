# PromptCraft

PromptCraft是一个Minecraft Fabric模组，通过集成SiliconFlow AI API在游戏中实现AI驱动的指令生成功能。

## 功能特性

### 核心功能
- 🤖 **AI指令生成**: 通过SiliconFlow API调用AI模型生成Minecraft指令
- 🔗 **多API支持**: 支持OpenAI和Google两种API调用格式
- 🌐 **多语言支持**: 支持中文和英文输入及界面
- ⚡ **实时生成**: 快速响应用户输入，生成相应的游戏指令

### 用户界面
- 🎮 **专用界面**: 按G键打开专用的AI指令生成界面
- 📝 **输入框**: 中央位置的文本输入框，支持自然语言描述
- 📋 **结果显示**: 清晰显示生成的指令内容
- 🔄 **一键操作**: 复制和执行按钮，方便快速使用生成的指令
- ⚙️ **配置管理**: 右上角配置按钮，管理API设置和黑名单

### 权限与安全
- 🛡️ **黑名单系统**: 关键词过滤，防止危险指令执行
- 👥 **权限控制**: 
  - 服务器环境：仅房主和管理员可修改黑名单
  - 单机游戏：玩家默认可配置
- 🔧 **指令类型**: 支持切换命令方块指令和普通指令模式

## 安装要求

- Minecraft 1.21.4
- Fabric Loader 0.16.9+
- Fabric API 0.110.5+
- Java 21+

## 安装方法

1. 确保已安装Fabric Loader和Fabric API
2. 下载PromptCraft模组文件
3. 将模组文件放入`.minecraft/mods`文件夹
4. 启动游戏

## 配置说明

### API配置
首次使用需要配置以下信息：
- **API密钥**: 从SiliconFlow获取的API密钥
- **模型编号**: 选择要使用的AI模型（默认：deepseek-ai/DeepSeek-V3）
- **基础URL**: API调用地址（默认：https://api.siliconflow.cn/v1）

### 黑名单配置
为了安全起见，模组内置了一些危险关键词的黑名单：
- `rm`, `delete`, `format`, `shutdown`, `stop`

用户可以根据需要添加或删除黑名单关键词。

## 使用方法

1. **打开界面**: 按G键（可自定义）打开AI指令生成界面
2. **输入描述**: 在输入框中用中文或英文描述你想要实现的功能
3. **生成指令**: 点击"生成"按钮，等待AI生成相应的Minecraft指令
4. **使用指令**: 
   - 点击"复制"按钮复制指令到剪贴板
   - 点击"执行"按钮直接在游戏中执行指令

## 开发信息

### 项目结构
```
src/
├── main/java/com/promptcraft/
│   ├── PromptCraft.java              # 主模组类
│   ├── api/SiliconFlowClient.java    # API客户端
│   ├── config/ConfigManager.java     # 配置管理
│   ├── network/NetworkHandler.java   # 网络处理
│   └── util/
│       ├── TestHelper.java          # 测试工具
│       └── ErrorHandler.java        # 错误处理
├── client/java/com/promptcraft/client/
│   ├── PromptCraftClient.java        # 客户端初始化
│   ├── api/ApiManager.java           # API管理器
│   ├── gui/
│   │   ├── PromptCraftScreen.java    # 主界面
│   │   ├── ConfigScreen.java         # 配置界面
│   │   ├── BlacklistScreen.java      # 黑名单配置界面
│   │   └── ConfirmationScreen.java   # 确认对话框
│   ├── network/ClientNetworkHandler.java # 客户端网络处理
│   └── util/
│       ├── LocalizationHelper.java  # 本地化助手
│       └── PerformanceMonitor.java  # 性能监控
└── main/resources/
    ├── fabric.mod.json               # 模组元数据
    ├── assets/promptcraft/lang/      # 语言文件
    │   ├── en_us.json               # 英文
    │   └── zh_cn.json               # 中文
    └── promptcraft.mixins.json       # Mixin配置
```

### 构建项目
```bash
./gradlew build
```

### 开发环境
```bash
./gradlew runClient  # 运行客户端
./gradlew runServer  # 运行服务器
```

### 测试
```bash
# Windows
build-and-test.bat

# Linux/Mac
chmod +x build-and-test.sh
./build-and-test.sh
```

### 功能特性详细说明

#### 🤖 AI指令生成
- 支持自然语言输入，自动转换为Minecraft指令
- 集成SiliconFlow API，支持多种AI模型
- 智能指令验证和安全检查
- 支持中英文输入

#### 🎮 用户界面
- **主界面**: 简洁直观的指令生成界面
- **配置界面**: 完整的API和模组设置
- **黑名单管理**: 可视化的关键词管理
- **确认对话框**: 安全的指令执行确认

#### ⌨️ 快捷键支持
- `G`: 打开主界面
- `Ctrl+G`: 快速生成模式
- `Alt+G`: 切换命令方块模式
- 所有快捷键均可自定义

#### 🛡️ 安全系统
- **黑名单过滤**: 防止危险关键词执行
- **权限控制**: 区分普通用户和管理员权限
- **指令验证**: 多层安全检查
- **执行确认**: 可选的执行前确认

#### 🌍 多语言支持
- 完整的中英文界面
- 自动语言检测
- 本地化的错误消息和提示

#### ⚡ 性能优化
- 异步API调用，不阻塞游戏
- 内存使用监控
- 性能统计和优化建议
- 智能垃圾回收

## 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情。

## 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 支持

如果你遇到任何问题或有建议，请在GitHub上创建Issue。
