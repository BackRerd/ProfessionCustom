# ProfessionCustom 数据包配置说明

本说明文档介绍如何通过数据包配置本模组的各类系统，包括：

- 护甲 / 武器词条的刷新概率、最大词条数、基础数值以及特殊“反向成长”词条（`armor_attributes` / `weapon_attributes`）
- 生物词条的出现概率与强度（`tag_probabilities` / `mob_tag_configs`）
- 职业参数与职业树结构（`professions`）
- 生物与世界倍率（`professioncustom/example_config.json`）
- 物品职业限制（`item_professions`）
- 普通物品参与武器系统，让其拥有武器品质 / 等级 / 词条（`item`）

## 快速索引

- 只想调整武器/护甲词条数值和刷新概率：看第 1–5 章。
- 想调生物词条出现频率和强度：看第 6 章。
- 想调职业参数和职业树：看第 7 章。
- 想按维度或按生物整体放大怪物强度：看第 8 章。
- 想限制哪些职业可以使用某件物品：看第 9 章。
- 想让普通物品（例如木棍）也参与武器系统：看第 10 章。

## 1. 数据包基础路径

模组会从以下路径读取 JSON 配置（支持普通资源包 / 世界 datapack）：

- 护甲词条：
  `data/professioncustom/armor_attributes/*.json`
- 武器词条：
  `data/professioncustom/weapon_attributes/*.json`

模组自带的默认配置位于：

- `data/professioncustom/armor_attributes/default.json`
- `data/professioncustom/weapon_attributes/default.json`

你可以在 datapack 中增加新的 JSON 文件（例如 `my_pack.json`）来覆盖或扩展这些配置。

> 同一文件夹下多个 JSON 会按加载顺序合并，后加载的会覆盖同名字段。

---

## 2. qualities：按品质配置刷新概率和最大词条数

`qualities` 字段用于按武器/护甲的品质（`WeaponQuality`）配置：

- 升级时新增/强化词条的概率
- 每个品质最大可拥有的词条数量
- 该品质整体的基础数值倍率

可用的品质 key（全部为小写）：

- `common`
- `uncommon`
- `rare`
- `epic`
- `legendary`
- `mythic`

### 2.1 字段说明

在 `armor_attributes` 和 `weapon_attributes` 中结构相同，例如：

```jsonc
{
  "qualities": {
    "legendary": {
      "attribute_refresh_chance": 0.25,
      "max_attributes": 7,
      "base_value_multiplier": 1.0
    }
  }
}
```

含义：

- `attribute_refresh_chance`  (0.0 ~ 1.0)
  - 升级时，进行「新增词条或强化词条」判定的概率。
- `max_attributes`  (整数)
  - 该品质最多可以拥有的词条数量。
- `base_value_multiplier` (> 0)
  - 该品质的所有词条基础数值整体倍率。

如果某个品质在 JSON 中没有配置，对应值会从原来的 `weapon-common.toml` / 默认值中回退。

---

## 3. attributes：按词条配置基础数值

`attributes` 字段用于为每一个词条设置基础数值和随机浮动范围，key 采用 **枚举名小写** 的形式。

### 3.1 护甲词条 key 对照表

`ArmorAttribute` → JSON key：

- `HEAVY_ARMOR` → `"heavy_armor"`
- `DAMAGE_LIMIT` → `"damage_limit"`
- `THORNS` → `"thorns"`
- `FIXED_DAMAGE` → `"fixed_damage"`
- `SLOW` → `"slow"`
- `STUN` → `"stun"`
- `REGEN` → `"regen"`
- `EXPLOSIVE` → `"explosive"`
- `SOUL_IMMUNE` → `"soul_immune"`
- `BERSERK` → `"berserk"`
- `SPEED` → `"speed"`
- `REPAIR` → `"repair"`
- `DODGE` → `"dodge"`
- `SHOCK` → `"shock"`

### 3.2 武器词条 key 对照表

`WeaponAttribute` → JSON key：

