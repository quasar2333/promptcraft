# PromptCraft 多版本分支管理工作流

## 分支策略

### 分支命名规范
- `main` - 主开发分支，包含最新的 Fabric 版本代码
- `fabric/1.21.x` - Fabric 1.21.x 稳定版本
- `fabric/1.20.x` - Fabric 1.20.x 稳定版本
- `forge/1.21.x` - NeoForge 1.21.x 版本
- `forge/1.20.x` - Forge 1.20.x 版本

### 版本差异概要

| 版本 | Java | 加载器 | GUI API | 网络 API |
|------|------|--------|---------|----------|
| Fabric 1.21.x | 21 | Fabric Loader 0.17+ | Screen, DrawContext | Fabric Networking API |
| Fabric 1.20.x | 17 | Fabric Loader 0.16+ | Screen, DrawContext | Fabric Networking API |
| NeoForge 1.21.x | 21 | NeoForge 21+ | Screen, GuiGraphics | NeoForge Networking |
| Forge 1.20.x | 17 | Forge 47+ | Screen, GuiGraphics | Forge Networking |

## 开发工作流

### 1. 从 main 同步功能到其他分支
```bash
# 切换到目标分支
git checkout fabric/1.20.x

# 从 main 挑选特定提交
git cherry-pick <commit-hash>

# 或合并（需要解决冲突）
git merge main --no-ff
```

### 2. 版本适配要点

#### Fabric 1.20.x 适配
- Java 版本: 17
- `Identifier.of()` -> 使用兼容方法
- ModMenu 版本: 7.x

#### NeoForge 1.21.x 适配
- 使用 NeoForge Gradle 插件
- `GuiGraphics` 代替 `DrawContext`
- 注册系统使用 `DeferredRegister`

#### Forge 1.20.x 适配
- 使用 Forge Gradle 插件
- `GuiGraphics` 代替 `DrawContext`
- Mixin 配置不同

### 3. 构建和测试
// turbo
```bash
./gradlew clean build
```

### 4. 推送分支
```bash
git push github <branch-name>
```
