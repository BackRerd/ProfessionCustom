package site.backrer.professioncustom.profession;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import site.backrer.professioncustom.MobConfig;

import java.util.Collections;
import java.util.List;

/**
 * 职业系统配置类，用于管理全局职业设置
 */
public class ProfessionConfig {
    
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    // 基础配置
    public static final ForgeConfigSpec.BooleanValue ENABLE_PROFESSION_SYSTEM;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PROFESSION_RESTRICTION;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CLASS_LEVELING;
    
    // 经验配置
    public static final ForgeConfigSpec.IntValue BASE_EXP_FOR_KILL;
    public static final ForgeConfigSpec.DoubleValue EXP_MULTIPLIER_PER_LEVEL;
    
    // 生物等级系统配置
    public static final ForgeConfigSpec.BooleanValue ENABLE_MOB_LEVELING;
    public static final ForgeConfigSpec.DoubleValue BASE_LEVEL_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue DISTANCE_LEVEL_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue SHOW_LEVEL_NAME_TAG;
    public static final ForgeConfigSpec.BooleanValue SHOW_EXTRA_LEVEL_TAG;
    public static final ForgeConfigSpec.DoubleValue HEALTH_BONUS_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ARMOR_BONUS_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ARMOR_TOUGHNESS_BONUS_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE_BONUS_PER_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_MOBS;
    
    // 职业切换配置
    public static final ForgeConfigSpec.BooleanValue ALLOW_PROFESSION_CHANGE;
    public static final ForgeConfigSpec.IntValue PROFESSION_CHANGE_COST;
    
    // 属性配置
    public static final ForgeConfigSpec.DoubleValue MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue MAX_ARMOR;
    public static final ForgeConfigSpec.DoubleValue MAX_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue MAX_DAMAGE_SPEED;
    
    // 计算公式系数配置
    public static final ForgeConfigSpec.DoubleValue HEALTH_COEFFICIENT;
    public static final ForgeConfigSpec.DoubleValue ARMOR_COEFFICIENT;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_COEFFICIENT;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_SPEED_COEFFICIENT;
    