- `ATTACK_DAMAGE` → `"attack_damage"`
- `CRITICAL_RATE` → `"critical_rate"`
- `CRITICAL_DAMAGE` → `"critical_damage"`
- `FIRE_DAMAGE` → `"fire_damage"`
- `ICE_DAMAGE` → `"ice_damage"`
- `SOUL_DAMAGE` → `"soul_damage"`
- `DAMAGE_PER_SECOND` → `"damage_per_second"`
- `LIFESTEAL_RATE` → `"lifesteal_rate"`
- `LIFESTEAL_MULTIPLIER` → `"lifesteal_multiplier"`
- `LIGHTBURST` → `"lightburst"`

### 3.3 字段说明

每个词条支持的字段：

```jsonc
"attributes": {
  "dodge": {
    "base": 3.0,
    "random_factor_min": 0.8,
    "random_factor_max": 1.2,
    "reverse_scale": false,
    "refresh_weight": 1.0
  }
}
```

- `base` (必选)：词条的基础数值。
- `random_factor_min` / `random_factor_max` (可选)：生成随机数值时的随机系数范围，默认为 `0.8` / `1.2`。
- `reverse_scale` (仅护甲可用，可选)：
  - `true`：该词条属于“数值越大越差”的类型（如 `damage_limit`、`fixed_damage`），品质和等级越高数值会被压低。
  - `false`：正常成长，品质/等级越高数值越大。
- `refresh_weight` (可选)：控制**该词条在“首次生成 / 升级新增 / 升级强化”时被抽中的相对权重**，默认 `1.0`。
  - 数值越大，该词条在同品质下越容易出现或被强化。
  - 设置为 `0.0` 时，表示该词条几乎不会被随机抽到（仍可以通过指令或其他逻辑手动赋值）。

---

### 3.4 按词条控制刷新概率权重示例（refresh_weight）

`refresh_weight` 在护甲与武器的 `armor_attributes` / `weapon_attributes` 中都可用，用于在同品质内细调**具体哪个词条更容易被刷到**。示例：

```jsonc
{
  "attributes": {
    "critical_rate": {
      "base": 5.0,
      "refresh_weight": 5.0
    },
    "lightburst": {
      "base": 1.0,
      "refresh_weight": 0.2
    }
  }
}
```

- 在上述配置中：
  - `critical_rate` 的权重远大于默认值 `1.0`，因此在同品质内更容易被新增/强化。
  - `lightburst` 的权重只有 `0.2`，会变得十分稀有，除非其他词条都被禁用或权重很低。

整体刷新流程可以理解为：

- `qualities.*.attribute_refresh_chance`：先决定本次升级是否触发“新增/强化词条”。
- `attributes.*.refresh_weight`：在触发后，决定**具体选中哪个词条**的相对概率。

---

## 4. 实战例子

### 4.1 提升史诗/传说品质的强度

```jsonc
{
  "qualities": {
    "epic": {
      "attribute_refresh_chance": 0.30,
      "max_attributes": 7,
      "base_value_multiplier": 1.3
    },
    "legendary": {
      "attribute_refresh_chance": 0.50,
      "max_attributes": 9,
      "base_value_multiplier": 1.5
    }
  }
}
```

### 4.2 强化护甲的闪避和反甲

```jsonc
{
  "attributes": {
    "dodge": {
      "base": 8.0,
      "random_factor_min": 0.9,
      "random_factor_max": 1.1
    },
    "thorns": {
      "base": 10.0,
      "random_factor_min": 0.8,
      "random_factor_max": 1.2
    }
  }
}
```

---

## 5. 与原 toml 配置的关系

- `professioncustom-common.toml` 仍然用于职业系统、生物等级等配置。
- `weapon-common.toml` 中原有的“词条刷新概率 / 最大词条数”现在作为兜底：
  - 若数据包中未配置某个品质，对应值会回退到 toml 的配置。
  - 若你希望完全由数据包控制，建议在数据包中为所有品质写上 `qualities` 配置。

