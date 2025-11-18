# ProfessionCustom - 数据包配置指南

本模组支持通过数据包来配置生物等级系统的参数，这样玩家和服务器管理员可以自定义游戏体验，而无需修改模组代码或重新编译。

## 数据包配置结构

要创建数据包配置，您需要按照以下目录结构组织JSON文件：

```
/data_pack_name/data/professioncustom/professioncustom/your_config.json
```

## JSON配置格式

### 配置文件基本结构

配置文件必须包含以下两个可选部分：

```json
{
  "dimension_bonuses": {
    "维度ID": 等级加成倍数
  },
  "mob_bonuses": {
    "生物类型ID": {
      "health_multiplier": 生命值倍数,
      "armor_multiplier": 护甲倍数,
      "armor_toughness_multiplier": 护甲韧性倍数,
      "attack_damage_multiplier": 攻击力倍数
    }
  }
}
```

### 配置说明

1. **dimension_bonuses**：定义不同维度的等级加成倍数
   - 键：维度的资源位置（如 `minecraft:overworld`）
   - 值：浮点数，表示该维度中生物等级的额外加成倍数

2. **mob_bonuses**：定义特定生物类型的属性加成
   - 键：生物类型的资源位置（如 `minecraft:zombie`）
   - 值：包含以下属性的对象：
     - `health_multiplier`：生命值提升倍数
     - `armor_multiplier`：护甲值提升倍数
     - `armor_toughness_multiplier`：护甲韧性提升倍数
     - `attack_damage_multiplier`：攻击力提升倍数

### 配置优先级

1. 数据包配置优先于模组自带的文件配置
2. 多个数据包中的配置会合并，后加载的数据包会覆盖先前数据包中的相同配置

## 创建示例数据包

1. 在Minecraft主目录下的`datapacks`文件夹中创建一个新文件夹，例如`professioncustom_config`

2. 在该文件夹中创建`pack.mcmeta`文件：

```json
{
  "pack": {
    "pack_format": 10,
    "description": "自定义 ProfessionCustom 配置"
  }
}
```

3. 创建目录结构：`professioncustom_config/data/professioncustom/professioncustom/`

4. 在该目录中创建JSON配置文件，例如`my_config.json`

5. 将数据包放入世界的`datapacks`文件夹中，或在服务器的`world/datapacks`文件夹中

6. 使用`/reload`命令重新加载数据包

## 内置示例

模组自带了两个示例配置文件，位于：
- `example_config.json`：基本示例
- `detailed_config.json`：更详细的配置示例

您可以参考这些文件来创建自己的配置。

## 日志输出

配置加载过程会在日志中记录以下信息：
- 从数据包加载的维度加成数量
- 从数据包加载的生物加成数量
- 详细的调试信息（如果启用了调试日志级别）

## 注意事项

- 所有配置文件必须是有效的JSON格式
- 配置中的资源位置必须使用正确的命名空间和路径（如 `minecraft:zombie`）
- 配置参数必须是有效的数值
- 建议先在测试环境中测试您的配置文件，确保它们按预期工作