package site.backrer.professioncustom.weapon;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 武器/装备相关配置
 */
public class WeaponConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // 等级与经验
    public static final ForgeConfigSpec.IntValue MAX_WEAPON_LEVEL;
    public static final ForgeConfigSpec.IntValue BASE_EXP_REQUIRED;
    public static final ForgeConfigSpec.DoubleValue EXP_MULTIPLIER;

    // 等级面板显示的伤害/护甲加成（每级）
    public static final ForgeConfigSpec.DoubleValue DAMAGE_BONUS_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue ARMOR_BONUS_PER_LEVEL;

    // 词条刷新/新增概率（按品质区分）
    public static final ForgeConfigSpec.DoubleValue ATTRIBUTE_REFRESH_CHANCE_COMMON;
    public static final ForgeConfigSpec.DoubleValue ATTRIBUTE_REFRESH_CHANCE_UNCOMMON;
    public static final ForgeConfigSpec.DoubleValue ATTRIBUTE_REFRESH_CHANCE_RARE;
    public static final ForgeConfigSpec.DoubleValue ATTRIBUTE_REFRESH_CHANCE_EPIC;
    public static final ForgeConfigSpec.DoubleValue ATTRIBUTE_REFRESH_CHANCE_LEGENDARY;
    public static final ForgeConfigSpec.DoubleValue ATTRIBUTE_REFRESH_CHANCE_MYTHIC;

    // 不同品质最大可出现的词条数量
    public static final ForgeConfigSpec.IntValue MAX_ATTRIBUTES_COMMON;
    public static final ForgeConfigSpec.IntValue MAX_ATTRIBUTES_UNCOMMON;
    public static final ForgeConfigSpec.IntValue MAX_ATTRIBUTES_RARE;
    public static final ForgeConfigSpec.IntValue MAX_ATTRIBUTES_EPIC;
    public static final ForgeConfigSpec.IntValue MAX_ATTRIBUTES_LEGENDARY;
    public static final ForgeConfigSpec.IntValue MAX_ATTRIBUTES_MYTHIC;

    static {
        BUILDER.push("weapon_general");

        MAX_WEAPON_LEVEL = BUILDER
                .comment("武器/装备的最大等级")
                .defineInRange("maxWeaponLevel", 100, 1, 1000);

        BASE_EXP_REQUIRED = BUILDER
                .comment("1 级升级所需的基础经验值")
                .defineInRange("baseExpRequired", 100, 1, 100000);

        EXP_MULTIPLIER = BUILDER
                .comment("每级升级经验倍率")
                .defineInRange("expMultiplier", 1.5, 1.0, 10.0);

        DAMAGE_BONUS_PER_LEVEL = BUILDER
                .comment("每级提供的伤害加成，用于 tooltip 显示")
                .defineInRange("damageBonusPerLevel", 0.5, 0.0, 100.0);

        ARMOR_BONUS_PER_LEVEL = BUILDER
                .comment("每级提供的护甲加成，用于 tooltip 显示")
                .defineInRange("armorBonusPerLevel", 0.5, 0.0, 100.0);

        BUILDER.pop();

        BUILDER.push("weapon_attribute_refresh");

        ATTRIBUTE_REFRESH_CHANCE_COMMON = BUILDER
                .comment("普通品质武器/装备升级时新增/强化词条的概率")
                .defineInRange("commonRefreshChance", 0.05, 0.0, 1.0);

        ATTRIBUTE_REFRESH_CHANCE_UNCOMMON = BUILDER
                .comment("优秀品质武器/装备升级时新增/强化词条的概率")
                .defineInRange("uncommonRefreshChance", 0.08, 0.0, 1.0);

        ATTRIBUTE_REFRESH_CHANCE_RARE = BUILDER
                .comment("稀有品质武器/装备升级时新增/强化词条的概率")
                .defineInRange("rareRefreshChance", 0.12, 0.0, 1.0);

        ATTRIBUTE_REFRESH_CHANCE_EPIC = BUILDER
                .comment("史诗品质武器/装备升级时新增/强化词条的概率")
                .defineInRange("epicRefreshChance", 0.18, 0.0, 1.0);

        ATTRIBUTE_REFRESH_CHANCE_LEGENDARY = BUILDER
                .comment("传说品质武器/装备升级时新增/强化词条的概率")
                .defineInRange("legendaryRefreshChance", 0.25, 0.0, 1.0);

        ATTRIBUTE_REFRESH_CHANCE_MYTHIC = BUILDER
                .comment("神话品质武器/装备升级时新增/强化词条的概率")
                .defineInRange("mythicRefreshChance", 0.35, 0.0, 1.0);

        // 不同品质最大词条数量
        MAX_ATTRIBUTES_COMMON = BUILDER
                .comment("普通品质武器/装备最多可拥有的词条数量")
                .defineInRange("maxAttributesCommon", 2, 0, 10);

        MAX_ATTRIBUTES_UNCOMMON = BUILDER
                .comment("优秀品质武器/装备最多可拥有的词条数量")
                .defineInRange("maxAttributesUncommon", 3, 0, 10);

        MAX_ATTRIBUTES_RARE = BUILDER
                .comment("稀有品质武器/装备最多可拥有的词条数量")
                .defineInRange("maxAttributesRare", 4, 0, 10);

        MAX_ATTRIBUTES_EPIC = BUILDER
                .comment("史诗品质武器/装备最多可拥有的词条数量")
                .defineInRange("maxAttributesEpic", 6, 0, 10);

        MAX_ATTRIBUTES_LEGENDARY = BUILDER
                .comment("传说品质武器/装备最多可拥有的词条数量")
                .defineInRange("maxAttributesLegendary", 7, 0, 10);

        MAX_ATTRIBUTES_MYTHIC = BUILDER
                .comment("神话品质武器/装备最多可拥有的词条数量")
                .defineInRange("maxAttributesMythic", 8, 0, 10);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "professioncustom/weapon-common.toml");
    }

    public static int getMaxWeaponLevel() {
        return MAX_WEAPON_LEVEL.get();
    }

    public static int getBaseExpRequired() {
        return BASE_EXP_REQUIRED.get();
    }

    public static double getExpMultiplier() {
        return EXP_MULTIPLIER.get();
    }

    public static double getDamageBonusPerLevel() {
        return DAMAGE_BONUS_PER_LEVEL.get();
    }

    public static double getArmorBonusPerLevel() {
        return ARMOR_BONUS_PER_LEVEL.get();
    }

    public static double getAttributeRefreshChance(WeaponQuality quality) {
        switch (quality) {
            case UNCOMMON:
                return ATTRIBUTE_REFRESH_CHANCE_UNCOMMON.get();
            case RARE:
                return ATTRIBUTE_REFRESH_CHANCE_RARE.get();
            case EPIC:
                return ATTRIBUTE_REFRESH_CHANCE_EPIC.get();
            case LEGENDARY:
                return ATTRIBUTE_REFRESH_CHANCE_LEGENDARY.get();
            case MYTHIC:
                return ATTRIBUTE_REFRESH_CHANCE_MYTHIC.get();
            case COMMON:
            default:
                return ATTRIBUTE_REFRESH_CHANCE_COMMON.get();
        }
    }

    public static int getMaxAttributesForQuality(WeaponQuality quality) {
        switch (quality) {
            case UNCOMMON:
                return MAX_ATTRIBUTES_UNCOMMON.get();
            case RARE:
                return MAX_ATTRIBUTES_RARE.get();
            case EPIC:
                return MAX_ATTRIBUTES_EPIC.get();
            case LEGENDARY:
                return MAX_ATTRIBUTES_LEGENDARY.get();
            case MYTHIC:
                return MAX_ATTRIBUTES_MYTHIC.get();
            case COMMON:
            default:
                return MAX_ATTRIBUTES_COMMON.get();
        }
    }
}