---

## 6. 生物词条：出现概率与强度

### 6.1 生物词条出现概率：`tag_probabilities`

- 路径：`data/professioncustom/tag_probabilities/*.json`
- 示例：`data/professioncustom/tag_probabilities/example_tag_probabilities.json`

结构：

```jsonc
{
  "tag_probabilities": {
    "professioncustom:tag_cold": 0.3,
    "professioncustom:tag_resurrect": 0.25,
    "professioncustom:tag_brutal": 0.35,
    "professioncustom:tag_explosive": 0.2,
    "professioncustom:tag_greedy": 0.25,
    "professioncustom:tag_vampire": 0.3,
    "professioncustom:tag_lightburst": 0.25,
    "professioncustom:tag_frenzy": 0.3,
    "professioncustom:tag_swift": 0.35,
    "professioncustom:tag_unmatched": 0.15,
    "professioncustom:tag_soul": 0.2,
    "professioncustom:tag_teleport": 0.25,
    "professioncustom:tag_celestial": 0.2,
    "professioncustom:tag_leader": 0.25,
    "professioncustom:tag_desperate": 0.1
  }
}
```

含义：

- key：生物标签 ID（实体上的 tag 字符串），例如 `professioncustom:tag_cold`。
- value：`0.0 ~ 1.0`，用于控制**随机生成怪物标签时的出现概率/权重**。数值越高，对应标签越容易被刷在怪物身上。

使用方式示例：

```jsonc
{
  "tag_probabilities": {
    "professioncustom:tag_cold": 0.6,
    "professioncustom:tag_desperate": 0.02
  }
}
```

放在 `data/professioncustom/tag_probabilities/my_pack.json`，会覆盖默认值。

---

### 6.2 生物词条强度：`mob_tag_configs`

- 路径：`data/professioncustom/mob_tag_configs/*.json`
- 默认示例：`data/professioncustom/mob_tag_configs/default.json`
- 对应管理器：`MobTagConfigManager`

基础结构：

```jsonc
{
  "tags": {
    "<tag_id>": {
      "<参数名>": 数值
    }
  }
}
```

示例（节选）：

```jsonc
{
  "tags": {
    "professioncustom:tag_explosive": {
      "base_power": 2.0,
      "power_per_level": 0.5,
      "random_extra_base": 1.0,
      "random_extra_per_level": 0.1,
      "fire_base": 0.2,
      "fire_per_level": 0.1,
      "particle_base": 15,
      "particle_per_level": 5
    },
    "professioncustom:tag_brutal": {
      "damage_bonus_base": 0.2,
      "damage_bonus_per_level": 0.1
    },
    "professioncustom:tag_cold": {
      "attack_freeze_chance_base": 0.4,
      "attack_freeze_chance_per_level": 0.1,
      "attack_slow_duration_base": 40,
      "attack_slow_duration_per_level": 15,
      "attack_slow_amplifier_break_level": 3,
      "attack_particle_base": 8,
      "attack_particle_per_level": 2,
      "aura_range_base": 3.0,
      "aura_range_per_level": 0.5,
      "aura_chance_base": 0.15,
      "aura_chance_per_level": 0.05,
      "aura_duration_base": 30,
      "aura_duration_per_level": 10
    }
  }
}
```

常见标签字段含义示例：

- `professioncustom:tag_explosive`
  - `base_power` / `power_per_level`：基础爆炸威力与每级增加量。
  - `random_extra_base` / `random_extra_per_level`：额外随机威力上限。
  - `fire_base` / `fire_per_level`：点燃概率基数与每级增加。
  - `particle_base` / `particle_per_level`：爆炸粒子数量。

- `professioncustom:tag_brutal`
  - `damage_bonus_base` / `damage_bonus_per_level`：额外伤害比例。

