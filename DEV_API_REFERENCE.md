# ProfessionCustom 前置 API 参考文档

> 面向开发者的二次开发接口说明。本文件主要介绍可被其他模组调用、作为「前置模组 API」使用的核心类与方法，便于在代码层面与 ProfessionCustom 联动。

---

## 目录

1. 武器 / 护甲 NBT 工具类
   - `WeaponNBTUtil`
   - `ArmorNBTUtil`
2. 装备词条枚举
   - `WeaponAttribute`
   - `ArmorAttribute`
3. 装备等级与经验管理
   - `WeaponLevelManager`
   - `ArmorLevelManager`
4. 生物标签与配置接口
   - `MobTagManager`
   - `MobConfig`
5. 职业系统接口
   - `ProfessionManager`
   - `Profession`
   - `ModVariables.PlayerVariables`
6. 关于「新增词条」与扩展能力的说明

---

## 1. 武器 / 护甲 NBT 工具类

### 1.1 `WeaponNBTUtil` —— 武器 NBT 工具

- **包路径**：`site.backrer.professioncustom.weapon.WeaponNBTUtil`
- **作用**：
  - 负责对 `ItemStack` 读写 ProfessionCustom 武器相关 NBT 数据：等级、经验、品质、词条列表等。
  - 适用于其他模组直接接入 ProfessionCustom 的武器系统，为自定义物品写入武器信息。

#### 1.1.1 常用方法

- **`boolean isWeapon(ItemStack stack)`**  
  判断该物品是否已经是 ProfessionCustom 格式的武器。

- **`void initializeWeapon(ItemStack stack, WeaponQuality quality)`**  
  将一个可用作武器的 `ItemStack` 初始化为 ProfessionCustom 武器：
  - 设置品质为 `quality`
  - 等级 = 1
  - 经验 = 0
  - 初始化内部 NBT 结构

- **`int getLevel(ItemStack stack)` / `void setLevel(ItemStack stack, int level)`**  
  读取 / 写入武器等级。

- **`int getExp(ItemStack stack)` / `void setExp(ItemStack stack, int exp)`**  
  读取 / 写入当前经验值（仅 NBT 层面的设置，不含升级逻辑）。

- **`WeaponQuality getQuality(ItemStack stack)` / `void setQuality(ItemStack stack, WeaponQuality quality)`**  
  读取 / 写入武器品质。

- **`Map<WeaponAttribute, Double> getAttributes(ItemStack stack)`**  
  获取武器当前所有词条及其数值映射。

- **`void setAttribute(ItemStack stack, WeaponAttribute attribute, double value)`**  
  新增或更新指定武器词条的数值。若原先不存在该词条则添加，存在则覆盖。

- **`void removeAttribute(ItemStack stack, WeaponAttribute attribute)`**  
  从武器上移除指定词条。

#### 1.1.2 典型用法示例

- **将自定义武器接入 ProfessionCustom 武器系统**：

  ```java
  ItemStack mySword = ...;

  // 初始化为 ProfessionCustom 武器，品质为 RARE
  WeaponNBTUtil.initializeWeapon(mySword, WeaponQuality.RARE);

  // 添加基础攻击力与暴击率词条
  WeaponNBTUtil.setAttribute(mySword, WeaponAttribute.ATTACK_DAMAGE, 10.0);
  WeaponNBTUtil.setAttribute(mySword, WeaponAttribute.CRITICAL_RATE, 0.15);
  ```

- **在事件中动态调整武器词条**：

  ```java
  if (WeaponNBTUtil.isWeapon(stack)) {
      double oldSoulDamage = WeaponNBTUtil.getAttributes(stack)
          .getOrDefault(WeaponAttribute.SOUL_DAMAGE, 0.0);
      WeaponNBTUtil.setAttribute(stack, WeaponAttribute.SOUL_DAMAGE, oldSoulDamage + 5.0);
  }
  ```

---

### 1.2 `ArmorNBTUtil` —— 护甲 NBT 工具

- **包路径**：`site.backrer.professioncustom.armor.ArmorNBTUtil`
- **作用**：
  - 对护甲类 `ItemStack` 读写 ProfessionCustom 护甲相关 NBT 数据：等级、经验、品质、护甲词条等。
  - 使用方式与 `WeaponNBTUtil` 完全类似，只是词条类型为 `ArmorAttribute`。

#### 1.2.1 常用方法

