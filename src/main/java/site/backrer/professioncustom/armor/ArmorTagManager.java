package site.backrer.professioncustom.armor;

import net.minecraft.world.item.ItemStack;
import site.backrer.professioncustom.weapon.WeaponQuality;

import java.util.*;

public class ArmorTagManager {
    private static final Random RANDOM = new Random();

    public static Map<ArmorAttribute, Double> generateRandomAttributes(ItemStack stack, WeaponQuality quality, int level) {
        Map<ArmorAttribute, Double> attributes = new HashMap<>();

        int attributeCount = ArmorAttributeConfigManager.getMaxAttributesForQuality(quality);

        List<ArmorAttribute> availableAttributes = new ArrayList<>(Arrays.asList(ArmorAttribute.values()));

        // 按权重随机选择词条
        for (int i = 0; i < attributeCount && !availableAttributes.isEmpty(); i++) {
            ArmorAttribute attribute = getRandomAttributeByWeight(availableAttributes);
            if (attribute == null) {
                attribute = availableAttributes.get(RANDOM.nextInt(availableAttributes.size()));
            }
            availableAttributes.remove(attribute);
            double value = generateAttributeValue(attribute, quality, level);
            attributes.put(attribute, value);
        }

        return attributes;
    }

    private static double generateAttributeValue(ArmorAttribute attribute, WeaponQuality quality, int level) {
        double baseValue = ArmorAttributeConfigManager.getBaseValue(attribute);
        double baseMultiplier = ArmorAttributeConfigManager.getBaseValueMultiplier(quality);
        double minFactor = ArmorAttributeConfigManager.getRandomFactorMin(attribute);
        double maxFactor = ArmorAttributeConfigManager.getRandomFactorMax(attribute);
        double randomFactor = minFactor + RANDOM.nextDouble() * (maxFactor - minFactor);

        // 对于定伤和限伤：数值越大越"差"，随品质和等级降低
        if (ArmorAttributeConfigManager.isReverseScale(attribute)) {
            // 品质越高，伤害上限/定伤越小
            double qualityFactor = 1.0 / quality.getMultiplier();
            // 等级越高，数值越小（每级降低 5%，最多降低 50%）
            int effectiveLevel = Math.max(1, level);
            double levelFactor = 1.0 - Math.min(0.5, (effectiveLevel - 1) * 0.05);

            double value = baseValue * qualityFactor * levelFactor * randomFactor;
            // 保证下限大于 0
            return Math.max(1.0, value);
        }

        // 其他词条走原来的递增公式：品质/等级越高数值越大
        double qualityMultiplier = quality.getMultiplier() * baseMultiplier;
        double levelMultiplier = 1.0 + (level - 1) * 0.1;
        return baseValue * qualityMultiplier * levelMultiplier * randomFactor;
    }

    public static void initializeAttributes(ItemStack stack, WeaponQuality quality) {
        if (!ArmorNBTUtil.isArmor(stack)) {
            return;
        }

        Map<ArmorAttribute, Double> existing = ArmorNBTUtil.getAttributes(stack);
        if (!existing.isEmpty()) {
            return;
        }

        int level = ArmorNBTUtil.getLevel(stack);
        Map<ArmorAttribute, Double> attributes = generateRandomAttributes(stack, quality, level);
        for (Map.Entry<ArmorAttribute, Double> entry : attributes.entrySet()) {
            ArmorNBTUtil.setAttribute(stack, entry.getKey(), entry.getValue());
        }
    }

    public static void updateAttributesOnLevelUp(ItemStack stack) {
        if (!ArmorNBTUtil.isArmor(stack)) {
            return;
        }

        WeaponQuality quality = ArmorNBTUtil.getQuality(stack);
        int level = ArmorNBTUtil.getLevel(stack);
        Map<ArmorAttribute, Double> attributes = ArmorNBTUtil.getAttributes(stack);

        for (Map.Entry<ArmorAttribute, Double> entry : attributes.entrySet()) {
            ArmorAttribute attribute = entry.getKey();
            double oldValue = entry.getValue();

            // 反向缩放词条（如定伤/限伤）在升级时缓慢降低数值
            if (ArmorAttributeConfigManager.isReverseScale(attribute)) {
                double baseValue = ArmorAttributeConfigManager.getBaseValue(attribute);
                double newValue = oldValue * 0.95;
                double minValue = baseValue * 0.2;
                if (newValue < minValue) {
                    newValue = minValue;
                }
                ArmorNBTUtil.setAttribute(stack, attribute, newValue);
                continue;
            }

            // 其他词条保持原来的递增逻辑
            double baseValue = ArmorAttributeConfigManager.getBaseValue(attribute);
            double baseMultiplier = ArmorAttributeConfigManager.getBaseValueMultiplier(quality);
            double qualityMultiplier = quality.getMultiplier() * baseMultiplier;
            double levelMultiplier = 1.0 + (level - 1) * 0.1;

            double oldLevelMultiplier = 1.0 + (Math.max(1, level - 1) - 1) * 0.1;
            double randomFactor = oldValue / (baseValue * qualityMultiplier * oldLevelMultiplier);

            double newValue = baseValue * qualityMultiplier * levelMultiplier * randomFactor;
            ArmorNBTUtil.setAttribute(stack, attribute, newValue);
        }

        double refreshChance = ArmorAttributeConfigManager.getAttributeRefreshChance(quality);
        if (refreshChance > 0 && RANDOM.nextDouble() < refreshChance) {
            attributes = ArmorNBTUtil.getAttributes(stack);
            int maxAttributes = ArmorAttributeConfigManager.getMaxAttributesForQuality(quality);

            List<ArmorAttribute> allAttributes = new ArrayList<>(Arrays.asList(ArmorAttribute.values()));
            List<ArmorAttribute> missing = new ArrayList<>();
            for (ArmorAttribute attr : allAttributes) {
                if (!attributes.containsKey(attr)) {
                    missing.add(attr);
                }
            }

            if (!missing.isEmpty() && attributes.size() < maxAttributes) {
                ArmorAttribute newAttr = getRandomAttributeByWeight(missing);
                if (newAttr == null) {
                    newAttr = missing.get(RANDOM.nextInt(missing.size()));
                }
                double value = generateAttributeValue(newAttr, quality, level);
                ArmorNBTUtil.setAttribute(stack, newAttr, value);
            } else if (!attributes.isEmpty()) {
                List<ArmorAttribute> list = new ArrayList<>(attributes.keySet());
                ArmorAttribute toBoost = getRandomAttributeByWeight(list);
                if (toBoost == null) {
                    toBoost = list.get(RANDOM.nextInt(list.size()));
                }
                double boostFactor = 1.05 + RANDOM.nextDouble() * 0.15;
                double boostedValue = attributes.get(toBoost) * boostFactor;
                ArmorNBTUtil.setAttribute(stack, toBoost, boostedValue);
            }
        }
    }

    private static ArmorAttribute getRandomAttributeByWeight(List<ArmorAttribute> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        double totalWeight = 0.0;
        for (ArmorAttribute attr : candidates) {
            double w = ArmorAttributeConfigManager.getAttributeRefreshWeight(attr);
            if (w > 0) {
                totalWeight += w;
            }
        }

        if (totalWeight <= 0) {
            return null;
        }

        double r = RANDOM.nextDouble() * totalWeight;
        for (ArmorAttribute attr : candidates) {
            double w = ArmorAttributeConfigManager.getAttributeRefreshWeight(attr);
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