- `professioncustom:tag_cold`
  - `attack_freeze_chance_base` / `attack_freeze_chance_per_level`：攻击冰冻触发概率。
  - `aura_range_base` / `aura_range_per_level`：寒气光环范围。
  - `aura_chance_base` / `aura_chance_per_level`：光环触发概率。
  - 其他字段控制减速持续时间/等级等。

如果某个字段未配置，会使用代码内默认值，保持当前手感不变；你只需要改你关心的数值即可。

---

### 6.3 推荐调参顺序

1. 在 `tag_probabilities` 下调整各标签出现频率。
2. 在 `mob_tag_configs` 下微调具体标签强度（爆炸威力、额外伤害、减速范围等）。
3. `/reload` 后进游戏实测，根据体感继续收紧或放大数值。

---

## 7. 职业配置：`professions`

### 7.1 路径与结构

- 路径：`data/professioncustom/professions/*.json`
- 每个 JSON 定义一个职业，例如：`berserker.json`（狂战士）。

示例（`berserker.json`）：

```jsonc
{
  "name": "berserker",
  "displayName": "狂战士",
  "isNormal": false,
  "upperProfession": "warrior",
  "professionLevel": 2,
  "maxLevel": 15,
  "maxExp": 200,
  "multiplier": 3,
  "health": 30,
  "armor": 3,
  "damage": 6,
  "damageSpeed": 1
}
```

这些字段由职业系统读取，用于决定职业树结构和属性成长。

### 7.2 字段说明

- **`name`**  
  职业内部 ID，用于逻辑和配置引用。建议与文件名一致，例如 `berserker.json` → `"name": "berserker"`。

- **`displayName`**  
  职业显示名称。你也可以改为翻译 key 再在语言文件中配置。

- **`isNormal`**  
  是否为基础职业：  
  - `true`：初始普通职业（如战士、法师）。  
  - `false`：进阶/高阶职业（如狂战士）。

- **`upperProfession`**  
  上位职业 / 母职业 ID，用于职业树结构：  
  - 例如 `"upperProfession": "warrior"` 表示该职业从战士进阶而来。  
  - 基础职业可以设置为空或不写（按 `ProfessionConfig` 的实现处理）。

- **`professionLevel`**  
  职业所在阶级，例如：  
  - `1`：基础职业  
  - `2`：二转职业（如 `berserker`）。

- **`maxLevel`**  
  该职业自身的等级上限。

- **`maxExp`**  
  该职业当前阶段的经验上限，具体经验曲线由 `ProfessionConfig` 与 `professioncustom-common.toml` 中经验配置共同决定。

- **`multiplier`**  
  职业整体倍率系数，用于放大或压缩该职业的属性成长。数值越大，这个职业整体越强。

- **`health`**  
  职业的基础生命参数，通常会参与类似：  
  `最终生命 ≈ 等级 * health * healthCoefficient * multiplier`  的公式。

- **`armor`**  
  职业的基础护甲参数，对应 `professioncustom-common.toml` 中的 `armorCoefficient`。

- **`damage`**  
  职业的基础攻击参数，对应 `damageCoefficient`。

- **`damageSpeed`**  
  职业的基础攻速参数，对应 `damageSpeedCoefficient`。

> 具体公式以 `ProfessionConfig` 与 `professioncustom-common.toml` 的实现为准，上面是常见写法的说明。

### 7.3 新增一个职业的步骤

1. 在 `data/professioncustom/professions/` 下新建一个 JSON 文件，例如 `paladin.json`。
2. 写入类似结构：

```jsonc
{
  "name": "paladin",
  "displayName": "圣骑士",
  "isNormal": false,
  "upperProfession": "warrior",
  "professionLevel": 2,
  "maxLevel": 20,
  "maxExp": 250,
  "multiplier": 2,
  "health": 40,
  "armor": 6,
  "damage": 4,
  "damageSpeed": 0.9
}
```

3. `/reload` 或重启游戏，让数据包生效。  
4. 按职业系统实现，在相应位置（GUI / 命令 / 配置）允许玩家转职到新职业。

### 7.4 职业与全局配置的关系