- `boolean isArmor(ItemStack stack)`  
- `void initializeArmor(ItemStack stack, WeaponQuality quality)`  
- `int getLevel(ItemStack stack)` / `void setLevel(ItemStack stack, int level)`  
- `int getExp(ItemStack stack)` / `void setExp(ItemStack stack, int exp)`  
- `WeaponQuality getQuality(ItemStack stack)` / `void setQuality(ItemStack stack, WeaponQuality quality)`  
- `Map<ArmorAttribute, Double> getAttributes(ItemStack stack)`  
- `void setAttribute(ItemStack stack, ArmorAttribute attribute, double value)`  
- `void removeAttribute(ItemStack stack, ArmorAttribute attribute)`  

#### 1.2.2 典型用法示例

- **初始化护甲并赋予多种词条**：

  ```java
  ItemStack chest = ...;

  ArmorNBTUtil.initializeArmor(chest, WeaponQuality.LEGENDARY);
  ArmorNBTUtil.setAttribute(chest, ArmorAttribute.HEAVY_ARMOR, 5.0);
  ArmorNBTUtil.setAttribute(chest, ArmorAttribute.DAMAGE_LIMIT, 10.0);
  ArmorNBTUtil.setAttribute(chest, ArmorAttribute.THORNS, 3.0);
  ```

---

## 2. 装备词条枚举

### 2.1 `WeaponAttribute` —— 武器词条枚举

- **包路径**：`site.backrer.professioncustom.weapon.WeaponAttribute`
- **作用**：
  - 定义了所有内置的武器词条，如 `ATTACK_DAMAGE`、`CRITICAL_RATE`、`FIRE_DAMAGE` 等。
  - 可在代码中安全引用这些词条，避免魔法字符串。

#### 2.1.1 常用成员与方法

- 使用 `WeaponAttribute.ATTACK_DAMAGE` 等枚举常量。
- **`static WeaponAttribute valueOf(String name)`**  
  通过名称（如 `"ATTACK_DAMAGE"`）获取对应枚举项。

- **`String getTranslationKey()`**  
  返回本地化键，用于语言文件。

- **`String getDisplayName()`**  
  返回简体中文显示名（如“攻击力”）。

- **`boolean isNumeric()`**  
  标识该词条是否带有数值。

- **`Component getDisplayWithValue(double value)`**  
  返回带颜色与数值的展示文本，可用于物品描述。

#### 2.1.2 使用建议

- 外部模组在 **配置 / 代码中引用现有词条** 时，建议统一使用 `WeaponAttribute` 枚举，不直接自己拼字符串。
- 若要从配置文件中读取到字符串后转换为枚举，可用：

  ```java
  WeaponAttribute attr = WeaponAttribute.valueOf(attrNameFromConfig);
  ```

---

### 2.2 `ArmorAttribute` —— 护甲词条枚举

- **包路径**：`site.backrer.professioncustom.armor.ArmorAttribute`
- **作用**：
  - 定义所有内置护甲词条，如 `HEAVY_ARMOR`、`DAMAGE_LIMIT`、`THORNS` 等。

- 常用方法与 `WeaponAttribute` 基本一致：
  - `static ArmorAttribute valueOf(String name)`
  - `String getTranslationKey()`
  - `String getDisplayName()`
  - `boolean isNumeric()`
  - `Component getDisplayWithValue(double value)`

---

## 3. 装备等级与经验管理

### 3.1 `WeaponLevelManager` —— 武器等级管理

- **包路径**：`site.backrer.professioncustom.weapon.WeaponLevelManager`
- **作用**：
  - 负责武器经验增加与升级逻辑（包括多级连升、更新词条、刷新耐久等）。
  - 建议外部模组在「给武器加经验」时使用本类，而不是直接修改 NBT。

#### 3.1.1 常用方法

- **`int getExpRequiredForLevel(int level)`**  
  获取从指定等级升级到下一等级所需的经验值。

- **`boolean addExp(ItemStack stack, int exp)`**  
  为武器增加经验：
  - 若物品不是 ProfessionCustom 武器，则返回 `false`；
  - 处理经验累加与升级；
  - 在升级时调用 `WeaponTagManager.updateAttributesOnLevelUp`，并可刷新耐久；
  - 返回值表示本次是否发生升级。

- **`int getExpFromKill(int mobLevel)`**  
  根据怪物等级计算默认经验奖励，可供自定义逻辑参考。

#### 3.1.2 典型用法

- **在你自己的伤害事件中给武器经验**：

  ```java
  ItemStack weapon = player.getMainHandItem();
  if (WeaponNBTUtil.isWeapon(weapon)) {
      boolean leveled = WeaponLevelManager.addExp(weapon, 20);
      if (leveled) {
          // 自定义：例如发送一条升级提示
      }
  }
  ```

---

