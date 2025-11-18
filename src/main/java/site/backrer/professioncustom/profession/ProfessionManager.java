package site.backrer.professioncustom.profession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    
    private static final Map<String, Profession> PROFESSIONS = new HashMap<>();
    
    private static final Map<String, List<Profession>> PROFESSION_LEVELS = new HashMap<>();

    private static final Map<String, List<StartingGearItem>> PROFESSION_STARTING_GEAR = new HashMap<>();
    
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
        PROFESSION_STARTING_GEAR.clear();
        
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
                
                PROFESSIONS.put(data.name, profession);
                
                updateProfessionLevels(profession);

                if (data.startingGear != null && !data.startingGear.isEmpty()) {
                    PROFESSION_STARTING_GEAR.put(data.name, new java.util.ArrayList<>(data.startingGear));
                }
                
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
    
    public static void clearProfessionAttributes(Player player) {
        if (player == null) {
            return;
        }
        final UUID HEALTH_MODIFIER_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
        final UUID ARMOR_MODIFIER_ID = UUID.fromString("23456789-2345-2345-2345-234567890123");
        final UUID DAMAGE_MODIFIER_ID = UUID.fromString("34567890-3456-3456-3456-345678901234");

        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);

        if (maxHealthAttr != null) {
            maxHealthAttr.removeModifier(HEALTH_MODIFIER_ID);
            player.setHealth(Math.min(player.getHealth(), (float) maxHealthAttr.getValue()));
        }

        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_MODIFIER_ID);
        }

        if (damageAttr != null) {
            damageAttr.removeModifier(DAMAGE_MODIFIER_ID);
        }
    }
    
    public static void giveStartingGear(String professionName, Player player) {
        if (player == null || professionName == null || professionName.isEmpty()) {
            return;
        }
        List<StartingGearItem> gearList = PROFESSION_STARTING_GEAR.get(professionName);
        if (gearList == null || gearList.isEmpty()) {
            return;
        }
        for (StartingGearItem gear : gearList) {
            if (gear == null || gear.itemId == null || gear.itemId.isEmpty()) {
                continue;
            }
            ResourceLocation itemId = ResourceLocation.tryParse(gear.itemId);
            if (itemId == null) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null) {
                continue;
            }
            int count = gear.count <= 0 ? 1 : gear.count;
            ItemStack stack = new ItemStack(item, count);
            if (gear.nbt != null && !gear.nbt.isEmpty()) {
                try {
                    CompoundTag tag = TagParser.parseTag(gear.nbt);
                    stack.setTag(tag);
                } catch (Exception e) {
                }
            }
            String slotName = gear.slot != null ? gear.slot.toLowerCase(Locale.ROOT) : "";
            boolean equipped = false;
            if (!slotName.isEmpty()) {
                EquipmentSlot slot = null;
                if ("head".equals(slotName) || "helmet".equals(slotName)) {
                    slot = EquipmentSlot.HEAD;
                } else if ("chest".equals(slotName) || "chestplate".equals(slotName)) {
                    slot = EquipmentSlot.CHEST;
                } else if ("legs".equals(slotName) || "leggings".equals(slotName)) {
                    slot = EquipmentSlot.LEGS;
                } else if ("feet".equals(slotName) || "boots".equals(slotName)) {
                    slot = EquipmentSlot.FEET;
                } else if ("mainhand".equals(slotName) || "main_hand".equals(slotName)) {
                    slot = EquipmentSlot.MAINHAND;
                } else if ("offhand".equals(slotName) || "off_hand".equals(slotName)) {
                    slot = EquipmentSlot.OFFHAND;
                }
                if (slot != null) {
                    ItemStack existing = player.getItemBySlot(slot);
                    if (existing.isEmpty()) {
                        player.setItemSlot(slot, stack);
                        equipped = true;
                    }
                }
            }
            if (!equipped) {
                if (!player.addItem(stack)) {
                    player.drop(stack, false);
                }
            }
        }
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
        List<StartingGearItem> startingGear;
    }

    private static class StartingGearItem {
        String itemId;
        int count;
        String slot;
        String nbt;
    }
}
