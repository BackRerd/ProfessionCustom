package site.backrer.professioncustom.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public enum ArmorAttribute {
    HEAVY_ARMOR("armor.attribute.heavy_armor", "重甲", ChatFormatting.GRAY, true, "每次受到伤害时减去该值"),
    DAMAGE_LIMIT("armor.attribute.damage_limit", "限伤", ChatFormatting.BLUE, true, "本次受到的伤害不会超过该值"),
    THORNS("armor.attribute.thorns", "反甲", ChatFormatting.DARK_GREEN, true, "反弹一定比例的伤害"),
    FIXED_DAMAGE("armor.attribute.fixed_damage", "定伤", ChatFormatting.DARK_AQUA, true, "每次只承受固定伤害，超过 800% 时失效"),
    SLOW("armor.attribute.slow", "延缓", ChatFormatting.DARK_BLUE, true, "受到攻击时使攻击者缓慢，数值越大触发概率越高"),
    STUN("armor.attribute.stun", "晕厥", ChatFormatting.DARK_PURPLE, true, "受到攻击时使攻击者失明反胃，数值越大触发概率越高"),
    REGEN("armor.attribute.regen", "恢复", ChatFormatting.GREEN, true, "每秒恢复一定生命值"),
    EXPLOSIVE("armor.attribute.explosive", "爆甲", ChatFormatting.RED, true, "受到攻击时有概率产生对自己无效的爆炸"),
    SOUL_IMMUNE("armor.attribute.soul_immune", "灵魂", ChatFormatting.AQUA, false, "免疫直接伤害"),
    BERSERK("armor.attribute.berserk", "狂躁", ChatFormatting.DARK_RED, true, "提高攻击力但每次攻击扣除一定百分比生命"),
    SPEED("armor.attribute.speed", "速度", ChatFormatting.YELLOW, true, "提高移动速度"),
    REPAIR("armor.attribute.repair", "修复", ChatFormatting.GOLD, true, "每秒修复一点耐久"),
    DODGE("armor.attribute.dodge", "闪避", ChatFormatting.WHITE, true, "有概率完全闪避一次攻击"),
    SHOCK("armor.attribute.shock", "震荡", ChatFormatting.DARK_GRAY, true, "受到攻击时震荡附近生物并造成伤害，数值越大触发概率越高");

    private final String translationKey;
    private final String displayName;
    private final ChatFormatting color;
    private final boolean isNumeric;
    private final String description;

    ArmorAttribute(String translationKey, String displayName, ChatFormatting color, boolean isNumeric, String description) {
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
        // 从语言文件获取当前语言下的名称
        return Component.translatable(translationKey).getString();
    }

    public ChatFormatting getColor() {
        return color;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public String getDescription() {
        // 从语言文件获取当前语言下的描述
        return Component.translatable(translationKey + ".desc").getString();
    }

    @Nonnull
    public Component getDisplayComponent() {
        // 使用语言文件中的 translationKey 渲染名称
        return Component.translatable(translationKey).withStyle(color);
    }

    @Nonnull
    public Component getDescriptionComponent() {
        // 描述使用 translationKey.desc 从语言文件中获取
        return Component.translatable(translationKey + ".desc").withStyle(ChatFormatting.DARK_GRAY);
    }

    @Nonnull
    public Component getDisplayWithValue(double value) {
        if (isNumeric) {
            String valueStr;
            if (value == (int) value) {
                valueStr = String.valueOf((int) value);
            } else {
                valueStr = String.format("%.1f", value);
            }

            String suffix = "";
            if (this == THORNS || this == DODGE || this == SLOW || this == STUN || this == EXPLOSIVE || this == BERSERK) {
                suffix = "%";
            }

            // 使用 translationKey 渲染名称，然后附加数值
            return Component.translatable(translationKey)
                    .append(": " + valueStr + suffix)
                    .withStyle(color);
        } else {
            return getDisplayComponent();
        }
    }
}
