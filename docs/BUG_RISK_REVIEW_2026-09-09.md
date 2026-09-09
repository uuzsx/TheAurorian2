# 极光：树木以外的问题与误区统计

日期：2026-09-09。下方问题描述是修复前的分析快照，原位置行号可能已随修复移动。

## 后续修复状态

用户授权后，已修复 **1—6**，处理 **8、9**；随后用户确认 **7 按建议的饱和度总量调整**，8 种食物已完成修改。**10—12 是误区和功能边界，没有按故障修改**。

- 饰品在反序列化阶段只恢复数据，登录后再同步效果；同时修复菜单槽位仍绑定旧库存实例的问题，保持原存档结构。
- 仪式音乐按“维度 + 祭坛位置”记录来源，多祭坛共用一条音乐，最后一个来源结束才停止；移除祭坛清理音乐和底座灯光。音轨结束后不再持续压制原版音乐。
- 未完成的仪式遇到停止加载、跳过服务端 tick 或服务器重启会中断，需要重新开始；已完成的净化保持不变。这防止通过停止加载跳过守护阶段。
- 水壶从自然水源装水时向使用者同步音效。
- 第 7 项的熟皎月鱼、熟极光翅鱼、薰衣草沙拉、伪藻渊鱼、谧木果、金谧木果、菇肉串、烤极光冬根，饱和度恢复总量分别改为 6、6、5、5、2.5、5、15、6；明确由总量换算倍率，饥饿值和附加效果不变，其他食物不改。

后续用户补充料理配方后，菇肉串进一步调整为饥饿值 6、饱和度 7.2；生鱼片调整为 2／2，极光培根调整为 1／0.6。上述第 7 项列表保留首次修复记录，最新料理数值以验证记录末尾为准。
- 禁咒的附魔屏蔽改为临时持有者状态，离开该玩家库存的物品不再因旧标记持续失效。旧标记不会再影响附魔读取，进入玩家库存时会被清理，原附魔和其他自定义数据保留。
- 删除 40 份仅修改木棍材料的原版配方覆盖；通过配方事件扩展材料，并保留数据包使用的公共木棍标签、配方形状与产出。
- 背包检查先判断自定义数据是否含相关键，避免普通物品每 tick 复制整份 NBT。仍保留有界库存检查，保障外部修改物品与旧数据能被处理。

验证记录见 [BUGFIX_VERIFICATION_2026-09-09.md](BUGFIX_VERIFICATION_2026-09-09.md)。音乐网络协议已从 4 更新为 5，客户端和服务端应同时更新模组。

## 修复前统计

统计为 **12 项：7 项功能问题或疑点、2 项工程风险、3 项容易误判的情况**。不是“发现了 12 个已复现的 Bug”。既有饰品日志属于运行证据，其余通过本模组源码及本地 Minecraft/NeoForge 源码核对；本轮未启动游戏复现，也没有进行性能测量。

## 功能问题与疑点：7 项

### 1. 饰品库存读档失败——已有日志确认，优先级高

读取饰品附件结束时立即重算属性，重算又获取可自动同步的护盾附件。此时玩家网络连接还没建立，产生空指针，NeoForge 记录附件反序列化失败并跳过。

影响：可能妨碍饰品库存正常恢复。是否及在何种存档下实际丢失物品，需要用保存前后都有明确饰品内容的存档核验，不能直接推断所有玩家都会丢饰品。

位置：[AccessoryInventory.java](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/inventory/AccessoryInventory.java:56)、[MoonShieldSystem.java](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/world/MoonShieldSystem.java:70)。已有日志：[无光影测试](D:/极光/build/mirror-no-iris-check.log:620)、[光影测试](D:/极光/build/mirror-iris-release-check.log:1715)。

建议验证：放入饰品并保存，退出重登，比较库存和属性；覆盖已净化、未净化、不同附件保存状态及死亡重生。数据反序列化阶段应保持为数据恢复，登录后再进行需要连接的操作。

### 2. 多个净化仪式的音乐互相覆盖——代码确认缺少仪式区分

所有祭坛都向整个维度发送同一个“播放/停止”布尔消息，消息不带祭坛或仪式 ID。客户端也只有一个全局音乐实例。

