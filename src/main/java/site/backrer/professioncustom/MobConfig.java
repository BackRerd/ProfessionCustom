package site.backrer.professioncustom;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

// 生物等级系统配置类
@Mod.EventBusSubscriber(modid = Professioncustom.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MobConfig {
    // 数据包配置相关常量
    private static final String DATA_PACK_NAMESPACE = "professioncustom";
    private static final String DIMENSION_BONUSES_PATH = "dimension_bonuses";
    private static final String MOB_BONUSES_PATH = "mob_bonuses";
    private static final String TAG_PROBABILITIES_PATH = "tag_probabilities";
    
    // 数据包配置存储 - 使用ConcurrentHashMap保证线程安全
    private static final Map<String, Double> dataPackDimensionBonuses = new ConcurrentHashMap<>();
    private static final Map<String, MobBonusConfig> dataPackMobBonuses = new ConcurrentHashMap<>();
    private static final Map<String, Double> dataPackTagProbabilities = new ConcurrentHashMap<>();
    
    // 调试开关 - 默认关闭以避免输出debug信息
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING = BUILDER
            .comment("是否启用调试日志 - 建议启用以诊断数据包配置问题")
            .define("enableDebugLogging", true);
    
    static final ForgeConfigSpec SPEC = BUILDER.build();

    // 调试配置字段
    public static boolean enableDebugLogging;
    
    // 特定生物的属性提升倍率配置类
    public static class MobBonusConfig {
        public final double healthMultiplier;
        public final double armorMultiplier;
        public final double armorToughnessMultiplier;
        public final double attackDamageMultiplier;
        public final boolean enabled;
        
        public MobBonusConfig(double healthMultiplier, double armorMultiplier, double armorToughnessMultiplier, double attackDamageMultiplier, boolean enabled) {
            this.healthMultiplier = healthMultiplier;
            this.armorMultiplier = armorMultiplier;
            this.armorToughnessMultiplier = armorToughnessMultiplier;
            this.attackDamageMultiplier = attackDamageMultiplier;
            this.enabled = enabled;
        }
    }
    
    // 实际加载数据包配置
    public static void onResourceManagerReload(ResourceManager resourceManager) {
        try {
            // 清空当前配置
            dataPackDimensionBonuses.clear();
            dataPackMobBonuses.clear();
            dataPackTagProbabilities.clear();
            
            // 加载维度加成配置
            loadDimensionBonuses(resourceManager);
            
            // 加载生物加成配置
            loadMobBonuses(resourceManager);
            
            // 加载标签概率配置
            loadTagProbabilities(resourceManager);
            
            Professioncustom.LOGGER.info("Data pack configs loaded successfully: {} dimension bonuses, {} mob bonuses, {} tag probabilities", 
                    dataPackDimensionBonuses.size(), dataPackMobBonuses.size(), dataPackTagProbabilities.size());
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Error during data pack config reload", e);
        }
    }
    
    // 加载维度加成配置
    private static void loadDimensionBonuses(ResourceManager resourceManager) {
        try {
            // 清空旧配置，确保新配置完全覆盖
            dataPackDimensionBonuses.clear();
            Professioncustom.LOGGER.info("开始加载数据包维度加成配置");
            
            // 查找所有维度加成配置文件
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(DIMENSION_BONUSES_PATH,
                    path -> path.toString().endsWith(".json"));
            
            // 也搜索根目录下的配置文件，支持example_config.json的路径
            Map<ResourceLocation, Resource> rootResources = resourceManager.listResources(DATA_PACK_NAMESPACE,
                    path -> path.toString().endsWith(".json"));
            
            // 尝试直接搜索所有json文件，确保能找到example_config.json
            Map<ResourceLocation, Resource> allJsonResources = resourceManager.listResources("",
                    path -> path.toString().endsWith(".json"));
            
            // 合并所有找到的资源
            Map<ResourceLocation, Resource> allResources = new HashMap<>();
            allResources.putAll(resources);
            allResources.putAll(rootResources);
            allResources.putAll(allJsonResources);
            
            Professioncustom.LOGGER.info("总共找到 {} 个JSON配置文件需要处理", allResources.size());
            
            // 特别检查是否存在example_config.json文件
            boolean foundExampleConfig = false;
            for (ResourceLocation loc : allResources.keySet()) {
                if (loc.toString().contains("example_config.json")) {
                    foundExampleConfig = true;
                    Professioncustom.LOGGER.info("找到example_config.json文件: {}", loc);
                    break;
                }
            }
            
            if (!foundExampleConfig) {
                Professioncustom.LOGGER.warn("未找到example_config.json文件");
            }
            
            int processedFiles = 0;
            int loadedBonuses = 0;
            
            for (Map.Entry<ResourceLocation, Resource> entry : allResources.entrySet()) {
                ResourceLocation resourceLocation = entry.getKey();
                Resource resource = entry.getValue();
                
                Professioncustom.LOGGER.debug("处理配置文件: {}", resourceLocation);
                
                try (Reader reader = new BufferedReader(new InputStreamReader(
                        resource.open()))) {
                    String fileContent = new BufferedReader(reader).lines()
                            .collect(Collectors.joining(System.lineSeparator()));
                    
                    // 检查文件内容是否包含dimension_bonuses
                    if (fileContent.contains("dimension_bonuses")) {
                        Professioncustom.LOGGER.info("文件 {} 包含dimension_bonuses配置", resourceLocation);
                        
                        JsonObject jsonObject = new Gson().fromJson(fileContent, JsonObject.class);
                        
                        // 处理维度加成数据
                        if (jsonObject.has("dimension_bonuses")) {
                            JsonObject bonusesObject = jsonObject.getAsJsonObject("dimension_bonuses");
                            Professioncustom.LOGGER.info("从文件 {} 加载了 {} 个维度加成配置", 
                                    resourceLocation, bonusesObject.size());
                            
                            for (Map.Entry<String, JsonElement> bonusEntry : bonusesObject.entrySet()) {
                                String dimensionId = bonusEntry.getKey();
                                double bonus = bonusEntry.getValue().getAsDouble();
                                dataPackDimensionBonuses.put(dimensionId, bonus);
                                Professioncustom.LOGGER.info("✅ 加载维度加成: {} = {}", dimensionId, bonus);
                                loadedBonuses++;
                            }
                        } else {
                            Professioncustom.LOGGER.warn("文件 {} 包含dimension_bonuses文本但解析后未找到该字段", resourceLocation);
                        }
                    } else {
                        Professioncustom.LOGGER.debug("文件 {} 不包含dimension_bonuses配置，跳过处理", resourceLocation);
                    }
                    
                    processedFiles++;
                } catch (Exception e) {
                    Professioncustom.LOGGER.error("读取配置文件 {} 时出错: {}", resourceLocation, e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Professioncustom.LOGGER.info("维度加成配置加载完成: 处理了 {} 个文件, 加载了 {} 个维度加成", 
                    processedFiles, loadedBonuses);
            if (loadedBonuses > 0) {
                Professioncustom.LOGGER.info("最终加载的维度加成配置: {}", dataPackDimensionBonuses);
            }
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Error loading dimension bonuses from data packs", e);
        }
    }
    
    // 加载生物加成配置
    private static void loadMobBonuses(ResourceManager resourceManager) {
        try {
            // 查找所有生物加成配置文件
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(MOB_BONUSES_PATH, 
                    path -> path.toString().endsWith(".json"));
            
            // 也搜索根目录下的配置文件，支持示例配置的路径
            Map<ResourceLocation, Resource> rootResources = resourceManager.listResources(DATA_PACK_NAMESPACE, 
                    path -> path.toString().endsWith(".json"));
            resources.putAll(rootResources);
            
            for (ResourceLocation resourceLocation : resources.keySet()) {
                try (Reader reader = new BufferedReader(new InputStreamReader(
                        resources.get(resourceLocation).open()))) {
                    JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
                    
                    // 处理生物加成数据
                    if (jsonObject.has("mob_bonuses")) {
                        JsonObject bonusesObject = jsonObject.getAsJsonObject("mob_bonuses");
                        for (Map.Entry<String, JsonElement> entry : bonusesObject.entrySet()) {
                            String mobType = entry.getKey();
                            JsonObject mobData = entry.getValue().getAsJsonObject();
                            
                            // 读取各项倍率
                            double healthMultiplier = mobData.has("health_multiplier") ? 
                                    mobData.get("health_multiplier").getAsDouble() : 1.0;
                            double armorMultiplier = mobData.has("armor_multiplier") ? 
                                    mobData.get("armor_multiplier").getAsDouble() : 1.0;
                            double armorToughnessMultiplier = mobData.has("armor_toughness_multiplier") ? 
                                    mobData.get("armor_toughness_multiplier").getAsDouble() : 1.0;
                            double attackDamageMultiplier = mobData.has("attack_damage_multiplier") ? 
                                    mobData.get("attack_damage_multiplier").getAsDouble() : 1.0;
                            boolean enabled = true;
                            if (mobData.has("enable")) {
                                try {
                                    JsonElement enableElement = mobData.get("enable");
                                    if (enableElement.isJsonPrimitive()) {
                                        JsonPrimitive primitive = enableElement.getAsJsonPrimitive();
                                        if (primitive.isBoolean()) {
                                            enabled = primitive.getAsBoolean();
                                        } else if (primitive.isString()) {
                                            String text = primitive.getAsString();
                                            enabled = !"false".equalsIgnoreCase(text);
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                            
                            // 创建并存储配置
                            dataPackMobBonuses.put(mobType, new MobBonusConfig(
                                    healthMultiplier, armorMultiplier, armorToughnessMultiplier, attackDamageMultiplier, enabled));
                            
                            Professioncustom.LOGGER.debug("Loaded data pack mob bonus for {}: health={}, armor={}, armorToughness={}, attack={}", 
                                    mobType, healthMultiplier, armorMultiplier, armorToughnessMultiplier, attackDamageMultiplier);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Error loading mob bonuses from data packs", e);
        }
    }
    
    // 加载标签概率配置
    private static void loadTagProbabilities(ResourceManager resourceManager) {
        try {
            // 清空旧配置
            dataPackTagProbabilities.clear();
            Professioncustom.LOGGER.info("开始加载数据包标签概率配置");
            
            // 查找所有标签概率配置文件
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(TAG_PROBABILITIES_PATH,
                    path -> path.toString().endsWith(".json"));
            
            // 也搜索根目录下的配置文件
            Map<ResourceLocation, Resource> rootResources = resourceManager.listResources(DATA_PACK_NAMESPACE,
                    path -> path.toString().endsWith(".json"));
            resources.putAll(rootResources);
            
            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation resourceLocation = entry.getKey();
                Resource resource = entry.getValue();
                
                try (Reader reader = new BufferedReader(new InputStreamReader(resource.open()))) {
                    String fileContent = new BufferedReader(reader).lines()
                            .collect(Collectors.joining(System.lineSeparator()));
                    
                    // 检查文件内容是否包含tag_probabilities
                    if (fileContent.contains("tag_probabilities")) {
                        Professioncustom.LOGGER.info("文件 {} 包含tag_probabilities配置", resourceLocation);
                        
                        JsonObject jsonObject = new Gson().fromJson(fileContent, JsonObject.class);
                        
                        // 处理标签概率数据
                        if (jsonObject.has("tag_probabilities")) {
                            JsonObject probabilitiesObject = jsonObject.getAsJsonObject("tag_probabilities");
                            Professioncustom.LOGGER.info("从文件 {} 加载了 {} 个标签概率配置", 
                                    resourceLocation, probabilitiesObject.size());
                            
                            for (Map.Entry<String, JsonElement> probEntry : probabilitiesObject.entrySet()) {
                                String tagId = probEntry.getKey();
                                double probability = probEntry.getValue().getAsDouble();
                                
                                // 验证概率范围（0.0-1.0）
                                if (probability < 0.0 || probability > 1.0) {
                                    Professioncustom.LOGGER.warn("标签 {} 的概率 {} 超出范围(0.0-1.0)，将被忽略", tagId, probability);
                                    continue;
                                }
                                
                                dataPackTagProbabilities.put(tagId, probability);
                                Professioncustom.LOGGER.info("✅ 加载标签概率: {} = {}", tagId, probability);
                            }
                        } else {
                            Professioncustom.LOGGER.warn("文件 {} 包含tag_probabilities文本但解析后未找到该字段", resourceLocation);
                        }
                    } else {
                        Professioncustom.LOGGER.debug("文件 {} 不包含tag_probabilities配置，跳过处理", resourceLocation);
                    }
                } catch (Exception e) {
                    Professioncustom.LOGGER.error("读取配置文件 {} 时出错: {}", resourceLocation, e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Professioncustom.LOGGER.info("标签概率配置加载完成: 加载了 {} 个标签概率", dataPackTagProbabilities.size());
            if (dataPackTagProbabilities.size() > 0) {
                Professioncustom.LOGGER.info("最终加载的标签概率配置: {}", dataPackTagProbabilities);
            }
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Error loading tag probabilities from data packs", e);
        }
    }
    
    // 获取指定标签的出现概率 - 优先使用数据包配置，后使用默认值
    public static double getTagProbability(String tagId) {
        // 优先返回数据包配置
        if (dataPackTagProbabilities.containsKey(tagId)) {
            return dataPackTagProbabilities.get(tagId);
        }
        // 默认概率为 0.25 (25%)
        return 0.25;
    }
    
    // 数据包配置监听器类
    public static class DataPackConfigListener extends SimpleJsonResourceReloadListener {
        public DataPackConfigListener() {
            super(new Gson(), "professioncustom");
        }
        
        @Override
        protected void apply(@Nonnull Map<ResourceLocation, JsonElement> resources, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller profiler) {
            // 委托给静态方法处理重载
            onResourceManagerReload(resourceManager);
        }
        
        public static void register(ResourceManager resourceManager) {
            try {
                // 直接调用重载方法
                onResourceManagerReload(resourceManager);
            } catch (Exception e) {
                Professioncustom.LOGGER.error("Failed to register data pack config listener", e);
            }
        }
    }
    
    // 获取指定维度的等级加成 - 优先使用数据包配置，后使用文件配置
    public static double getDimensionLevelBonus(String dimensionId) {
        // 特殊处理overworld维度ID
        if (dimensionId != null) {
            String dimensionIdLower = dimensionId.toLowerCase();
            
            // 检查原始ID是否匹配
            if (dataPackDimensionBonuses.containsKey(dimensionId)) {
                return dataPackDimensionBonuses.get(dimensionId);
            }
            
            // Minecraft中常见的overworld维度ID变体
            String[] possibleOverworldIds = {
                "minecraft:overworld",
                "overworld",
                "minecraft:the_overworld",
                "the_overworld"
            };
            
            // 检查是否为overworld相关维度
            if (dimensionIdLower.contains("overworld")) {
                // 尝试所有可能的overworld ID变体
                for (String overworldId : possibleOverworldIds) {
                    if (dataPackDimensionBonuses.containsKey(overworldId)) {
                        return dataPackDimensionBonuses.get(overworldId);
                    }
                }
            }
            
            // 执行大小写不敏感的匹配
            for (Map.Entry<String, Double> entry : dataPackDimensionBonuses.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(dimensionId)) {
                    return entry.getValue();
                }
                
                // 移除命名空间后的匹配
                String cleanKey = entry.getKey();
                String cleanDimensionId = dimensionId;
                
                if (cleanKey.contains(":")) {
                    cleanKey = cleanKey.split(":")[1];
                }
                if (cleanDimensionId.contains(":")) {
                    cleanDimensionId = cleanDimensionId.split(":")[1];
                }
                
                if (cleanKey.equalsIgnoreCase(cleanDimensionId)) {
                    return entry.getValue();
                }
            }
        }
        
        // 后备使用默认值
        return 0.0;
    }
    
    // 获取指定生物类型的属性提升倍率配置 - 优先使用数据包配置，后使用文件配置
    public static MobBonusConfig getMobBonusConfig(String mobType) {
        // 优先返回数据包配置
        if (dataPackMobBonuses.containsKey(mobType)) {
            MobBonusConfig config = dataPackMobBonuses.get(mobType);
            // 添加调试日志，确认数据包配置被正确应用
            if (enableDebugLogging) {
                Professioncustom.LOGGER.debug("Using data pack config for {}: health={}, armor={}, toughness={}, attack={}",
                        mobType, config.healthMultiplier, config.armorMultiplier, 
                        config.armorToughnessMultiplier, config.attackDamageMultiplier);
            }
            return config;
        }
        // 后备使用默认值
        MobBonusConfig config = new MobBonusConfig(1.0, 1.0, 1.0, 1.0, true);
        // 添加调试日志，确认使用的是文件配置或默认值
        if (enableDebugLogging) {
            Professioncustom.LOGGER.debug("Using default config for {}: health={}, armor={}, toughness={}, attack={}",
                    mobType, config.healthMultiplier, config.armorMultiplier, 
                    config.armorToughnessMultiplier, config.attackDamageMultiplier);
        }
        return config;
    }

    public static boolean isMobDisabled(String mobType) {
        if (mobType == null) {
            return false;
        }
        MobBonusConfig config = dataPackMobBonuses.get(mobType);
        if (config != null) {
            return !config.enabled;
        }
        return false;
    }
    
    // 注册配置相关事件
    @Mod.EventBusSubscriber(modid = Professioncustom.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            try {
                // 配置重新加载时的处理
                Professioncustom.LOGGER.info("Mob config reloaded");
                
                // 重新加载配置到内存变量
                if (event.getConfig().getSpec() == SPEC) {
                    loadConfigValues();
                }
            } catch (Exception e) {
                Professioncustom.LOGGER.error("Error during mob config reload", e);
            }
        }
        
        @SubscribeEvent
        public static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
            try {
                // 服务器启动时初始化数据包配置系统
                DataPackConfig.initialize();
            } catch (Exception e) {
                Professioncustom.LOGGER.error("Error during server startup initialization", e);
            }
        }
        
        @SubscribeEvent
        public static void onAddReloadListener(AddReloadListenerEvent event) {
            try {
                // 注册我们的数据包配置监听器
                event.addListener(new DataPackConfigListener());
                Professioncustom.LOGGER.info("Data pack config listener registered");
            } catch (Exception e) {
                Professioncustom.LOGGER.error("Error in add reload listener event", e);
            }
        }
    }

    // 将配置加载逻辑抽取为单独的方法，便于在多个地方调用
    private static void loadConfigValues() {
        try {
            // 加载调试配置
            enableDebugLogging = ENABLE_DEBUG_LOGGING.get();
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to load mob config values", e);
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        try {
            // 处理配置加载事件
            if (event.getConfig().getSpec() == SPEC) {
                loadConfigValues();
                Professioncustom.LOGGER.info("Mob config loaded successfully");
            }
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Error during mob config load event", e);
        }
    }
    
    // 数据包配置支持
    public static class DataPackConfig {
        public static void initialize() {
            try {
                // 初始化数据包配置系统
                Professioncustom.LOGGER.info("Data pack config system initialized");
                
                // 注册到Forge事件总线
                MinecraftForge.EVENT_BUS.register(ForgeBusEvents.class);
            } catch (Exception e) {
                Professioncustom.LOGGER.error("Error initializing data pack config system", e);
            }
        }
    }
}
