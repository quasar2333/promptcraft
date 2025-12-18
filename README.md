# PromptCraft

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8-green.svg)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-0.17.2+-orange.svg)](https://fabricmc.net)
[![Build and Release](https://github.com/quasar2333/promptcraft/actions/workflows/build-release.yml/badge.svg?branch=fabric/1.21.x)](https://github.com/quasar2333/promptcraft/actions/workflows/build-release.yml?query=branch%3Afabric%2F1.21.x)

**PromptCraft** 是一个 Minecraft Fabric 模组，通过调用 AI API 实现自动编写命令的功能。

> **作者**: Ultimate_Kevin
> **官方页面**: [GitHub 仓库](https://github.com/quasar2333/promptcraft)

## 🔗 多版本支持

| 模组加载器 | Minecraft 版本 | 分支 | 状态 |
|-----------|---------------|------|------|
| Fabric | 1.21.x | [fabric/1.21.x](../../tree/fabric/1.21.x) | ✅ 当前分支 |
| Fabric | 1.20.x | [fabric/1.20.x](../../tree/fabric/1.20.x) | ✅ 可用 |
| NeoForge | 1.21.x | [forge/1.21.x](../../tree/forge/1.21.x) | ✅ 可用 |
| Forge | 1.20.x | [forge/1.20.x](../../tree/forge/1.20.x) | ✅ 可用 |

## ✨ 功能特性

### 🤖 AI 指令生成
- 通过 SiliconFlow API 调用 AI 模型生成 Minecraft 指令
- 支持自然语言描述，AI 自动转换为对应的游戏指令
- 支持多种 AI 模型（DeepSeek-V3 等）

### 🎮 用户界面
- **快捷键**: 按 `G` 键打开 AI 指令生成界面
- **输入框**: 支持中英文自然语言描述
- **一键操作**: 生成、复制、执行指令
- **ModMenu 集成**: 通过 ModMenu 管理配置

### 🛡️ 安全防护
- **黑名单系统**: 过滤危险关键词，防止恶意指令
- **权限控制**: 服务器环境下的权限管理
- **执行确认**: 可选的指令执行前确认

## 📋 系统要求

- **Minecraft**: 1.21.8
- **Fabric Loader**: 0.17.2+
- **Fabric API**: 0.132.0+1.21.8
- **Java**: 21+
- **ModMenu**: 推荐安装（可选）

## 🚀 安装方法

1. 确保已安装 **Fabric Loader** 和 **Fabric API**
2. 从 [Releases](../../releases) 下载最新版本的 PromptCraft
3. 将 `.jar` 文件放入 `.minecraft/mods` 文件夹
4. 启动游戏

## ⚙️ 配置说明

### API 配置
首次使用需要配置 AI API：

1. **获取 API 密钥**: 从 [SiliconFlow](https://siliconflow.cn) 获取 API 密钥
2. **游戏内配置**:
   - 按 `G` 键打开界面，点击"配置"按钮
   - 或通过 ModMenu 进入配置界面
3. **填写信息**:
   - **API 密钥**: 你的 SiliconFlow API 密钥
   - **模型**: 推荐使用 `deepseek-ai/DeepSeek-V3`
   - **API 地址**: `https://api.siliconflow.cn/v1`

### 安全设置
- **黑名单**: 自动过滤危险关键词（如 `rm`, `delete`, `format` 等）
- **执行确认**: 可开启指令执行前的确认对话框
- **权限控制**: 服务器环境下的权限管理

## 📖 使用方法

1. **打开界面**: 按 `G` 键打开 AI 指令生成界面
2. **描述需求**: 用自然语言描述你想要实现的功能
   ```
   例如: "给我一把附魔钻石剑"
   例如: "在我脚下生成一个村庄"
   例如: "把天气改成雷雨"
   ```
3. **生成指令**: 点击"生成"按钮，等待 AI 生成指令
4. **使用指令**:
   - 点击"复制"复制到剪贴板
   - 点击"执行"直接在游戏中运行

## 🔧 开发信息

### 技术栈
- **Minecraft**: 1.21.8
- **Fabric Loader**: 0.17.2+
- **Fabric API**: 0.132.0+1.21.8
- **Java**: 21+
- **HTTP 客户端**: Java 内置
- **JSON 处理**: Gson 2.10.1
- **ModMenu**: 11.0.3（可选集成）

### 构建项目
```bash
./gradlew build
```

### 开发环境
```bash
./gradlew runClient  # 运行客户端
./gradlew runServer  # 运行服务器
```

## 📄 许可证

本项目采用 **GPL-3.0** 许可证：
- ✅ **允许商用**: 可用于商业用途
- ✅ **允许修改**: 可以修改和分发
- ⚠️ **强制开源**: 任何基于此项目的衍生作品必须同样开源
- ⚠️ **保留署名**: 必须保留原作者信息

查看 [LICENSE](LICENSE) 文件了解完整条款。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 贡献指南
1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📞 联系方式

- **作者邮箱**: quasar2333@gmail.com
- **问题反馈**: [GitHub Issues](../../issues)

---

⭐ 如果这个项目对你有帮助，请给个 Star！
