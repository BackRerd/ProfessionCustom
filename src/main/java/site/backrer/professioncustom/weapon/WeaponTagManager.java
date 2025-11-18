package site.backrer.professioncustom.weapon;

import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 武器词条管理器，负责生成和管理武器的词条
 */
public class WeaponTagManager {
    private static final Random RANDOM = new Random();
    
    /**
     * 为武器生成随机词条
     * @param stack 武器物品
     * @param quality 武器品质
     * @param level 武器等级
     * @return 生成的词条映射
     */
    public static Map<WeaponAttribute, Double> generateRandomAttributes(ItemStack stack, WeaponQuality quality, int level) {
        Map<WeaponAttribute, Double> attributes = new HashMap<>();
        
        // 根据品质决定词条数量
        int attributeCount = getAttributeCountForQuality(quality);
        
        // 获取所有可用的词条
        List<WeaponAttribute> availableAttributes = new ArrayList<>(Arrays.asList(WeaponAttribute.values()));
        
        // 按权重随机选择词条
        for (int i = 0; i < attributeCount && !availableAttributes.isEmpty(); i++) {
            WeaponAttribute attribute = getRandomAttributeByWeight(availableAttributes);
            if (attribute == null) {
                attribute = availableAttributes.get(RANDOM.nextInt(availableAttributes.size()));
            }
            availableAttributes.remove(attribute);
            double value = generateAttributeValue(attribute, quality, level);
            attributes.put(attribute, value);
        }
        
        return attributes;
    }
    
    /**
     * 根据品质获取词条数量（受配置中最大词条数量限制）
     */
    private static int getAttributeCountForQuality(WeaponQuality quality) {
        return WeaponAttributeConfigManager.getMaxAttributesForQuality(quality);
    }
    
    /**
     * 生成词条数值
     */
    private static double generateAttributeValue(WeaponAttribute attribute, WeaponQuality quality, int level) {
        double baseValue = WeaponAttributeConfigManager.getBaseValue(attribute);
        double baseMultiplier = WeaponAttributeConfigManager.getBaseValueMultiplier(quality);
        double minFactor = WeaponAttributeConfigManager.getRandomFactorMin(attribute);
        double maxFactor = WeaponAttributeConfigManager.getRandomFactorMax(attribute);

        double qualityMultiplier = quality.getMultiplier() * baseMultiplier;
        double levelMultiplier = 1.0 + (level - 1) * 0.1; // 每级增加10%

        double randomFactor = minFactor + RANDOM.nextDouble() * (maxFactor - minFactor);

        return baseValue * qualityMultiplier * levelMultiplier * randomFactor;
    }
    
    /**
     * 为武器添加词条（首次生成时调用）
     */
    public static void initializeAttributes(ItemStack stack, WeaponQuality quality) {
        if (!WeaponNBTUtil.isWeapon(stack)) {
            return;
        }
        
        // 检查是否已有词条
        Map<WeaponAttribute, Double> existingAttributes = WeaponNBTUtil.getAttributes(stack);
        if (!existingAttributes.isEmpty()) {
            return; // 已有词条，不重新生成
        }
        
        int level = WeaponNBTUtil.getLevel(stack);
        Map<WeaponAttribute, Double> attributes = generateRandomAttributes(stack, quality, level);
        
        // 保存词条到NBT
        for (Map.Entry<WeaponAttribute, Double> entry : attributes.entrySet()) {
            WeaponNBTUtil.setAttribute(stack, entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 升级时更新词条数值
     */
    public static void updateAttributesOnLevelUp(ItemStack stack) {
        if (!WeaponNBTUtil.isWeapon(stack)) {
            return;
        }
        
        WeaponQuality quality = WeaponNBTUtil.getQuality(stack);
        int level = WeaponNBTUtil.getLevel(stack);
        Map<WeaponAttribute, Double> attributes = WeaponNBTUtil.getAttributes(stack);
        
        // 更新所有词条的数值
        for (Map.Entry<WeaponAttribute, Double> entry : attributes.entrySet()) {
            WeaponAttribute attribute = entry.getKey();
            double baseValue = WeaponAttributeConfigManager.getBaseValue(attribute);
            double baseMultiplier = WeaponAttributeConfigManager.getBaseValueMultiplier(quality);
            double qualityMultiplier = quality.getMultiplier() * baseMultiplier;
            double levelMultiplier = 1.0 + (level - 1) * 0.1;
            
            // 保持原有的随机因子（通过反向计算）
            double oldValue = entry.getValue();
            double oldLevelMultiplier = 1.0 + (Math.max(1, level - 1) - 1) * 0.1;
            double randomFactor = oldValue / (baseValue * qualityMultiplier * oldLevelMultiplier);
            
            double newValue = baseValue * qualityMultiplier * levelMultiplier * randomFactor;
            WeaponNBTUtil.setAttribute(stack, attribute, newValue);
        }

        // 按配置概率新增/强化词条
        double refreshChance = WeaponAttributeConfigManager.getAttributeRefreshChance(quality);
        if (refreshChance > 0 && RANDOM.nextDouble() < refreshChance) {
            // 重新获取最新词条映射
            attributes = WeaponNBTUtil.getAttributes(stack);

            int maxAttributes = WeaponAttributeConfigManager.getMaxAttributesForQuality(quality);

            // 计算未拥有的词条列表
            List<WeaponAttribute> allAttributes = new ArrayList<>(Arrays.asList(WeaponAttribute.values()));
            List<WeaponAttribute> missingAttributes = new ArrayList<>();
            for (WeaponAttribute attr : allAttributes) {
                if (!attributes.containsKey(attr)) {
                    missingAttributes.add(attr);
                }
            }

            if (!missingAttributes.isEmpty() && attributes.size() < maxAttributes) {
                // 随机新增 1 个未拥有的词条
                WeaponAttribute newAttr = getRandomAttributeByWeight(missingAttributes);
                if (newAttr == null) {
                    newAttr = missingAttributes.get(RANDOM.nextInt(missingAttributes.size()));
                }
                double value = generateAttributeValue(newAttr, quality, level);
                WeaponNBTUtil.setAttribute(stack, newAttr, value);
            } else if (!attributes.isEmpty()) {
                // 如果没有可新增的词条，则随机强化一个已有词条
                List<WeaponAttribute> existingList = new ArrayList<>(attributes.keySet());
                WeaponAttribute toBoostAttr = getRandomAttributeByWeight(existingList);
                if (toBoostAttr == null) {
                    toBoostAttr = existingList.get(RANDOM.nextInt(existingList.size()));
                }

                double boostFactor = 1.05 + RANDOM.nextDouble() * 0.15; // 提升 5% - 20%
                double boostedValue = attributes.get(toBoostAttr) * boostFactor;
                WeaponNBTUtil.setAttribute(stack, toBoostAttr, boostedValue);
            }
        }
    }

    private static WeaponAttribute getRandomAttributeByWeight(List<WeaponAttribute> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        double totalWeight = 0.0;
        for (WeaponAttribute attr : candidates) {
            double w = WeaponAttributeConfigManager.getAttributeRefreshWeight(attr);
            if (w > 0) {
                totalWeight += w;
            }
        }

        if (totalWeight <= 0) {
            return null;
        }

        double r = RANDOM.nextDouble() * totalWeight;
        for (WeaponAttribute attr : candidates) {
            double w = WeaponAttributeConfigManager.getAttributeRefreshWeight(attr);
            if (w <= 0) {
                continue;
            }
            r -= w;
            if (r <= 0) {
                return attr;
            }
        }

        return candidates.get(candidates.size() - 1);
    }
}