- 职业 JSON：决定每个职业自身的基础参数与成长倾向（偏坦克、偏输出、偏攻速等）。
- `professioncustom-common.toml`：通过 `healthCoefficient` / `armorCoefficient` / `damageCoefficient` / `damageSpeedCoefficient` 等系数，把职业参数转换成实际属性。

你可以通过修改职业 JSON 调整职业之间的相对强度，再通过 toml 系数整体放大或压缩整个职业系统的强度。

---

## 8. 生物与世界倍率配置：`professioncustom/example_config.json`

### 8.1 路径与结构

- 路径示例：`data/professioncustom/professioncustom/example_config.json`
- 用于控制不同维度、不同生物的属性倍率。

示例：

```jsonc
{
  "dimension_bonuses": {
    "minecraft:overworld": 1.0,
    "minecraft:nether": 1.5,
    "minecraft:end": 2.0,
    "minecraft:the_nether": 1.5
  },
  "mob_bonuses": {
    "minecraft:zombie": {
      "health_multiplier": 1.5,
      "armor_multiplier": 1.0,
      "armor_toughness_multiplier": 1.0,
      "attack_damage_multiplier": 1.2
    },
    "minecraft:skeleton": {
      "health_multiplier": 1.2,
      "armor_multiplier": 1.0,
      "armor_toughness_multiplier": 1.0,
      "attack_damage_multiplier": 1.5
    },
    "minecraft:creeper": {
      "health_multiplier": 1.0,
      "armor_multiplier": 1.0,
      "armor_toughness_multiplier": 1.0,
      "attack_damage_multiplier": 2.0
    }
  }
}
```

### 8.2 维度倍率：`dimension_bonuses`

- 结构：`"dimension_bonuses": { <dimension_id>: <multiplier>, ... }`
- key：维度 ID，例如：
  - `"minecraft:overworld"`：主世界
  - `"minecraft:the_nether"`：下界
  - `"minecraft:end"`：末地
- value：数值倍率，一般用于整体放大/缩小该维度内怪物的属性（如生命、伤害等）。

可以理解为：

> 最终属性 ≈ 原始属性 * dimension_bonuses[当前维度]

如果某个维度没有配置，通常按 1.0 处理（以具体实现为准）。

### 8.3 生物倍率：`mob_bonuses`

- 结构：`"mob_bonuses": { <entity_id>: { ... }, ... }`
- key：生物 ID，例如：
  - `"minecraft:zombie"`
  - `"minecraft:skeleton"`
  - `"minecraft:creeper"`

每个生物可以单独配置：

- `health_multiplier`：生命值倍率。
- `armor_multiplier`：护甲值倍率。
- `armor_toughness_multiplier`：护甲韧性倍率。
- `attack_damage_multiplier`：攻击伤害倍率。

概念上的应用方式：

> 最终生命 ≈ 原生命 * health_multiplier * 维度倍率  
> 最终护甲 ≈ 原护甲 * armor_multiplier  
> 最终伤害 ≈ 原伤害 * attack_damage_multiplier * 维度倍率

具体公式以代码实现为准，上述是常规用法的说明。

### 8.4 调整示例

- 提升末地整体危险度：

```jsonc
"dimension_bonuses": {
  "minecraft:overworld": 1.0,
  "minecraft:the_nether": 1.5,
  "minecraft:end": 3.0
}
```

- 只增强僵尸的血量，不改其他怪物：

```jsonc
"mob_bonuses": {
  "minecraft:zombie": {
    "health_multiplier": 2.0,
    "armor_multiplier": 1.0,
    "armor_toughness_multiplier": 1.0,
    "attack_damage_multiplier": 1.0
  }
}
```

搭配前面的职业配置、武器/护甲词条配置、生物标签配置一起调整，可以精细地控制不同维度和不同怪物的整体难度。

### 8.5 禁用某些生物参与等级/词条系统：`enable`

从 `mob_bonuses` 中，你还可以**直接在数据包里禁用某些生物，让它们完全不参与本模组的“生物等级 + 生物词条系统”**。