具体结果：A 仪式进行时，B 开始会重新播放音乐；B 结束会发出停止消息，即使 A 还没结束，A 的音乐也被停止。源码没有禁止不同祭坛同时运行的全局限制。

位置：[PurificationRitualMusicPayload.java](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/network/PurificationRitualMusicPayload.java:10)、[祭坛广播](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/block/entity/PurificationAltarBlockEntity.java:462)、[客户端处理](D:/极光/src/main/java/cn/teampancake/theaurorian2/client/sound/PurificationRitualMusic.java:27)。

建议验证：两名未净化玩家在同一维度启动不同祭坛，错开开始与结束时间。修复时按明确的仪式归属或活动仪式集合管理音乐。

### 3. 祭坛移除后的音乐清理不完整——代码确认生命周期缺口

正常成功、失败会发送停止音乐消息，但方块实体 `setRemoved()` 只清除进度条和附近仪式实体，没有停止音乐。祭坛方块的移除处理也没有补上这一步。

客户端是否抑制原版音乐，只检查 `currentMusic != null` 和维度；没有在曲目自然播完时清空该引用。因此在仪式中拆除祭坛、又留在同一维度时，可能继续播放仪式曲目，之后原版背景音乐仍被阻止，直到收到停止消息、换维度或退出。

位置：[祭坛移除](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/block/entity/PurificationAltarBlockEntity.java:600)、[音乐状态判断](D:/极光/src/main/java/cn/teampancake/theaurorian2/client/sound/PurificationRitualMusic.java:35)、[原版音乐拦截](D:/极光/src/main/java/cn/teampancake/theaurorian2/mixin/MusicManagerMixin.java:15)。

建议验证：仪式中拆除祭坛，并等待曲目结束；另测区块卸载。区块卸载与真正破坏应分别定义暂停、恢复和取消行为。

### 4. 卸载祭坛区块可能跳过守护过程——高可信疑似玩法漏洞

仪式进度使用“当前世界时间减开始时间”，而不是实际完成的仪式 tick 数。活动状态和开始时间会保存；持续过程只检查玩家存活、在线且仍在同一维度，没有持续距离检查。

触发设想：开始仪式后远离，使祭坛区块不再 tick；在同一维度等待超过总时长 4 分 20 秒，再回来。返回后计算的进度已经满了，期间停止加载的怪物无法持续攻击祭坛。若护盾仍在，代码会进入完成分支。

位置：[持续检查与计时](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/block/entity/PurificationAltarBlockEntity.java:365)、[完成分支](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/block/entity/PurificationAltarBlockEntity.java:386)、[进度保存](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/block/entity/PurificationAltarBlockEntity.java:568)。

尚未实测不同模拟距离、区块卸载时机下的完整利用过程。建议优先复现；若守护过程必须实际进行，应明确离开、暂停、卸载和恢复规则。

### 5. 凝月水壶取自然水源时，使用者收不到装水音效——代码确认收件人错误

客户端在播放声音前直接返回成功；服务端随后调用 `playSound(player, ...)`。本地 `ServerLevel` 源码表明，该参数表示排除的玩家，广播会跳过使用者。

因此这条路径没有向使用者播放声音，附近其他玩家可以收到。该问题针对从自然水源装水的路径；炼药锅交互使用 `playSound(null, ...)`，不属于同一问题。

位置：[AurorianJugItem.java](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/item/AurorianJugItem.java:72)。依据为本地依赖源码 `minecraft-patched-26.1.2.84-sources.jar` 中 `ServerLevel.playSeededSound` 的 `except` 广播参数。

建议验证：两名玩家站近，一人用空水壶装水，对比双方音效。

### 6. 禁咒标记会跟装备转移，清理范围却只有玩家库存——代码确认残留路径

禁咒将“禁用附魔”标记写进物品本身。读取附魔时，只要物品有标记就清空有效附魔，不判断现在的持有者是否中了禁咒。清除标记只遍历玩家的库存和装备。

具体场景：中禁咒的玩家把附魔装备交给可穿装备的生物。装备离开玩家后，即使原玩家的禁咒解除，这件装备也不在清理范围内；生物使用它时仍会遇到标记导致的附魔屏蔽。物品回到无禁咒玩家背包后，玩家 tick 才有机会清理。

附魔数据没有被删除，问题是效果屏蔽标记残留。本轮未实际验证每一种生物及附魔组合。

