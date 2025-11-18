package site.backrer.professioncustom.armor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.weapon.WeaponQuality;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static site.backrer.professioncustom.Professioncustom.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class ArmorAttributeConfigManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "armor_attributes";

    private static ArmorAttributeConfigManager INSTANCE;

    private static final Map<WeaponQuality, QualityConfig> QUALITY_CONFIGS = new EnumMap<>(WeaponQuality.class);
    private static final Map<ArmorAttribute, AttributeConfig> ATTRIBUTE_CONFIGS = new EnumMap<>(ArmorAttribute.class);

    public ArmorAttributeConfigManager() {
        super(GSON, FOLDER);
        INSTANCE = this;
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ArmorAttributeConfigManager());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        QUALITY_CONFIGS.clear();
        ATTRIBUTE_CONFIGS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            JsonElement jsonElement = entry.getValue();
            if (!jsonElement.isJsonObject()) {
                continue;
            }
            JsonObject root = jsonElement.getAsJsonObject();

            // 读取 qualities
            if (root.has("qualities") && root.get("qualities").isJsonObject()) {
                JsonObject qualities = root.getAsJsonObject("qualities");
                for (WeaponQuality quality : WeaponQuality.values()) {
                    String key = quality.name().toLowerCase();
                    if (!qualities.has(key) || !qualities.get(key).isJsonObject()) {
                        continue;
                    }
                    JsonObject q = qualities.getAsJsonObject(key);
                    QualityConfig cfg = new QualityConfig();
                    cfg.attributeRefreshChance = q.has("attribute_refresh_chance") ? q.get("attribute_refresh_chance").getAsDouble() : 0.0;
                    cfg.maxAttributes = q.has("max_attributes") ? q.get("max_attributes").getAsInt() : 0;
                    cfg.baseValueMultiplier = q.has("base_value_multiplier") ? q.get("base_value_multiplier").getAsDouble() : 1.0;
                    QUALITY_CONFIGS.put(quality, cfg);
                }
            }

            // 读取 attributes
            if (root.has("attributes") && root.get("attributes").isJsonObject()) {
                JsonObject attrs = root.getAsJsonObject("attributes");
                for (ArmorAttribute attr : ArmorAttribute.values()) {
                    String key = attr.name().toLowerCase();
                    if (!attrs.has(key) || !attrs.get(key).isJsonObject()) {
                        continue;
                    }
                    JsonObject a = attrs.getAsJsonObject(key);
                    AttributeConfig cfg = new AttributeConfig();
                    cfg.base = a.has("base") ? a.get("base").getAsDouble() : 1.0;
                    cfg.randomFactorMin = a.has("random_factor_min") ? a.get("random_factor_min").getAsDouble() : 0.8;
                    cfg.randomFactorMax = a.has("random_factor_max") ? a.get("random_factor_max").getAsDouble() : 1.2;
                    cfg.reverseScale = a.has("reverse_scale") && a.get("reverse_scale").getAsBoolean();
                    cfg.refreshWeight = a.has("refresh_weight") ? a.get("refresh_weight").getAsDouble() : 1.0;
                    ATTRIBUTE_CONFIGS.put(attr, cfg);
                }
            }
        }
    }

    public static double getBaseValue(ArmorAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        if (cfg != null) {
            return cfg.base;
        }
        // 兜底：保持与原始硬编码一致
        switch (attribute) {
            case HEAVY_ARMOR:
                return 2.0;
            case DAMAGE_LIMIT:
                return 40.0;
            case THORNS:
                return 5.0;
            case FIXED_DAMAGE:
                return 20.0;
            case SLOW:
                return 5.0;
            case STUN:
                return 3.0;
            case REGEN:
                return 0.5;
            case EXPLOSIVE:
                return 3.0;
            case SPEED:
                return 0.02;
            case REPAIR:
                return 1.0;
            case DODGE:
                return 3.0;
            case SHOCK:
                return 3.0;
            case SOUL_IMMUNE:
            case BERSERK:
            default:
                return 1.0;
        }
    }

    public static double getRandomFactorMin(ArmorAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        return cfg != null ? cfg.randomFactorMin : 0.8;
    }

    public static double getRandomFactorMax(ArmorAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        return cfg != null ? cfg.randomFactorMax : 1.2;
    }

    public static boolean isReverseScale(ArmorAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        if (cfg != null) {
            return cfg.reverseScale;
        }
        return attribute == ArmorAttribute.DAMAGE_LIMIT || attribute == ArmorAttribute.FIXED_DAMAGE;
    }

    public static double getAttributeRefreshWeight(ArmorAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        return cfg != null ? cfg.refreshWeight : 1.0;
    }

    public static double getAttributeRefreshChance(WeaponQuality quality) {
        QualityConfig cfg = QUALITY_CONFIGS.get(quality);
        if (cfg != null) {
            return cfg.attributeRefreshChance;
        }
        // 默认退回 WeaponConfig 的设置
        return site.backrer.professioncustom.weapon.WeaponConfig.getAttributeRefreshChance(quality);
    }

    public static int getMaxAttributesForQuality(WeaponQuality quality) {
        QualityConfig cfg = QUALITY_CONFIGS.get(quality);
        if (cfg != null) {
            return cfg.maxAttributes;
        }
        return site.backrer.professioncustom.weapon.WeaponConfig.getMaxAttributesForQuality(quality);
    }

    public static double getBaseValueMultiplier(WeaponQuality quality) {
        QualityConfig cfg = QUALITY_CONFIGS.get(quality);
        return cfg != null ? cfg.baseValueMultiplier : 1.0;
    }

    private static class QualityConfig {
        double attributeRefreshChance;
        int maxAttributes;
        double baseValueMultiplier = 1.0;
    }

    private static class AttributeConfig {
        double base = 1.0;
        double randomFactorMin = 0.8;
        double randomFactorMax = 1.2;
        boolean reverseScale = false;
        double refreshWeight = 1.0;
    }
}
