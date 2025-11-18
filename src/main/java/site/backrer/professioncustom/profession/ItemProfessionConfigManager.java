package site.backrer.professioncustom.profession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


import java.util.*;
import java.util.function.Supplier;

import static site.backrer.professioncustom.Professioncustom.MODID;

/**
 * 物品职业配置管理器
 * 负责加载和管理物品与职业的关联配置
 */
@Mod.EventBusSubscriber(modid = MODID)
public class ItemProfessionConfigManager extends SimpleJsonResourceReloadListener {

    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "item_professions";
    
    // 存储物品ID到职业列表的映射
    private static final Map<String, List<String>> ITEM_PROFESSIONS = new HashMap<>();
    
    // 存储物品ID到配置对象的映射，用于缓存完整配置
    private static final Map<String, ItemProfessionData> ITEM_CONFIGS = new HashMap<>();
    
    // 静态实例，用于支持热加载
    private static ItemProfessionConfigManager INSTANCE;
    
    public ItemProfessionConfigManager() {
        super(GSON, FOLDER);
        INSTANCE = this;
    }
    
    /**
     * 获取单例实例
     */
    public static ItemProfessionConfigManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemProfessionConfigManager();
        }
        return INSTANCE;
    }
    
    /**
     * 当资源重新加载时调用，用于加载/重新加载物品职业配置
     */
    @Override
    protected void apply(Map<ResourceLocation, com.google.gson.JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ITEM_PROFESSIONS.clear();
        ITEM_CONFIGS.clear();
        
        for (Map.Entry<ResourceLocation, com.google.gson.JsonElement> entry : object.entrySet()) {
            String configName = entry.getKey().getPath();
            com.google.gson.JsonElement jsonElement = entry.getValue();
            
            try {
                // 确保是JsonObject类型
                if (jsonElement.isJsonObject()) {
                    com.google.gson.JsonObject jsonObject = jsonElement.getAsJsonObject();
                    // 解析JSON对象为ItemProfessionData
                    ItemProfessionData data = GSON.fromJson(jsonObject, ItemProfessionData.class);
                    
                    // 验证必要字段
                    if (data.itemId == null || data.itemId.isEmpty()) {
                        continue;
                    }
                    
                    if (data.professions == null || data.professions.isEmpty()) {
                        continue;
                    }
                    
                    // 转换为小写以确保不区分大小写的匹配
                    String itemIdLower = data.itemId.toLowerCase();
                    
                    // 存储物品ID到职业列表的映射
                    ITEM_PROFESSIONS.put(itemIdLower, new ArrayList<>(data.professions));
                    
                    // 缓存完整配置对象
                    ITEM_CONFIGS.put(itemIdLower, data);
                }
            } catch (Exception e) {
                // 静默处理异常，保持功能稳定
            }
        }
    }
    
    /**
     * 根据物品ID获取可使用该物品的职业列表
     * @param itemId 物品ID，格式为"modid:itemname"
     * @return 职业列表，如果没有找到则返回空列表
     */
    public static List<String> getProfessionsForItem(String itemId) {
        String itemIdLower = itemId.toLowerCase();
        List<String> professions = ITEM_PROFESSIONS.getOrDefault(itemIdLower, Collections.emptyList());
        
        return professions;
    }
    
    /**
     * 检查物品是否特定职业可使用
     * @param itemId 物品ID
     * @param professionName 职业名称
     * @return 如果职业可以使用该物品则返回true
     */
    public static boolean canProfessionUseItem(String itemId, String professionName) {
        List<String> professions = getProfessionsForItem(itemId);
        // 确保职业名称转换为小写进行比较
        String professionNameLower = professionName.toLowerCase();
        return professions.contains(professionNameLower);
    }
    
    /**
     * 获取物品的完整配置对象
     * @param itemId 物品ID
     * @return ItemProfessionData对象，如果没有找到则返回null
     */
    public static ItemProfessionData getItemConfig(String itemId) {
        String itemIdLower = itemId.toLowerCase();
        ItemProfessionData config = ITEM_CONFIGS.get(itemIdLower);
        
        return config;
    }
    
    /**
     * 获取所有已加载的物品职业配置
     */
    public static Map<String, List<String>> getAllItemProfessions() {
        return new HashMap<>(ITEM_PROFESSIONS);
    }
    
    /**
     * 注册资源加载监听器
     */
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ItemProfessionConfigManager());
    }
    
    /**
     * 手动触发配置重新加载
     * @param resourceManager 当前的资源管理器
     */
    public static void reload(ResourceManager resourceManager) {
        if (INSTANCE != null) {
            try {

                // 创建一个简单的ProfilerFiller实现
                ProfilerFiller profiler = new ProfilerFiller() {
                    @Override
                    public void push(String p_10152_) {}

                    @Override
                    public void push(Supplier<String> p_18582_) {}

                    public void push(String p_10153_, int p_10154_) {}
                    
                    @Override
                    public void pop() {}
                    
                    @Override
                    public void popPush(String p_10155_) {}

                    @Override
                    public void popPush(Supplier<String> p_18584_) {}

                    @Override
                    public void markForCharting(MetricCategory p_145959_) {}

                    @Override
                    public void incrementCounter(String p_185258_, int p_185259_) {}

                    @Override
                    public void incrementCounter(Supplier<String> p_185260_, int p_185261_) {}

                    @Override
                    public void endTick() {}
                    
                    @Override
                    public void startTick() {}
                };
                
                // 直接调用SimpleJsonResourceReloadListener的apply方法所需的资源加载逻辑
                net.minecraft.util.profiling.ProfilerFiller emptyProfiler = profiler;
                
                // 使用ResourceManager获取所有JSON文件
                Map<ResourceLocation, com.google.gson.JsonElement> jsonMap = new HashMap<>();
                try {
                    // 扫描配置文件夹中的所有JSON文件
                    resourceManager.listResources(FOLDER, path -> path.toString().endsWith(".json")).forEach((location, resource) -> {
                        try {
                            // 读取和解析JSON文件
                            try (java.io.InputStream inputStream = resource.open()) {
                                com.google.gson.JsonElement jsonElement = GSON.fromJson(new java.io.InputStreamReader(inputStream), com.google.gson.JsonElement.class);
                                jsonMap.put(location, jsonElement);
                            }
                        } catch (Exception e) {
                            // 静默处理异常
                        }
                    });
                    
                    // 调用apply方法应用配置
                    INSTANCE.apply(jsonMap, resourceManager, emptyProfiler);
                } catch (Exception e) {
                    // 静默处理异常
                }
            } catch (Exception e) {
                // 静默处理异常
            }
        }
    }
    
    /**
     * 物品职业配置数据类，用于JSON解析
     */
    public static class ItemProfessionData {
        // 物品ID，格式为"modid:itemname"
        public String itemId;
        
        // 可使用该物品的职业列表
        public List<String> professions;
        
        // 可选：物品描述前缀
        public String descriptionPrefix = "需要职业: ";
        
        // 可选：是否显示在物品lore中
        public boolean showInLore = true;
        
        // 可选：lore文本的颜色
        public String loreColor = "aqua";
    }
}