- 支持的额外字段：

  - `enable`（可选，布尔或字符串）
    - `false` 或 `"false"`：表示禁用该生物。
    - 其他情况或未写：视为启用（默认行为）。

- 当某个生物在 `mob_bonuses` 里配置为 `enable = false` 时，该生物将被视为**不录入本系统**：

  - 不会被计算和缓存等级，`MobLevelManager.getEntityLevel` 对它返回 `1`。
  - 不会被随机分配任何生物词条，`MobTagManager` 对它返回空标签。
  - 生物词条相关的所有效果（爆炸、复活、寒冷、吸血、灵魂等）不会在它身上触发。
  - 不会显示等级前缀 `[Lv.X]`，也不会在 `EntityNameRenderer` 中绘制额外的等级/词条悬浮信息。

#### 8.5.1 使用示例

例如，你希望僵尸完全不参与等级和词条系统，只作为普通怪：

```jsonc
{
  "mob_bonuses": {
    "minecraft:zombie": {
      "enable": "false",
      "health_multiplier": 1.5,
      "armor_multiplier": 1.0,
      "armor_toughness_multiplier": 1.0,
      "attack_damage_multiplier": 1.2
    }
  }
}
```

只要 `enable` 为 `false` / `"false"`，这个生物就会被当成“被禁用的生物”，即：

- 不受本模组生物等级系统影响（等级始终视作 1，且不加等级属性）。
- 不受本模组生物词条系统影响（不生成、不生效）。

> 说明：仍然保留 `professioncustom-common.toml` 中的 `excludedMobs` 列表，作为服务器/整合包作者的附加黑名单；但在一般情况下，**推荐优先通过数据包里的 `enable` 字段来控制某个生物是否参与系统**。

---

## 9. 物品职业限制配置：`item_professions`

### 9.1 路径与结构

- 路径：`data/professioncustom/item_professions/*.json`
- 每个 JSON 绑定一个物品与可使用该物品的职业列表，用于职业限制展示与判定。

示例（`minecraft_diamond_hoe.json`）：

```jsonc
{
  "itemId": "minecraft:diamond_hoe",
  "professions": [
    "mage",
    "wizard"
  ],
  "descriptionPrefix": "职业限制: ",
  "showInLore": true,
  "loreColor": "purple"
}
```

`ItemProfessionConfigManager` 会读取该目录，提供职业与物品的匹配查询。物品 tooltip 中也可以根据这些字段显示职业限制说明。

### 9.2 字段说明

- **`itemId`**  
  物品 ID，格式为 `"modid:itemname"`，例如：  
  - `"minecraft:diamond_hoe"`  
  - `"professioncustom:my_sword"`

- **`professions`**  
  数组，表示**允许使用该物品的职业 ID 列表**。  
  这些 ID 应与职业配置中 `name` 字段对应，例如：  
  - `"warrior"`、`"mage"`、`"berserker"` 等。

- **`descriptionPrefix`**  
  可选，显示在物品描述中的前缀文本，例如 `"职业限制: "`。  
  可以用于在 tooltip 中渲染类似：`职业限制: 法师、巫师`。

- **`showInLore`**  
  布尔值，表示是否在物品的 lore/描述中显示职业限制信息。  
  - `true`：在物品说明中显示职业限制行。  
  - `false`：仅在逻辑层做职业限制，不显示文本。

- **`loreColor`**  
  文本颜色字符串，用于控制职业限制这一行的颜色，例如：  
  - `"purple"`、`"aqua"`、`"gold"` 等（需要与你在渲染时的颜色解析逻辑匹配）。

### 9.3 使用示例

1. 限制一把自定义剑只能由战士和狂战士使用：

```jsonc
{
  "itemId": "professioncustom:warrior_sword",
  "professions": [
    "warrior",
    "berserker"
  ],
  "descriptionPrefix": "职业限制: ",
  "showInLore": true,
  "loreColor": "red"
}
```

