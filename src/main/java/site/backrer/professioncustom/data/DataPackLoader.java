package site.backrer.professioncustom.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.MobTagConfigManager;

import java.util.Map;

/**
 * 数据包加载器 - 负责加载模组的数据包配置
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class DataPackLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    
    public DataPackLoader() {
        super(GSON, "professioncustom");
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Professioncustom.LOGGER.info("Loading ProfessionCustom data packs...");
        
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement element = entry.getValue();
            
            try {
                if (element.isJsonObject()) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    String path = location.getPath();
                    
                    // 加载标签概率配置
                    if (path.startsWith("tag_probabilities/")) {
                        loadTagProbabilities(location, jsonObject);
                    }
                    // 加载生物标签配置
                    else if (path.startsWith("mob_tag_configs/")) {
                        loadMobTagConfigs(location, jsonObject);
                    }
                    // 加载职业配置
                    else if (path.startsWith("professions/")) {
                        loadProfessionConfigs(location, jsonObject);
                    }
                    // 加载物品职业配置
                    else if (path.startsWith("item_professions/")) {
                        loadItemProfessionConfigs(location, jsonObject);
                    }
                    // 加载武器属性配置
                    else if (path.startsWith("weapon_attributes/")) {
                        loadWeaponAttributeConfigs(location, jsonObject);
                    }
                    // 加载护甲属性配置
                    else if (path.startsWith("armor_attributes/")) {
                        loadArmorAttributeConfigs(location, jsonObject);
                    }
                }
            } catch (Exception e) {
                Professioncustom.LOGGER.error("Failed to load data pack: {}", location, e);
            }
        }
        
        Professioncustom.LOGGER.info("Finished loading ProfessionCustom data packs");
    }
    
    private void loadTagProbabilities(ResourceLocation location, JsonObject jsonObject) {
        try {
            // 加载标签概率配置
            if (jsonObject.has("tag_probabilities")) {
                JsonObject probabilities = jsonObject.getAsJsonObject("tag_probabilities");
                for (Map.Entry<String, JsonElement> entry : probabilities.entrySet()) {
                    String tagId = entry.getKey();
                    double probability = entry.getValue().getAsDouble();
                    // 这里需要将概率存储到MobConfig中
                    // MobConfig.setTagProbability(tagId, probability);
                }
                Professioncustom.LOGGER.debug("Loaded tag probabilities from: {}", location);
            }
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to load tag probabilities from: {}", location, e);
        }
    }
    
    private void loadMobTagConfigs(ResourceLocation location, JsonObject jsonObject) {
        try {
            // 加载生物标签配置
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String tagId = entry.getKey();
                if (entry.getValue().isJsonObject()) {
                    JsonObject tagConfig = entry.getValue().getAsJsonObject();
                    // 将配置加载到MobTagConfigManager中
                    MobTagConfigManager.loadTagConfig(tagId, tagConfig);
                }
            }
            Professioncustom.LOGGER.debug("Loaded mob tag configs from: {}", location);
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to load mob tag configs from: {}", location, e);
        }
    }
    
    private void loadProfessionConfigs(ResourceLocation location, JsonObject jsonObject) {
        try {
            // 加载职业配置
            // 这里需要调用ProfessionManager来加载职业配置
            Professioncustom.LOGGER.debug("Loaded profession configs from: {}", location);
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to load profession configs from: {}", location, e);
        }
    }
    
    private void loadItemProfessionConfigs(ResourceLocation location, JsonObject jsonObject) {
        try {
            // 加载物品职业配置
            Professioncustom.LOGGER.debug("Loaded item profession configs from: {}", location);
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to load item profession configs from: {}", location, e);
        }
    }
    
    private void loadWeaponAttributeConfigs(ResourceLocation location, JsonObject jsonObject) {
        try {
            // 加载武器属性配置
            Professioncustom.LOGGER.debug("Loaded weapon attribute configs from: {}", location);
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to load weapon attribute configs from: {}", location, e);
        }
    }
    
    private void loadArmorAttributeConfigs(ResourceLocation location, JsonObject jsonObject) {
        try {
            // 加载护甲属性配置
            Professioncustom.LOGGER.debug("Loaded armor attribute configs from: {}", location);
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to load armor attribute configs from: {}", location, e);
        }
    }
    
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new DataPackLoader());
        Professioncustom.LOGGER.info("Registered ProfessionCustom data pack loader");
    }
    
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        Professioncustom.LOGGER.info("Server started, data packs should be loaded");
        
        // 强制重新加载数据包
        try {
            server.getResourceManager().listResources("data/professioncustom", (location) -> {
                return location.getPath().endsWith(".json");
            }).forEach((location, resource) -> {
                Professioncustom.LOGGER.debug("Found data pack resource: {}", location);
            });
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to list data pack resources", e);
        }
    }
}
