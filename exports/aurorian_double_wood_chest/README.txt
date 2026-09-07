极光木大宝箱 · 双箱合并模型

aurorian_double_wood_chest.bbmodel：完整 32 单位宽布局。
aurorian_double_wood_chest_left.bbmodel：坐标 X=0..16 的半箱。
aurorian_double_wood_chest_right.bbmodel：右半箱已转成本地 X=0..16；整体拼合时沿 X 加 16。
left/right 是建模坐标命名，后续接入 Minecraft 时需根据朝向映射 ChestType，不能直接假定对应关系。

继承小箱子的极光木板、拱盖、深色包带和灰白铁锁。
中心连通，无隔板、侧把手或重复锁扣；中央锁扣随左右盖同步运动。
箱盖转轴 Y=10、Z=1.6，附 0.55 秒开盖动画，终点 -100 度。
贴图已嵌入每个 bbmodel，并另附 aurorian_chest.png。

已验证左右本地模型拼合后的部件坐标与完整模型一致，且 UV、尺寸、动画目标有效。
仅模型交付，未接入游戏的双箱检测、库存合并或渲染；未完成 Blockbench 界面内验证。

顶部 UV 已重排：顶板与斜面使用连续平铺纹理，保持每模型单位 1 像素，左右接缝连续。几何和开盖动画保持不变。