### 3.2 `ArmorLevelManager` —— 护甲等级管理

- **包路径**：`site.backrer.professioncustom.armor.ArmorLevelManager`
- **作用**：
  - 与 `WeaponLevelManager` 类似，用于护甲经验与等级管理。

#### 3.2.1 常用方法

- `int getExpRequiredForLevel(int level)`
- `boolean addExp(ItemStack stack, int exp)`

#### 3.2.2 典型用法

- **受到伤害时给护甲经验**：

  ```java
  ItemStack chest = player.getInventory().armor.get(2); // 胸甲槽
  if (ArmorNBTUtil.isArmor(chest)) {
      ArmorLevelManager.addExp(chest, 10);
  }
  ```

---

## 4. 生物标签与配置接口

### 4.1 `MobTagManager` —— 生物标签管理

- **包路径**：`site.backrer.professioncustom.MobTagManager`
- **作用**：
  - 统一管理 ProfessionCustom 定义的怪物标签（如 COLD、SOUL、EXPLOSIVE 等）。
  - 提供查询生物当前拥有标签、等级以及更新标签的能力，便于其他模组按标签做行为联动。

#### 4.1.1 内部枚举 `MobTag`

- **常用方法**：
  - `String getTagId()` —— 返回标签 ID 字符串（通常形如 `"professioncustom:tag_cold"`）。
  - `Component getDisplayName()` / `String getDisplayNameString()` —— 用于显示的本地化名称。

#### 4.1.2 常用静态方法

- **`Map<String, Integer> getEntityTagLevels(Entity entity)`**  
  返回该实体所有 ProfessionCustom 标签及其等级（`标签ID -> 等级`）。

- **`Set<String> getEntityTags(Entity entity)`**  
  仅返回标签 ID 集合。

- **`int getTagLevel(Entity entity, MobTag tag)`**  
- **`int getTagLevel(Entity entity, String tagId)`**  
  获取某个标签的等级（不存在时通常为 0）。

- **`boolean hasTag(Entity entity, MobTag tag)`**  
- **`boolean hasTag(Entity entity, String tagId)`**  
  判断实体是否拥有指定标签。

- **`List<TagDisplayInfo> getTagDisplayInfo(Entity entity)`**  
  返回适合用于 UI / HUD 的标签展示信息列表。

- **`void updateEntityTagLevels(UUID entityId, Map<String, Integer> tagLevels)`**  
  手动更新某实体的标签等级映射，可用于：
  - 自定义刷怪逻辑中注入特定标签；
  - 外部模组根据自己的规则动态提升某怪的标签等级。

#### 4.1.3 典型用法

- **基于标签联动自定义效果**：

  ```java
  if (MobTagManager.hasTag(entity, MobTagManager.MobTag.SOUL)) {
      // 例如：对具有 SOUL 标签的怪物造成额外伤害
  }
  ```

- **在你自己的刷怪逻辑中强制赋予标签**：

  ```java
  UUID id = entity.getUUID();
  Map<String, Integer> tags = new HashMap<>(MobTagManager.getEntityTagLevels(entity));

  tags.put(MobTagManager.MobTag.EXPLOSIVE.getTagId(), 5);
  MobTagManager.updateEntityTagLevels(id, tags);
  ```

---

### 4.2 `MobConfig` —— 生物配置访问

- **包路径**：`site.backrer.professioncustom.MobConfig`
- **作用**：
  - 访问通过数据包加载的怪物相关配置，例如：维度等级加成、标签生成概率等。
  - 外部模组可以使用这些配置来保证与 ProfessionCustom 的世界强度设定一致。

#### 4.2.1 常用方法

- **`double getTagProbability(String tagId)`**  
  返回指定标签的生成概率（若未在配置中定义，通常返回默认值或 0）。

- 其他方法（如维度等级加成）可用于根据世界环境对怪物强度进行统一调整。

---

## 5. 职业系统接口

### 5.1 `ProfessionManager` —— 职业管理

- **包路径**：`site.backrer.professioncustom.profession.ProfessionManager`
- **作用**：
  - 管理所有已注册职业，提供查询与应用属性加成的主要入口。

#### 5.1.1 常用方法

- **`Profession getProfessionByName(String name)`**  
  根据职业 ID（如 `"warrior"`）获取 `Profession` 对象。

- **`Map<String, Profession> getAllProfessions()`**  
  返回所有已加载的职业（`职业ID -> Profession`）。

- **`List<Profession> getNormalProfessions()`**  
  返回所有基础职业列表。

- **`List<Profession> getAdvancedProfessions(String baseProfessionName)`**  
  返回指定基础职业的所有进阶职业。

