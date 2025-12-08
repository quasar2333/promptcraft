# PromptCraft - Forge 1.20.x

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-47.3+-orange.svg)](https://minecraftforge.net)

**PromptCraft** 是一个 Minecraft Forge 模组，通过调用 AI API 实现自动编写命令的功能。

> **作者**: Ultimate_Kevin
> **所有者**: Shy_Creeper
> **官方页面**: [B站空间](https://space.bilibili.com/3493127828540221)

## 🔗 多版本支持

| 模组加载器 | Minecraft 版本 | 分支 | 状态 |
|-----------|---------------|------|------|
| Fabric | 1.21.x | [fabric/1.21.x](../../tree/fabric/1.21.x) | ✅ 可用 |
| Fabric | 1.20.x | [fabric/1.20.x](../../tree/fabric/1.20.x) | ✅ 可用 |
| NeoForge | 1.21.x | [forge/1.21.x](../../tree/forge/1.21.x) | ✅ 可用 |
| Forge | 1.20.x | [forge/1.20.x](../../tree/forge/1.20.x) | ✅ 当前分支 |

## ✨ 功能特性

### 🤖 AI 指令生成
- 通过 SiliconFlow API 调用 AI 模型生成 Minecraft 指令
- 支持自然语言描述，AI 自动转换为对应的游戏指令
- 支持多种 AI 模型（DeepSeek-V3 等）

### 🎮 用户界面
- **快捷键**: 按 `G` 键打开 AI 指令生成界面
- **输入框**: 支持中英文自然语言描述
- **一键操作**: 生成、复制、执行指令

### 🛡️ 安全防护
- **黑名单系统**: 过滤危险关键词，防止恶意指令
- **权限控制**: 服务器环境下的权限管理
- **执行确认**: 可选的指令执行前确认

## 📋 系统要求

- **Minecraft**: 1.20.1
- **Forge**: 47.3.0+
- **Java**: 17+

## 🚀 安装方法

1. 确保已安装 **Forge**
2. 从 [Releases](../../releases) 下载最新版本的 PromptCraft
3. 将 `.jar` 文件放入 `.minecraft/mods` 文件夹
4. 启动游戏

## ⚙️ 配置说明

### API 配置
首次使用需要配置 AI API：

1. **获取 API 密钥**: 从 [SiliconFlow](https://siliconflow.cn) 获取 API 密钥
2. **游戏内配置**: 按 `G` 键打开界面，点击"配置"按钮
3. **填写信息**:
   - **API 密钥**: 你的 SiliconFlow API 密钥
   - **模型**: 推荐使用 `deepseek-ai/DeepSeek-V3`
   - **API 地址**: `https://api.siliconflow.cn/v1`

## 📖 使用方法

1. **打开界面**: 按 `G` 键打开 AI 指令生成界面
2. **描述需求**: 用自然语言描述你想要实现的功能
3. **生成指令**: 点击"生成"按钮
4. **使用指令**: 复制或直接执行

## 🔧 开发信息

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

本项目采用 **GPL-3.0** 许可证。

---

⭐ 如果这个项目对你有帮助，请给个 Star！
