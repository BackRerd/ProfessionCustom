package site.backrer.professioncustom.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import javax.annotation.Nonnull;

/**
 * 武器品质枚举
 */
public enum WeaponQuality {
    COMMON("weapon.quality.common", "普通", ChatFormatting.WHITE, 1.0),
    UNCOMMON("weapon.quality.uncommon", "优秀", ChatFormatting.GREEN, 1.2),
    RARE("weapon.quality.rare", "稀有", ChatFormatting.BLUE, 1.5),
    EPIC("weapon.quality.epic", "史诗", ChatFormatting.DARK_PURPLE, 2.0),
    LEGENDARY("weapon.quality.legendary", "传说", ChatFormatting.GOLD, 3.0),
    MYTHIC("weapon.quality.mythic", "神话", ChatFormatting.LIGHT_PURPLE, 4.0);
    
    private final String translationKey;
    private final String displayName;
    private final ChatFormatting color;
    private final double multiplier; // 品质倍率，影响词条数值
    
    WeaponQuality(String translationKey, String displayName, ChatFormatting color, double multiplier) {
        this.translationKey = translationKey;
        this.displayName = displayName;
        this.color = color;
        this.multiplier = multiplier;
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
    
    public double getMultiplier() {
        return multiplier;
    }
    
    @Nonnull
    public Component getDisplayComponent() {
        return Component.literal(displayName).withStyle(color);
    }
    
    /**
     * 根据数值获取品质
     * @param value 品质数值（0-5）
     * @return 对应的品质
     */
    public static WeaponQuality fromValue(int value) {
        if (value < 0) value = 0;
        if (value >= values().length) value = values().length - 1;
        return values()[value];
    }
    
    /**
     * 获取品质的数值
     * @return 品质数值（0-5）
     */
    public int getValue() {
        return ordinal();
    }
}