位置：[附魔读取](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/effect/AurorianEffectEvents.java:289)、[标记范围](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/effect/AurorianEffectEvents.java:473)、[库存遍历](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/effect/AurorianEffectEvents.java:496)。

建议验证：用附魔防具完成“玩家中禁咒 → 转交生物 → 玩家解除禁咒 → 检查生物附魔效果”的过程。需要明确禁咒属于持有者状态还是物品状态，避免二者混用。

### 7. 部分食物可能把饱和度总量填进了倍率参数——数值疑点

例如 `kebab_with_mushroom` 使用营养值 12、饱和倍率 15。代码确实将 15 传入 `saturationModifier`；原版计算是 `营养值 × 倍率 × 2`，得到 360 的理论饱和补充值，而非 15。

实际饱和度会被限制到当前饥饿值，最高 20，因此不会变成 360，但容易让这类食物直接补满隐藏饱和度。其他若干食物也使用 5、6 这样的倍率。

位置：[食物数值](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/registry/ModItems.java:166)、[构造方式](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/registry/ModItems.java:345)。计算与上限已对照本地 `FoodProperties`、`FoodConstants`、`FoodData`。

是否属于 Bug 取决于原定平衡目标。若本来就希望补满，则不是实现错误；如果想补 15 点饱和度，则参数语义填错。未经确认不应直接降数值。

## 工程风险：2 项

### 8. 原版配方采用整份覆盖与事件修改两条路径

40 份 `minecraft` 命名空间配方覆盖了原版资源，同时事件又处理同批配方的木棍材料。与其他模组或数据包改同名配方时，存在覆盖冲突风险。单独加载极光时能合成，不足以证明整合包兼容。

位置：[AurorianRecipeIntegration.java](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/crafting/AurorianRecipeIntegration.java:62)、`src/main/resources/data/minecraft/recipe/`。

### 9. 没有效果的玩家也会每 tick 扫描库存和复制物品自定义数据

玩家每 tick 都调用禁咒标记处理；未中腐化时还遍历装备、库存检查护甲欠债。检查函数对非空物品调用 `copyTag()`，所以普通玩家也承担了这部分工作。

这是确定存在的额外遍历和分配，不能未经测量就宣称是明显卡顿原因。多人或大量携带复杂物品时值得单独计时，再考虑状态变化驱动和有债务物品的跟踪。

位置：[无条件调用](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/effect/AurorianEffectEvents.java:247)、[债务检查](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/effect/AurorianEffectEvents.java:516)、[标记读取](D:/极光/src/main/java/cn/teampancake/theaurorian2/common/effect/AurorianEffectEvents.java:544)。

## 容易误判的情况：3 项，不计作 Bug

### 10. 文件内容相同、没有直接 PNG，不代表资源无用或缺失

相同 JSON 可能属于不同注册项或状态，资源还会通过约定路径、标签、代码拼接和图集加载。箱子的 `block/*_chest_model` 就是图集别名，实际对应实体贴图。不能只靠文本引用次数或文件名删除。

### 11. 有名字、有模型、有注册，不等于功能完成

Legacy 普通物品、预留附魔键、NPC 模型和导出目录中的皎月箱方案，完成程度不同。木匠工作台暂无制作功能、测试板条箱掉金苹果，都是此前确定的阶段性要求，不应误报成忘写功能。

另发现一个小残留：祭坛的 `lastHealth` 字段目前只赋值、保存和读取，没有用于行为判断。它不构成“受伤会中断仪式”的实现证据，也不是独立严重 Bug；如要清理，应顺带处理旧存档字段兼容。

### 12. 镜子支持开启光影，不等于镜内完整执行光影包

当前方案在开 Iris 时让镜内走独立原版渲染流程，主画面继续使用光影。因此镜内外亮度不同可能是方案限制；不能把它自动归类为贴图或颜色错误。其他 Iris 版本、光影包也需要分别验证。

位置：[镜面兼容说明](D:/极光/docs/MIRROR_REFLECTIONS.md)。

## 处理顺序

优先核验并处理 1（饰品读档）、4（仪式跳过）、6（禁咒残留）；之后处理 2、3、5 的音乐与音效。7 先确认数值意图，8 做兼容验证，9 先测量成本。10—12 是理解边界，不需要按 Bug 批量“修复”。