2. 让一根法杖只允许法师系职业使用，但不在物品说明中显示：

```jsonc
{
  "itemId": "professioncustom:magic_staff",
  "professions": [
    "mage",
    "wizard"
  ],
  "showInLore": false
}
```

### 9.4 与职业系统的协同

- 职业 ID 需要与 `professions` 目录中对应职业 JSON 的 `name` 字段一致。  
- 判断流程通常是：
  1. 通过 `ItemProfessionConfigManager.getProfessionsForItem(itemId)` 获取可用职业列表。  
  2. 将玩家当前职业与列表对比，决定是否允许装备/使用该物品。  
  3. 若 `showInLore = true`，在 tooltip 中展示职业限制信息，使用 `descriptionPrefix` 与 `loreColor` 渲染。

---

## 10. 物品参与武器系统：`item`

有些物品本身不是武器/装备（例如木棍、特殊道具），但你希望它们也能拥有**武器品质 / 等级 / 词条**，并参与武器经验与伤害结算。这类物品可以通过数据包配置在 `item` 目录中开启。

### 10.1 路径与加载方式

- 路径：`data/professioncustom/item/*.json`
- 每个 JSON 文件可以是：
  - **单个对象**；或
  - **对象数组**（一个文件内配置多个物品）。

模组会遍历该目录下所有 JSON 文件并加载，每个 `itemId` 只保留最后一次加载的配置。

### 10.2 顶层结构：对象 vs. 数组

1. **单个对象写法**（兼容旧版）：

```jsonc
{
  "itemId": "minecraft:stick",
  "type": "weapon"
}
```

2. **数组写法**（推荐，一个文件配置多个物品）：

```jsonc
[
  {
    "itemId": "minecraft:stick",
    "type": "weapon"
  },
  {
    "itemId": "minecraft:diamond_sword",
    "type": "weapon"
  }
]
```

> 注意：当顶层是 **数组** 时，**每个元素都必须写 `itemId`**；此时文件名不再用于推断物品 ID。

### 10.3 字段说明

每个对象（无论是单个还是数组元素）代表一条物品配置，支持字段：

- **`itemId`**（必填）
  - 物品 ID，格式：`"modid:item_name"`。
  - 示例：`"minecraft:stick"`、`"minecraft:diamond_sword"`、`"professioncustom:long_zu"`。

- **`type`**（可选，当前版本主要用于标记，默认 `"weapon"`）
  - 不写或写成空字符串时，会自动当作 `"weapon"`。
  - 当前实现中：
    - **只要物品在 `item` 配置中出现，就会被视为“可作为武器参与本模组武器系统的物品”**：
      - 主手拿起时，会为该物品初始化武器 NBT（品质、等级、经验等）；
      - 生成武器词条，并在 tooltip 中显示；
      - 使用该物品击杀怪物会获得武器经验，触发武器词条效果。
    - `type = "weapon"`：明确标记这是“按武器处理”的物品。
    - `type = "armor"`：**预留值**，当前版本仅被读入并缓存，后续版本可以在此基础上扩展“通过数据包强制按护甲逻辑处理的物品”。

> 简单理解：目前只要在 `item` 下为某个 `itemId` 写一条配置（哪怕只写 `itemId`），这个物品就会被本模组视为“武器”，享受**武器品质 / 等级 / 词条 / 经验**等全套系统；`type` 字段用于标记类型，默认即为 `"weapon"`。

### 10.4 最小配置示例

让原版木棍参与武器系统（作为武器处理）：

```jsonc
{
  "itemId": "minecraft:stick"
}
```

或在一个文件中配置多件物品：

```jsonc
[
  {
    "itemId": "minecraft:stick"
  },
  {
    "itemId": "minecraft:diamond_sword",
    "type": "weapon"
  }
]
```

保存到 `data/professioncustom/item/my_items.json`，然后在游戏中执行 `/reload`（或重启世界），即可让这些物品按武器参与本模组的武器属性与词条系统。
