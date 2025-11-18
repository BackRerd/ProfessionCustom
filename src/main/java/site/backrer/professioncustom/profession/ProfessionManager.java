package site.backrer.professioncustom.profession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static site.backrer.professioncustom.Professioncustom.MODID;



/**
 * 职业管理器，负责从JSON配置文件加载和管理所有职业
 */
@Mod.EventBusSubscriber(modid = MODID)
public class ProfessionManager extends SimpleJsonResourceReloadListener {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "professions";
    
    // 存储所有已加载的职业，键为职业名称
    private static final Map<String, Profession> PROFESSIONS = new HashMap<>();
    
    // 存储职业等级映射，用于多级职业结构
    private static final Map<String, List<Profession>> PROFESSION_LEVELS = new HashMap<>();
    
    // 静态实例，用于支持热加载
    private static ProfessionManager INSTANCE;
    
    public ProfessionManager() {
        super(GSON, FOLDER);
        INSTANCE = this;
    }
    
    /**
     * 获取单例实例
     */
    public static ProfessionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfessionManager();
        }
        return INSTANCE;
    }
    
    /**
     * 当资源重新加载时调用，用于加载/重新加载职业配置
     */
    @Override
    protected void apply(Map<ResourceLocation, com.google.gson.JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        PROFESSIONS.clear();
        PROFESSION_LEVELS.clear();
        
        for (Map.Entry<ResourceLocation, com.google.gson.JsonElement> entry : object.entrySet()) {
            String professionName = entry.getKey().getPath();
            com.google.gson.JsonElement jsonElement = entry.getValue();
            
            try {
                // 确保是JsonObject类型
                if (jsonElement.isJsonObject()) {
                    com.google.gson.JsonObject jsonObjectData = jsonElement.getAsJsonObject();
                    // 解析JSON对象为ProfessionData（用于临时存储）
                    ProfessionData data = GSON.fromJson(jsonObjectData, ProfessionData.class);
                
                // 处理上级职业关系
                Profession upperProfession = null;
                if (data.upperProfession != null && !data.upperProfession.isEmpty()) {
                    upperProfession = PROFESSIONS.get(data.upperProfession);
                    if (upperProfession == null) {
                        // 如果上级职业尚未加载，先创建一个占位符，后续会更新
                        System.out.println("Warning: Upper profession " + data.upperProfession + " not found for " + professionName);
                    }
                }
                
                // 创建职业实例
                Profession profession = new Profession(
                    data.name,
                    data.displayName,
                    data.isNormal,
                    upperProfession,
                    data.professionLevel,
                    data.maxLevel,
                    data.maxExp,
                    data.multiplier,
                    data.health,
                    data.armor,
                    data.damage,
                    data.damageSpeed
                );
                
                // 存储职业
                PROFESSIONS.put(data.name, profession);
                
                // 更新职业等级映射
                updateProfessionLevels(profession);
                
                System.out.println("Loaded profession: " + data.name);
                } else {
                    System.err.println("Failed to load profession " + professionName + ": Not a valid JSON object");
                    continue;
                }
            } catch (Exception e) {
                System.err.println("Failed to load profession " + professionName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 修复上级职业引用
        fixUpperProfessionReferences();
        
        System.out.println("Successfully loaded " + PROFESSIONS.size() + " professions");
    }
    
    /**
     * 更新职业等级映射
     */
    private void updateProfessionLevels(Profession profession) {
        String baseProfessionName;
        if (profession.isNormal()) {
            baseProfessionName = profession.getName();
        } else if (profession.getUpperProfession() != null) {
            baseProfessionName = profession.getUpperProfession().getName();
        } else {
            baseProfessionName = "unknown";
        }
        
        PROFESSION_LEVELS.computeIfAbsent(baseProfessionName, k -> new java.util.ArrayList<>()).add(profession);
    }
    
    /**
     * 修复上级职业引用
     */
    private void fixUpperProfessionReferences() {
        // 首先创建一个映射，存储职业名称到职业对象的关系
        Map<String, Profession> nameToProfession = new HashMap<>();
        for (Profession profession : PROFESSIONS.values()) {
            nameToProfession.put(profession.getName(), profession);
        }
        
        // 然后修复所有职业的上级职业引用
        for (Profession profession : PROFESSIONS.values()) {
            // 对于所有进阶职业，确保它们的上级职业引用正确
            if (!profession.isNormal() && profession.getUpperProfession() != null) {
                String upperProfessionName = profession.getUpperProfession().getName();
                // 由于我们在构造函数中可能使用了占位符，这里确保使用正确的对象引用
                Profession actualUpperProfession = nameToProfession.get(upperProfessionName);
                if (actualUpperProfession != null) {
                    profession.setUpperProfession(actualUpperProfession);
                }
            }
        }
    }
    
    /**
     * 通过名称获取职业
     */
    public static Profession getProfessionByName(String name) {
        return PROFESSIONS.get(name);

    }
     /**
      * 根据玩家职业设置属性修改器
      * 使用属性修改器系统而非直接设置基础值，避免与其他模组冲突
      */
     public static void setAttributeByProfessionInPlayer(String professionName, Player player){
         // 创建修改器的唯一标识符
         final UUID HEALTH_MODIFIER_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
         final UUID ARMOR_MODIFIER_ID = UUID.fromString("23456789-2345-2345-2345-234567890123");
         final UUID DAMAGE_MODIFIER_ID = UUID.fromString("34567890-3456-3456-3456-345678901234");
         
         Profession professionByName = getProfessionByName(professionName);
         if (professionByName == null) return;
         
         ProfessionHelper.PlayerProfessionInfo playerProfessionInfo = ProfessionHelper.getPlayerProfessionInfo(player);
         if (playerProfessionInfo == null) return;
         
         // 获取职业最大等级
         int maxLevel = professionByName.getMaxLevel();
         // 获取玩家当前等级，并确保不超过职业最大等级
         int effectiveLevel = Math.min(playerProfessionInfo.getLevel(), maxLevel);
         
         // 计算职业加成值（基于有效等级和职业属性）
         double healthBonus = effectiveLevel * professionByName.getHealth() * professionByName.getMultiplier();
         double armorBonus = effectiveLevel * professionByName.getArmor() * professionByName.getMultiplier();
         double damageBonus = effectiveLevel * professionByName.getDamage() * professionByName.getMultiplier();

         // 获取属性实例
         AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
         AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
         AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);

         // 移除之前可能存在的修改器
         if (maxHealthAttr != null) {
             maxHealthAttr.removeModifier(HEALTH_MODIFIER_ID);
             // 添加新地修改器
             maxHealthAttr.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, "ProfessionCustom profession health bonus", healthBonus, AttributeModifier.Operation.ADDITION));
         }
         
         if (armorAttr != null) {
             armorAttr.removeModifier(ARMOR_MODIFIER_ID);
             // 添加新地修改器
             armorAttr.addPermanentModifier(new AttributeModifier(ARMOR_MODIFIER_ID, "ProfessionCustom profession armor bonus", armorBonus, AttributeModifier.Operation.ADDITION));
         }
         
         if (damageAttr != null) {
             damageAttr.removeModifier(DAMAGE_MODIFIER_ID);
             // 添加新地修改器
             damageAttr.addPermanentModifier(new AttributeModifier(DAMAGE_MODIFIER_ID, "ProfessionCustom profession damage bonus", damageBonus, AttributeModifier.Operation.ADDITION));
         }
         
         // 确保玩家生命值更新
         if (maxHealthAttr != null) {
             player.setHealth(Math.min(player.getHealth(), (float) maxHealthAttr.getValue()));
         }
     }
    
    /**
     * 获取所有职业
     */
    public static Map<String, Profession> getAllProfessions() {
        return new HashMap<>(PROFESSIONS);
    }
    
    /**
     * 获取所有基础职业（一级职业）
     */
    public static List<Profession> getNormalProfessions() {
        List<Profession> result = new java.util.ArrayList<>();
        for (Profession profession : PROFESSIONS.values()) {
            if (profession.isNormal()) {
                result.add(profession);
            }
        }
        return result;
    }
    
    /**
     * 获取指定基础职业的所有进阶职业
     */
    public static List<Profession> getAdvancedProfessions(String baseProfessionName) {
        List<Profession> result = new java.util.ArrayList<>();
        for (Profession profession : PROFESSIONS.values()) {
            if (!profession.isNormal() && profession.getUpperProfession() != null && 
                profession.getUpperProfession().getName().equals(baseProfessionName)) {
                result.add(profession);
            }
        }
        return result;
    }
    
    /**
     * 注册资源加载监听器
     */
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ProfessionManager());
    }
    
    /**
     * 手动触发职业配置重新加载
     * @param resourceManager 当前的资源管理器
     */
    public static void reload(ResourceManager resourceManager) {
        if (INSTANCE != null) {
            try {
                System.out.println("Triggering manual reload of profession configurations...");
                // 创建一个简单的ProfilerFiller实现
                ProfilerFiller profiler = new ProfilerFiller() {
                    @Override
                    public void push(String p_10152_) {}

                    @Override
                    public void push(Supplier<String> p_18582_) {

                    }


                    @Override
                    public void pop() {}
                    
                    @Override
                    public void popPush(String p_10155_) {}

                    @Override
                    public void popPush(Supplier<String> p_18584_) {

                    }

                    @Override
                    public void markForCharting(MetricCategory p_145959_) {

                    }

                    @Override
                    public void incrementCounter(String p_185258_, int p_185259_) {

                    }

                    @Override
                    public void incrementCounter(Supplier<String> p_185260_, int p_185261_) {

                    }

                    @Override
                    public void endTick() {}
                    
                    @Override
                    public void startTick() {}
                };
                
                // 直接调用SimpleJsonResourceReloadListener的apply方法所需的资源加载逻辑
                // 创建一个TaskQueue来触发资源加载
                net.minecraft.util.profiling.ProfilerFiller emptyProfiler = profiler;
                
                // 使用ResourceManager获取所有JSON文件
                Map<ResourceLocation, com.google.gson.JsonElement> jsonMap = new HashMap<>();
                try {
                    // 扫描配置文件夹中的所有JSON文件
                    String folderPath = "data/" + MODID + "/" + FOLDER;
                    System.out.println("Scanning for configs in: " + folderPath);
                    
                    // 获取所有资源位置
                    resourceManager.listResources(FOLDER, path -> path.toString().endsWith(".json")).forEach((location, resource) -> {
                        try {
                            // 读取和解析JSON文件
                            try (java.io.InputStream inputStream = resource.open()) {
                                com.google.gson.JsonElement jsonElement = GSON.fromJson(new java.io.InputStreamReader(inputStream), com.google.gson.JsonElement.class);
                                jsonMap.put(location, jsonElement);
                                System.out.println("Loaded config file: " + location);
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to load config file: " + location + ", error: " + e.getMessage());
                        }
                    });
                    
                    System.out.println("Found " + jsonMap.size() + " config files to reload");
                    // 调用apply方法应用配置
                    INSTANCE.apply(jsonMap, resourceManager, emptyProfiler);
                    System.out.println("Manual profession reload completed successfully!");
                } catch (Exception e) {
                    System.err.println("Error during resource scanning: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.err.println("Failed to reload profession configurations: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Cannot reload: ProfessionManager instance is not initialized");
        }
    }
    
    /**
     * 临时数据类，用于JSON解析
     */
    private static class ProfessionData {
        String name;
        String displayName;
        boolean isNormal;
        String upperProfession;
        int professionLevel;
        int maxLevel;
        int maxExp;
        double multiplier;
        double health;
        double armor;
        double damage;
        double damageSpeed;
    }
}
