package site.backrer.professioncustom.level;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import site.backrer.professioncustom.MobConfig;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.Professioncustom;

import java.util.UUID;
import javax.annotation.Nonnull;

/**
 * 生物属性计算器 - 负责根据等级计算生物的各项属性值
 */
public class MobAttributeCalculator {
    // UUID用于唯一标识属性修改器，确保可以正确地添加和移除
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789010");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789011");
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789013");

    /**
     * 根据等级计算额外的生命值加成
     * @param baseHealth 基础生命值
     * @param level 生物等级
     * @param entity 目标生物实体（用于获取特定生物的属性倍率）
     * @return 计算后的生命值
     */
    public static double calculateHealth(double baseHealth, int level, LivingEntity entity) {
        if (level <= 1 || baseHealth <= 0) return baseHealth;
        
        // 获取生物特定的属性倍率
        double mobSpecificMultiplier = 1.0;
        if (entity != null && !(entity instanceof Player)) {
            String mobTypeId = getMobTypeId(entity);
            MobConfig.MobBonusConfig bonusConfig = MobConfig.getMobBonusConfig(mobTypeId);
            mobSpecificMultiplier = bonusConfig.healthMultiplier;
        }
        
        // 每级增加 ProfessionConfig.getHealthBonusPerLevel() * 100% 的基础生命值，再乘以生物特定倍率
        double bonusMultiplier = 1.0 + (level - 1) * ProfessionConfig.getHealthBonusPerLevel() * mobSpecificMultiplier;
        
        // 确保加成比例有效
        if (bonusMultiplier <= 1.0) return baseHealth;
        
        double result = baseHealth * bonusMultiplier;
        return result;
    }
    
    /**
     * 获取生物的类型ID字符串
     * @param entity 生物实体
     * @return 类型ID字符串
     */
    private static String getMobTypeId(LivingEntity entity) {
        ResourceLocation resourceLocation = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return resourceLocation != null ? resourceLocation.toString() : "minecraft:unknown";
    }
    
    /**
     * 根据等级计算额外的护甲值加成
     * @param baseArmor 基础护甲值
     * @param level 生物等级
     * @param entity 目标生物实体（用于获取特定生物的属性倍率）
     * @return 计算后的护甲值
     */
    public static double calculateArmor(double baseArmor, int level, LivingEntity entity) {
        if (level <= 1) return baseArmor;
        
        // 获取生物特定的护甲倍率
        double mobSpecificMultiplier = 1.0;
        if (entity != null && !(entity instanceof Player)) {
            String mobTypeId = getMobTypeId(entity);
            MobConfig.MobBonusConfig bonusConfig = MobConfig.getMobBonusConfig(mobTypeId);
            mobSpecificMultiplier = bonusConfig.armorMultiplier;
        }
        
        // 每级增加固定的护甲值 ProfessionConfig.getArmorBonusPerLevel()，再乘以生物特定倍率
        return baseArmor + (level - 1) * ProfessionConfig.getArmorBonusPerLevel() * mobSpecificMultiplier;
    }
    
    /**
     * 根据等级计算额外的护甲韧性加成
     * @param baseToughness 基础护甲韧性
     * @param level 生物等级
     * @param entity 目标生物实体（用于获取特定生物的属性倍率）
     * @return 计算后的护甲韧性
     */
    public static double calculateArmorToughness(double baseToughness, int level, LivingEntity entity) {
        if (level <= 1) return baseToughness;
        
        // 获取生物特定的护甲韧性倍率
        double mobSpecificMultiplier = 1.0;
        if (entity != null && !(entity instanceof Player)) {
            String mobTypeId = getMobTypeId(entity);
            MobConfig.MobBonusConfig bonusConfig = MobConfig.getMobBonusConfig(mobTypeId);
            mobSpecificMultiplier = bonusConfig.armorToughnessMultiplier;
        }
        
        // 每级增加固定的护甲韧性 ProfessionConfig.getArmorToughnessBonusPerLevel()，再乘以生物特定倍率
        return baseToughness + (level - 1) * ProfessionConfig.getArmorToughnessBonusPerLevel() * mobSpecificMultiplier;
    }
    
    /**
     * 根据等级计算额外的攻击力加成
     * @param baseDamage 基础攻击力
     * @param level 生物等级
     * @param entity 目标生物实体（用于获取特定生物的属性倍率）
     * @return 计算后的攻击力
     */
    public static double calculateAttackDamage(double baseDamage, int level, LivingEntity entity) {
        if (level <= 1) return baseDamage;
        
        // 获取生物特定的攻击力倍率
        double mobSpecificMultiplier = 1.0;
        if (entity != null && !(entity instanceof Player)) {
            String mobTypeId = getMobTypeId(entity);
            MobConfig.MobBonusConfig bonusConfig = MobConfig.getMobBonusConfig(mobTypeId);
            mobSpecificMultiplier = bonusConfig.attackDamageMultiplier;
        }
        
        // 每级增加 ProfessionConfig.getAttackDamageBonusPerLevel() * 100% 的基础攻击力，再乘以生物特定倍率
        double bonusMultiplier = 1.0 + (level - 1) * ProfessionConfig.getAttackDamageBonusPerLevel() * mobSpecificMultiplier;
        return baseDamage * bonusMultiplier;
    }
    
    /**
     * 应用所有等级相关的属性加成到生物身上
     * @param entity 目标生物实体
     */
    public static void applyLevelAttributes(LivingEntity entity) {
        // 检查是否为null或不是生物（玩家除外）或模组等级功能未启用
        if (entity == null || entity instanceof Player || !ProfessionConfig.isMobLevelingEnabled() || ProfessionConfig.isMobExcluded(entity)) {
            return;
        }
        
        // 使用正确导入的MobLevelManager类获取等级
        int level = MobLevelManager.getEntityLevel(entity);
        
        if (level <= 1) {
            // 如果是1级或以下，移除所有加成
            removeLevelAttributes(entity);
            return;
        }
        
        // 只在调试模式下输出日志
        if (MobConfig.enableDebugLogging) {
            Professioncustom.LOGGER.debug("Applying level attributes: entity={}, level={}, enableMobLeveling={}", 
                entity.getType().getDescription().getString(), level, ProfessionConfig.isMobLevelingEnabled());
        }
        
        try {
            // 应用生命值加成
            applyHealthAttribute(entity, level);
            
            // 应用护甲值加成
            applyArmorAttribute(entity, level);
            
            // 应用护甲韧性加成
            applyArmorToughnessAttribute(entity, level);
            
            // 应用攻击力加成
            applyAttackDamageAttribute(entity, level);
            
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to apply level attributes to entity: " + e.getMessage());
        }
    }
    
    /**
     * 移除所有等级相关的属性加成
     * @param entity 目标生物实体
     */
    public static void removeLevelAttributes(LivingEntity entity) {
        if (entity == null || entity instanceof Player) return;
        
        try {
            // 移除生命值加成
            removeAttributeModifier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_UUID);
            
            // 移除护甲值加成
            removeAttributeModifier(entity, Attributes.ARMOR, ARMOR_MODIFIER_UUID);
            
            // 移除护甲韧性加成
            removeAttributeModifier(entity, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_MODIFIER_UUID);
            
            // 移除攻击力加成
            removeAttributeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_UUID);
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to remove level attributes from entity: " + e.getMessage());
        }
    }
    
    /**
     * 应用生命值属性加成
     */
    private static void applyHealthAttribute(@Nonnull LivingEntity entity, int level) {
        // 额外安全检查，确保不是玩家
        if (entity instanceof Player) return;
        
        AttributeInstance attribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        
        // 保存当前生命值比例，以便调整后保持相同的比例
        double healthPercentage = entity.getHealth() / attribute.getValue();
        
        // 移除旧的修改器，确保从干净的状态开始计算
        removeAttributeModifier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_UUID);
        
        // 获取生物特定的属性倍率
        String mobTypeId = getMobTypeId(entity);
        MobConfig.MobBonusConfig bonusConfig = MobConfig.getMobBonusConfig(mobTypeId);
        
        // 计算乘法加成比例，生物特定倍率直接应用于加成
        // 每级基础加成 * (1 + 生物特定倍率调整)
        double baseBonusPerLevel = ProfessionConfig.getHealthBonusPerLevel();
        
        // 计算总加成，生物特定倍率直接影响最终的健康值
        double totalBonusMultiplier = baseBonusPerLevel * bonusConfig.healthMultiplier;
        double bonusMultiplier = (level - 1) * totalBonusMultiplier;
        
        // 确保加成比例是正数
        if (bonusMultiplier <= 0) return;
        
        // 添加新的修改器，使用MULTIPLY_BASE操作类型
        attribute.addPermanentModifier(new AttributeModifier(
                HEALTH_MODIFIER_UUID,
                "professioncustom.level_health_bonus",
                bonusMultiplier,
                AttributeModifier.Operation.MULTIPLY_BASE
        ));
        
        // 调整当前生命值以保持相同的比例，添加显式类型转换
        entity.setHealth((float)(entity.getMaxHealth() * healthPercentage));
        
        // 只在调试模式下输出日志
        if (MobConfig.enableDebugLogging) {
            Professioncustom.LOGGER.debug("Applied health bonus: entity={}, level={}, bonusMultiplier={}, newMaxHealth={}", 
                entity.getType().getDescription().getString(), level, bonusMultiplier, entity.getMaxHealth());
        }
    }
    
    /**
     * 应用护甲值属性加成
     */
    private static void applyArmorAttribute(@Nonnull LivingEntity entity, int level) {
        // 额外安全检查，确保不是玩家
        if (entity instanceof Player) return;
        
        AttributeInstance attribute = entity.getAttribute(Attributes.ARMOR);
        if (attribute == null) return;
        
        // 移除旧的修改器
        removeAttributeModifier(entity, Attributes.ARMOR, ARMOR_MODIFIER_UUID);
        
        // 获取生物特定的护甲倍率
        String mobTypeId = getMobTypeId(entity);
        MobConfig.MobBonusConfig bonusConfig = MobConfig.getMobBonusConfig(mobTypeId);
        
        // 添加新的修改器，应用生物特定倍率
        double bonusValue = (level - 1) * ProfessionConfig.getArmorBonusPerLevel() * bonusConfig.armorMultiplier;
        attribute.addPermanentModifier(new AttributeModifier(
                ARMOR_MODIFIER_UUID,
                "professioncustom.level_armor_bonus",
                bonusValue,
                AttributeModifier.Operation.ADDITION
        ));
    }
    
    /**
     * 应用护甲韧性属性加成
     */
    private static void applyArmorToughnessAttribute(@Nonnull LivingEntity entity, int level) {
        // 额外安全检查，确保不是玩家
        if (entity instanceof Player) return;
        
        AttributeInstance attribute = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (attribute == null) return;
        
        // 移除旧的修改器
        removeAttributeModifier(entity, Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_MODIFIER_UUID);
        
        // 获取生物特定的护甲韧性倍率
        String mobTypeId = getMobTypeId(entity);
        MobConfig.MobBonusConfig bonusConfig = MobConfig.getMobBonusConfig(mobTypeId);
        
        // 添加新的修改器，应用生物特定倍率
        double bonusValue = (level - 1) * ProfessionConfig.getArmorToughnessBonusPerLevel() * bonusConfig.armorToughnessMultiplier;
        attribute.addPermanentModifier(new AttributeModifier(
                ARMOR_TOUGHNESS_MODIFIER_UUID,
                "professioncustom.level_armor_toughness_bonus",
                bonusValue,
                AttributeModifier.Operation.ADDITION
        ));
    }
    
    /**
     * 应用攻击力属性加成
     */
    private static void applyAttackDamageAttribute(@Nonnull LivingEntity entity, int level) {
        // 额外安全检查，确保不是玩家
        if (entity instanceof Player) return;
        
        AttributeInstance attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) return;
        
        // 移除旧的修改器，确保从干净的状态开始计算
        removeAttributeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_UUID);
        
        // 获取生物特定的攻击力倍率
        String mobTypeId = getMobTypeId(entity);
        MobConfig.MobBonusConfig bonusConfig = MobConfig.getMobBonusConfig(mobTypeId);
        
        // 关键修复：使用正确的乘法操作类型
        // 计算乘法加成比例，应用生物特定倍率
        double bonusMultiplier = (level - 1) * ProfessionConfig.getAttackDamageBonusPerLevel() * bonusConfig.attackDamageMultiplier;
        
        // 添加新的修改器，使用MULTIPLY_BASE操作类型
        attribute.addPermanentModifier(new AttributeModifier(
                ATTACK_DAMAGE_MODIFIER_UUID,
                "professioncustom.level_attack_damage_bonus",
                bonusMultiplier,
                AttributeModifier.Operation.MULTIPLY_BASE
        ));
    }
    
    /**
     * 移除特定的属性修改器
     */
    private static void removeAttributeModifier(@Nonnull LivingEntity entity, @Nonnull net.minecraft.world.entity.ai.attributes.Attribute attribute, @Nonnull UUID modifierUuid) {
        // 额外安全检查，确保不是玩家
        if (entity instanceof Player) return;
        
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            // 正确的方式是先获取所有修改器并检查
            for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                if (modifier.getId().equals(modifierUuid)) {
                    attributeInstance.removeModifier(modifier);
                    break;
                }
            }
        }
    }
}