- **`void setAttributeByProfessionInPlayer(String professionName, Player player)`**  
  按指定职业为玩家应用属性加成（血量、护甲、伤害、攻速等），内部通过 AttributeModifier 写入。

> 注意：该方法会对玩家属性做实际修改，外部调用时需避免重复叠加，一般在**职业变化或等级变化时重新应用**即可。

---

### 5.2 `Profession` —— 职业数据结构

- **包路径**：`site.backrer.professioncustom.profession.Profession`
- **作用**：
  - 表示单个职业的数据载体，包含名称、类型、最大等级、成长曲线与属性加成等。

#### 5.2.1 常用字段访问方法

- `String getName()` —— 职业 ID。
- `String getDisplayName()` —— 职业显示名。
- `boolean isNormal()` —— 是否为基础职业。
- `Profession getUpperProfession()` —— 若为进阶职业，则返回其上级职业。
- `int getMaxLevel()` —— 职业最大等级。
- `double getHealth()` / `double getArmor()` / `double getDamage()` / `double getDamageSpeed()` —— 各属性加成基础值（通常随等级叠加）。

> 外部模组可以使用这些 getter 来生成自定义职业介绍 GUI、Wiki 等。

---

### 5.3 `ModVariables.PlayerVariables` —— 玩家职业 Capability

- **包路径**：`site.backrer.professioncustom.profession.network.ModVariables.PlayerVariables`
- **作用**：
  - 作为玩家身上的能力（Capability），存储 ProfessionCustom 职业系统的核心数据：职业名、等级、当前经验、升级所需经验等。
  - 外部模组可通过标准 Capability 方式获得并修改玩家职业信息。

#### 5.3.1 获取方式

```java
Player player = ...;
ModVariables.PlayerVariables vars = player
    .getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
    .orElse(new ModVariables.PlayerVariables());
```

#### 5.3.2 常用方法

- **`boolean hasProfession()`**  
  玩家是否已经选择了某个职业。

- **`void setProfession(String professionName, Player player)`**  
  设置玩家职业：
  - 将 `professionName` 更新为指定值；
  - 重置职业等级为 1；
  - 重置当前经验；
  - 计算并设置最大经验；
  - 同步到客户端并调用 `ProfessionManager.setAttributeByProfessionInPlayer` 应用属性。

- **`boolean addExperience(int amount, Player player)`**  
  为当前职业增加经验：
  - 处理升级逻辑（可能多级连升）；
  - 最高等级后不再增加等级；
  - 返回是否发生升级；
  - 自动同步客户端。

- **`float getExperienceProgress()`**  
  返回当前经验进度 `0.0F ~ 1.0F`，适合用来绘制经验条。

#### 5.3.3 典型用法

- **在你自己的事件中为职业增加经验**：

  ```java
  player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
      vars.addExperience(50, player);
  });
  ```

- **通过自定义 GUI 修改玩家职业**：

  ```java
  player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
      vars.setProfession("warrior", player);
  });
  ```

---

## 6. 关于「新增词条」与扩展能力

### 6.1 现有实现的限制

- `WeaponAttribute` 与 `ArmorAttribute` 均为 Java `enum`：
  - 在运行时无法由其他模组动态注入新的枚举常量；
  - 因此，**真正意义上的「新增一种全新的词条类型」需要修改 ProfessionCustom 源码本身**（在本项目中扩展枚举与对应逻辑）。

### 6.2 推荐的扩展方式

在不修改 ProfessionCustom 源码的前提下，其他模组仍然可以做大量扩展：

- **组合现有词条**：
  - 利用已有的攻击力、暴击、元素伤害、护甲、反伤等词条组合出自定义装备效果。
  - 在自己的模组逻辑中，读取 `WeaponNBTUtil` / `ArmorNBTUtil` 的属性值，根据组合触发独特技能或效果。

- **使用数据包与配置联动**：
  - 借助 ProfessionCustom 已有的数据包配置能力，定义不同维度、怪物、标签的难度/奖励，然后在你自己的模组里读取 `MobConfig` 和 `MobTagManager` 数据进行统一难度控制。

- **通过 Capability 与事件系统联动**：
  - 读取/修改 `PlayerVariables` 实现自定义职业升级条件、额外加成等。
  - 在 Forge 事件中基于职业、武器/护甲词条以及怪物标签触发你自己的玩法逻辑。

---

本文件仅列出最适合作为前置 API 使用的主要类与方法。若你在二次开发过程中还需要更底层的接口说明，可以继续在源码中查阅对应类的实现细节，并在此基础上扩展。
