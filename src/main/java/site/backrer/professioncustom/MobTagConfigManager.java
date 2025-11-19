package site.backrer.professioncustom;

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

import java.util.HashMap;
import java.util.Map;

import static site.backrer.professioncustom.Professioncustom.MODID;

/**
 * 生物标签配置管理器，从 data/professioncustom/mob_tag_configs/*.json 读取各标签的强度参数
 */
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobTagConfigManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "mob_tag_configs";

    private static final Map<String, JsonObject> TAG_CONFIGS = new HashMap<>();

    public MobTagConfigManager() {
        super(GSON, FOLDER);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new MobTagConfigManager());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        TAG_CONFIGS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            JsonElement jsonElement = entry.getValue();
            if (!jsonElement.isJsonObject()) {
                continue;
            }
            JsonObject root = jsonElement.getAsJsonObject();
            if (!root.has("tags") || !root.get("tags").isJsonObject()) {
                continue;
            }
            JsonObject tags = root.getAsJsonObject("tags");
            for (Map.Entry<String, JsonElement> tagEntry : tags.entrySet()) {
                if (!tagEntry.getValue().isJsonObject()) {
                    continue;
                }
                TAG_CONFIGS.put(tagEntry.getKey(), tagEntry.getValue().getAsJsonObject());
            }
        }
    }

    public static double getDouble(String tagId, String key, double defaultValue) {
        JsonObject obj = TAG_CONFIGS.get(tagId);
        if (obj != null && obj.has(key)) {
            try {
                return obj.get(key).getAsDouble();
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }

    public static int getInt(String tagId, String key, int defaultValue) {
        JsonObject obj = TAG_CONFIGS.get(tagId);
        if (obj != null && obj.has(key)) {
            try {
                return obj.get(key).getAsInt();
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }
    
    /**
     * 加载标签配置
     * @param tagId 标签ID
     * @param config 配置JSON对象
     */
    public static void loadTagConfig(String tagId, JsonObject config) {
        TAG_CONFIGS.put(tagId, config);
        Professioncustom.LOGGER.debug("Loaded tag config for: {}", tagId);
    }
    
    /**
     * 获取所有已加载的标签配置
     * @return 标签配置映射
     */
    public static Map<String, JsonObject> getAllConfigs() {
        return new HashMap<>(TAG_CONFIGS);
    }
}
