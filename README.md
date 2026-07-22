# The Aurorian 2

The Aurorian 2（极光 2）是一个面向 Minecraft 26.1.2、基于 NeoForge 的大型维度与冒险模组。

这是一个全新实现。旧版 The Aurorian 仅作为设计与美术资源档案，不继承旧版 Java 代码。

作者：SXUUZ

简介：幽境之地的冒险

本项目公开仅供查看。除第三方材料外，代码及原创资源均保留全部权利，未经版权所有者事先书面许可不得复制、修改、分发或创建衍生作品。完整条款见 `LICENSE`，第三方声明见 `THIRD_PARTY_NOTICES.md`。

## 开发环境

- Minecraft 26.1.2
- NeoForge 26.1.2.84
- ModDevGradle 2.0.142
- Gradle 9.2.1
- Java 25

## 常用命令

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runData
```

## 项目约定

- 模组 ID：`theaurorian2`
- Java 包：`cn.teampancake.theaurorian2`
- 旧版资源库：`D:\TheAurorian`
- 新代码不得直接复制旧版 Java 实现；先重新定义行为和边界，再实现。
- 贴图、模型、动画、音效和结构可以从旧版迁移，但必须逐项登记并在 26.1.2 中验证。
- 通用逻辑不得引用客户端类，网络状态以服务端为准。
- 每个独立系统应配套 GameTest 或可重复的人工验收步骤。

详细边界与迁移流程见 `docs/ARCHITECTURE.md` 和 `docs/LEGACY_ASSETS.md`。
