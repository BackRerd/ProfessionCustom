package site.backrer.professioncustom.weapon;

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

import java.util.EnumMap;
import java.util.Map;

import static site.backrer.professioncustom.Professioncustom.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class WeaponAttributeConfigManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "weapon_attributes";

    private static final Map<WeaponQuality, QualityConfig> QUALITY_CONFIGS = new EnumMap<>(WeaponQuality.class);
    private static final Map<WeaponAttribute, AttributeConfig> ATTRIBUTE_CONFIGS = new EnumMap<>(WeaponAttribute.class);

    public WeaponAttributeConfigManager() {
        super(GSON, FOLDER);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new WeaponAttributeConfigManager());
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

            if (root.has("attributes") && root.get("attributes").isJsonObject()) {
                JsonObject attrs = root.getAsJsonObject("attributes");
                for (WeaponAttribute attr : WeaponAttribute.values()) {
                    String key = attr.name().toLowerCase();
                    if (!attrs.has(key) || !attrs.get(key).isJsonObject()) {
                        continue;
                    }
                    JsonObject a = attrs.getAsJsonObject(key);
                    AttributeConfig cfg = new AttributeConfig();
                    cfg.base = a.has("base") ? a.get("base").getAsDouble() : 1.0;
                    cfg.randomFactorMin = a.has("random_factor_min") ? a.get("random_factor_min").getAsDouble() : 0.8;
                    cfg.randomFactorMax = a.has("random_factor_max") ? a.get("random_factor_max").getAsDouble() : 1.2;
                    cfg.refreshWeight = a.has("refresh_weight") ? a.get("refresh_weight").getAsDouble() : 1.0;
                    ATTRIBUTE_CONFIGS.put(attr, cfg);
                }
            }
        }
    }

    public static double getBaseValue(WeaponAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        if (cfg != null) {
            return cfg.base;
        }
        return 1.0;
    }

    public static double getRandomFactorMin(WeaponAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        return cfg != null ? cfg.randomFactorMin : 0.8;
    }

    public static double getRandomFactorMax(WeaponAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        return cfg != null ? cfg.randomFactorMax : 1.2;
    }

    public static double getAttributeRefreshWeight(WeaponAttribute attribute) {
        AttributeConfig cfg = ATTRIBUTE_CONFIGS.get(attribute);
        return cfg != null ? cfg.refreshWeight : 1.0;
    }

    public static double getAttributeRefreshChance(WeaponQuality quality) {
        QualityConfig cfg = QUALITY_CONFIGS.get(quality);
        if (cfg != null) {
            return cfg.attributeRefreshChance;
        }
        return WeaponConfig.getAttributeRefreshChance(quality);
    }

    public static int getMaxAttributesForQuality(WeaponQuality quality) {
        QualityConfig cfg = QUALITY_CONFIGS.get(quality);
        if (cfg != null) {
            return cfg.maxAttributes;
        }
        return WeaponConfig.getMaxAttributesForQuality(quality);
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
        double refreshWeight = 1.0;
    }
}
