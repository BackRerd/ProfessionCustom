package site.backrer.professioncustom.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import javax.annotation.Nonnull;

/**
 * 武器词条枚举
 */
public enum WeaponAttribute {
    // 基础属性
    ATTACK_DAMAGE("weapon.attribute.attack_damage", "攻击力", ChatFormatting.RED, true, "提高基础伤害数值，所有伤害结算前先加上该数值"),
    CRITICAL_RATE("weapon.attribute.critical_rate", "暴击率", ChatFormatting.YELLOW, true, "提高暴击触发概率，数值为百分比"),
    CRITICAL_DAMAGE("weapon.attribute.critical_damage", "暴击伤害", ChatFormatting.GOLD, true, "提高暴击时的伤害倍率"),
    
    // 元素伤害
    FIRE_DAMAGE("weapon.attribute.fire_damage", "火焰伤害", ChatFormatting.RED, true, "额外附加火焰伤害并点燃目标"),
    ICE_DAMAGE("weapon.attribute.ice_damage", "冰冻伤害", ChatFormatting.AQUA, true, "额外附加冰冻伤害并降低目标移动速度"),
    SOUL_DAMAGE("weapon.attribute.soul_damage", "灵魂伤害", ChatFormatting.DARK_PURPLE, true, "额外附加灵魂伤害并施加凋零效果"),
    
    // 特殊属性
    DAMAGE_PER_SECOND("weapon.attribute.damage_per_second", "秒伤", ChatFormatting.DARK_RED, true, "在一段时间内持续对目标造成伤害"),
    LIFESTEAL_RATE("weapon.attribute.lifesteal_rate", "吸血率", ChatFormatting.DARK_RED, true, "有概率根据造成的伤害回复自身生命，数值为百分比"),
    LIFESTEAL_MULTIPLIER("weapon.attribute.lifesteal_multiplier", "吸血倍率", ChatFormatting.DARK_RED, true, "提高吸血时转化为生命值的倍率"),
    LIGHTBURST("weapon.attribute.lightburst", "光爆", ChatFormatting.YELLOW, false, "攻击时对目标施加失明与黑暗效果");
    
    private final String translationKey;
    private final String displayName;
    private final ChatFormatting color;
    private final boolean isNumeric; // 是否为数值型词条
    private final String description; // 词条详细说明
    
    WeaponAttribute(String translationKey, String displayName, ChatFormatting color, boolean isNumeric, String description) {
        this.translationKey = translationKey;
        this.displayName = displayName;
        this.color = color;
        this.isNumeric = isNumeric;
        this.description = description;
    }
    
    public String getTranslationKey() {
        return translationKey;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatFormatting getColor() {
        return color;
    }
    
    public boolean isNumeric() {
        return isNumeric;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Nonnull
    public Component getDisplayComponent() {
        // 使用语言文件中的 translationKey 渲染名称
        return Component.translatable(translationKey).withStyle(color);
    }
    
    @Nonnull
    public Component getDescriptionComponent() {
        // 使用 translationKey.desc 渲染描述
        return Component.translatable(translationKey + ".desc").withStyle(ChatFormatting.DARK_GRAY);
    }
    
    /**
     * 获取词条的显示文本（带数值）
     * @param value 词条数值
     * @return 显示文本
     */
    public Component getDisplayWithValue(double value) {
        if (isNumeric) {
            String valueStr;
            if (value == (int) value) {
                valueStr = String.valueOf((int) value);
            } else {
                valueStr = String.format("%.1f", value);
            }

            // 根据词条类型格式化显示
            String suffix = "";
            if (this == CRITICAL_RATE || this == LIFESTEAL_RATE) {
                suffix = "%";
            } else if (this == CRITICAL_DAMAGE || this == LIFESTEAL_MULTIPLIER) {
                suffix = "x";
            } else if (this == DAMAGE_PER_SECOND) {
                suffix = "/s";
            }

            return Component.translatable(translationKey)
                    .append(": " + valueStr + suffix)
                    .withStyle(color);
        } else {
            return getDisplayComponent();
        }
    }
}