    // 初始化配置
    static {
        BUILDER.push("general");
        
        ENABLE_PROFESSION_SYSTEM = BUILDER
                .comment("是否启用职业系统")
                .define("enableProfessionSystem", true);
        
        ENABLE_PROFESSION_RESTRICTION = BUILDER
                .comment("是否启用职业限制（不同职业使用不同武器/装备）")
                .define("enableProfessionRestriction", false);
        
        ENABLE_CLASS_LEVELING = BUILDER
                .comment("是否启用职业等级系统")
                .define("enableClassLeveling", true);
        
        BUILDER.pop();
        
        BUILDER.push("attribute_limits");
        
        MAX_HEALTH = BUILDER
                .comment("最大生命值限制")
                .defineInRange("maxHealth", 200.0, 20.0, 1000.0);
        
        MAX_ARMOR = BUILDER
                .comment("最大护甲值限制")
                .defineInRange("maxArmor", 30.0, 0.0, 100.0);
        
        MAX_DAMAGE = BUILDER
                .comment("最大攻击力限制")
                .defineInRange("maxDamage", 50.0, 1.0, 200.0);
        
        MAX_DAMAGE_SPEED = BUILDER
                .comment("最大攻击速度限制")
                .defineInRange("maxDamageSpeed", 3.8, 1.0, 10.0);
        
        BUILDER.pop();
        
        BUILDER.push("attribute_formulas");
        
        HEALTH_COEFFICIENT = BUILDER
                .comment("生命值计算公式系数（等级 * 职业生命值 * 系数）")
                .defineInRange("healthCoefficient", 0.2, 0.01, 1.0);
        
        ARMOR_COEFFICIENT = BUILDER
                .comment("护甲值计算公式系数（等级 * 职业护甲 * 系数）")
                .defineInRange("armorCoefficient", 0.2, 0.01, 1.0);
        
        DAMAGE_COEFFICIENT = BUILDER
                .comment("攻击力计算公式系数（等级 * 职业攻击力 * 系数）")
                .defineInRange("damageCoefficient", 0.2, 0.01, 1.0);
        
        DAMAGE_SPEED_COEFFICIENT = BUILDER
                .comment("攻击速度计算公式系数（等级 * 职业攻击速度 * 系数）")
                .defineInRange("damageSpeedCoefficient", 0.05, 0.01, 0.5);
        
        BUILDER.pop();
        
        BUILDER.push("experience");
        
        BASE_EXP_FOR_KILL = BUILDER
                .comment("击杀怪物获得的基础经验值")
                .defineInRange("baseExpForKill", 10, 0, 1000);
        
        EXP_MULTIPLIER_PER_LEVEL = BUILDER
                .comment("每升一级所需经验的倍率")
                .defineInRange("expMultiplierPerLevel", 1.5, 1.0, 5.0);
        
        BUILDER.pop();
        
        BUILDER.push("mob_leveling");
        
        ENABLE_MOB_LEVELING = BUILDER
                .comment("是否启用生物等级系统")
                .define("enableMobLeveling", true);
        
        BASE_LEVEL_DISTANCE = BUILDER
                .comment("每级所需的基础距离（方块）")
                .defineInRange("baseLevelDistance", 100.0, 10.0, 1000.0);
        
        DISTANCE_LEVEL_MULTIPLIER = BUILDER
                .comment("距离等级系数，影响等级增长速度")
                .defineInRange("distanceLevelMultiplier", 0.1, 0.01, 1.0);
        
        SHOW_LEVEL_NAME_TAG = BUILDER
                .comment("是否显示生物等级名称标签（作为前缀添加到生物名称中）")
                .define("showLevelNameTag", true);
        
        SHOW_EXTRA_LEVEL_TAG = BUILDER
                .comment("是否显示额外的等级标签（在生物头顶额外渲染）")
                .define("showExtraLevelTag", false);
        
        HEALTH_BONUS_PER_LEVEL = BUILDER
                .comment("每级额外增加的基础生命值百分比")
                .defineInRange("healthBonusPerLevel", 0.1, 0.0, 2.0);
        
        ARMOR_BONUS_PER_LEVEL = BUILDER
                .comment("每级额外增加的护甲值")
                .defineInRange("armorBonusPerLevel", 0.5, 0.0, 10.0);
        
        ARMOR_TOUGHNESS_BONUS_PER_LEVEL = BUILDER
                .comment("每级额外增加的护甲韧性")
                .defineInRange("armorToughnessBonusPerLevel", 0.1, 0.0, 5.0);
        
        ATTACK_DAMAGE_BONUS_PER_LEVEL = BUILDER
                .comment("每级额外增加的基础攻击力百分比")
                .defineInRange("attackDamageBonusPerLevel", 0.05, 0.0, 2.0);
        EXCLUDED_MOBS = BUILDER
                .defineList("excludedMobs", Collections.emptyList(), o -> o instanceof String);
        
        BUILDER.pop();
        
        BUILDER.push("profession_change");
        
        ALLOW_PROFESSION_CHANGE = BUILDER
                .comment("是否允许玩家切换职业")
                .define("allowProfessionChange", true);
        
        PROFESSION_CHANGE_COST = BUILDER
                .comment("切换职业的代价（如经验值或金币）")
                .defineInRange("professionChangeCost", 100, 0, 10000);
        
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
    
    /**
     * 注册配置
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "professioncustom/professioncustom-common.toml");
    }
    
    /**
     * 获取是否启用职业系统
     */
    public static boolean isProfessionSystemEnabled() {
        return ENABLE_PROFESSION_SYSTEM.get();
    }
    
    /**
     * 获取是否启用职业限制
     */
    public static boolean isProfessionRestrictionEnabled() {
        return ENABLE_PROFESSION_RESTRICTION.get();
    }
    
    /**
     * 获取是否启用职业等级系统
     */
    public static boolean isClassLevelingEnabled() {
        return ENABLE_CLASS_LEVELING.get();
    }
    
    /**
     * 获取击杀怪物获得的基础经验值
     */
    public static int getBaseExpForKill() {
        return BASE_EXP_FOR_KILL.get();
    }
    
    /**
     * 获取每升一级所需经验的倍率
     */
    public static double getExpMultiplierPerLevel() {
        return EXP_MULTIPLIER_PER_LEVEL.get();
    }
    
    /**
     * 获取是否允许玩家切换职业
     */
    public static boolean isProfessionChangeAllowed() {
        return ALLOW_PROFESSION_CHANGE.get();
    }
    
    /**
     * 获取切换职业的代价
     */
    public static int getProfessionChangeCost() {
        return PROFESSION_CHANGE_COST.get();
    }
    
    /**
     * 获取最大生命值限制
     */
    public static double getMaxHealth() {
        return MAX_HEALTH.get();
    }
    
    /**
     * 获取最大护甲值限制
     */
    public static double getMaxArmor() {
        return MAX_ARMOR.get();
    }
    
    /**
     * 获取最大攻击力限制
     */
    public static double getMaxDamage() {
        return MAX_DAMAGE.get();
    }
    
    /**
     * 获取最大攻击速度限制
     */
    public static double getMaxDamageSpeed() {
        return MAX_DAMAGE_SPEED.get();
    }
    
    /**
     * 获取生命值计算公式系数
     */
    public static double getHealthCoefficient() {
        return HEALTH_COEFFICIENT.get();
    }
    
    /**
     * 获取护甲值计算公式系数
     */
    public static double getArmorCoefficient() {
        return ARMOR_COEFFICIENT.get();
    }
    
    /**
     * 获取攻击力计算公式系数
     */
    public static double getDamageCoefficient() {
        return DAMAGE_COEFFICIENT.get();
    }
    
    /**
     * 获取攻击速度计算公式系数
     */
    public static double getDamageSpeedCoefficient() {
        return DAMAGE_SPEED_COEFFICIENT.get();
    }
    
    // 生物等级系统获取方法
    
    /**
     * 获取是否启用生物等级系统
     */
    public static boolean isMobLevelingEnabled() {
        return ENABLE_MOB_LEVELING.get();
    }
    
    /**
     * 获取每级所需的基础距离
     */
    public static double getBaseLevelDistance() {
        return BASE_LEVEL_DISTANCE.get();
    }
    
    /**
     * 获取距离等级系数
     */
    public static double getDistanceLevelMultiplier() {
        return DISTANCE_LEVEL_MULTIPLIER.get();
    }
    
    /**
     * 获取是否显示生物等级名称标签
     */
    public static boolean isShowLevelNameTag() {
        return SHOW_LEVEL_NAME_TAG.get();
    }
    
    /**
     * 获取是否显示额外的等级标签
     */
    public static boolean isShowExtraLevelTag() {
        return SHOW_EXTRA_LEVEL_TAG.get();
    }
    
    /**
     * 获取每级额外增加的基础生命值百分比
     */
    public static double getHealthBonusPerLevel() {
        return HEALTH_BONUS_PER_LEVEL.get();
    }
    
    /**
     * 获取每级额外增加的护甲值
     */
    public static double getArmorBonusPerLevel() {
        return ARMOR_BONUS_PER_LEVEL.get();
    }
    
    /**
     * 获取每级额外增加的护甲韧性
     */
    public static double getArmorToughnessBonusPerLevel() {
        return ARMOR_TOUGHNESS_BONUS_PER_LEVEL.get();
    }
    
    /**
     * 获取每级额外增加的基础攻击力百分比
     */
    public static double getAttackDamageBonusPerLevel() {
        return ATTACK_DAMAGE_BONUS_PER_LEVEL.get();
    }
    
    public static List<? extends String> getExcludedMobIds() {
        return EXCLUDED_MOBS.get();
    }
    
    public static boolean isMobExcluded(Entity entity) {
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
            return false;
        }
        ResourceLocation mobId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (mobId == null) {
            return false;
        }
        String id = mobId.toString();
        if (MobConfig.isMobDisabled(id)) {
            return true;
        }
        List<? extends String> excluded = EXCLUDED_MOBS.get();
        if (excluded == null || excluded.isEmpty()) {
            return false;
        }
        for (String value : excluded) {
            if (id.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
