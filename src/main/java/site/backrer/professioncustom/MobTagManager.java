package site.backrer.professioncustom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.MobConfig;

import java.util.*;
import javax.annotation.Nonnull;

/**
 * 生物标签管理器，负责为生物分配和管理标签
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class MobTagManager {
    // 存储生物的标签信息（标签ID -> 等级）
    private static final Map<UUID, Map<String, Integer>> entityTagLevels = new HashMap<>();
    
    // 可用的标签列表
    public enum MobTag {
        COLD("tag.professioncustom.cold", "professioncustom:tag_cold"),
        RESURRECT("tag.professioncustom.resurrect", "professioncustom:tag_resurrect"),
        BRUTAL("tag.professioncustom.brutal", "professioncustom:tag_brutal"),
        EXPLOSIVE("tag.professioncustom.explosive", "professioncustom:tag_explosive"),
        GREEDY("tag.professioncustom.greedy", "professioncustom:tag_greedy"),
        VAMPIRE("tag.professioncustom.vampire", "professioncustom:tag_vampire"),
        LIGHTBURST("tag.professioncustom.lightburst", "professioncustom:tag_lightburst"),
        FRENZY("tag.professioncustom.frenzy", "professioncustom:tag_frenzy"),
        SWIFT("tag.professioncustom.swift", "professioncustom:tag_swift"),
        UNMATCHED("tag.professioncustom.unmatched", "professioncustom:tag_unmatched"),
        SOUL("tag.professioncustom.soul", "professioncustom:tag_soul"),
        TELEPORT("tag.professioncustom.teleport", "professioncustom:tag_teleport"),
        CELESTIAL("tag.professioncustom.celestial", "professioncustom:tag_celestial"),
        LEADER("tag.professioncustom.leader", "professioncustom:tag_leader"),
        DESPERATE("tag.professioncustom.desperate", "professioncustom:tag_desperate");
        
        private final String translationKey;
        private final String tagId;
        
        MobTag(String translationKey, String tagId) {
            this.translationKey = translationKey;
            this.tagId = tagId;
        }
        
        /**
         * 获取翻译键
         * @return 翻译键
         */
        public String getTranslationKey() {
            return translationKey;
        }
        
        /**
         * 获取翻译后的显示名称
         * @return 翻译后的组件
         */
        public @Nonnull Component getDisplayName() {
            return Component.translatable(translationKey);
        }
        
        public String getDisplayNameString() {
            return Component.translatable(translationKey).getString();
        }
        
        public String getTagId() {
            return tagId;
        }
    }
    
    private static final Random RANDOM = new Random();
    
    /**
     * 当生物生成或加入世界时，为其随机分配标签
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!ProfessionConfig.isMobLevelingEnabled()) {
            return;
        }
        
        Entity entity = event.getEntity();
        // 排除玩家实体，不为玩家设置标签
        if (entity instanceof LivingEntity && !(entity instanceof Player) && !ProfessionConfig.isMobExcluded(entity)) {
            // 检查是否已经有标签，避免重复分配
            UUID uuid = entity.getUUID();
            if (!entityTagLevels.containsKey(uuid)) {
                // 延迟分配标签，确保实体完全加载
                if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.getServer().execute(() -> {
                        try {
                            // 随机分配1-2个标签，并分配等级
                            Map<String, Integer> assignedTags = assignRandomTagsWithLevels(entity);
                            entityTagLevels.put(uuid, assignedTags);
                            
                            // 为生物添加标签和等级标签
                            for (Map.Entry<String, Integer> entry : assignedTags.entrySet()) {
                                String tagId = entry.getKey();
                                int level = entry.getValue();
                                entity.addTag(tagId);
                                entity.addTag(tagId + "_level_" + level);
                            }
                            
                            // 添加通用标签标识
                            entity.addTag("professioncustom:has_tag");
                            
                            if (MobConfig.enableDebugLogging) {
                                Professioncustom.LOGGER.debug("Assigned tags to entity: {}, tags: {}", 
                                    entity.getType().getDescription().getString(), assignedTags);
                            }
                        } catch (Exception e) {
                            Professioncustom.LOGGER.error("Failed to assign tags to entity: {}", e.getMessage());
                        }
                    });
                }
            }
        }
    }
    
    /**
     * 为生物随机分配标签和等级
     * @param entity 生物实体
     * @return 分配的标签和等级映射（标签ID -> 等级）
     */
    private static Map<String, Integer> assignRandomTagsWithLevels(Entity entity) {
        Map<String, Integer> tagLevels = new HashMap<>();
        MobTag[] allTags = MobTag.values();
        
        // 获取生物等级，标签等级基于生物等级
        int mobLevel = MobLevelManager.getEntityLevel(entity);
        
        // 根据概率选择标签
        List<MobTag> selectedTags = new ArrayList<>();
        for (MobTag tag : allTags) {
            double probability = MobConfig.getTagProbability(tag.getTagId());
            if (RANDOM.nextDouble() < probability) {
                selectedTags.add(tag);
            }
        }
        
        // 如果没有任何标签被选中，至少选择一个（使用默认概率）
        if (selectedTags.isEmpty()) {
            // 随机选择一个标签
            MobTag randomTag = allTags[RANDOM.nextInt(allTags.length)];
            selectedTags.add(randomTag);
            
            if (Professioncustom.LOGGER.isDebugEnabled()) {
                Professioncustom.LOGGER.debug("没有标签通过概率检查，随机选择: {}", randomTag.getTagId());
            }
        }
        
        // 限制最多选择2个标签
        if (selectedTags.size() > 2) {
            Collections.shuffle(selectedTags, RANDOM);
            selectedTags = selectedTags.subList(0, 2);
        }
        
        // 为选中的标签分配等级
        for (MobTag tag : selectedTags) {
            // 标签等级：1-5级，基于生物等级随机分配
            // 生物等级越高，标签等级也倾向于更高
            int tagLevel = calculateTagLevel(mobLevel);
            tagLevels.put(tag.getTagId(), tagLevel);
        }
        
        return tagLevels;
    }
    
    /**
     * 根据生物等级计算标签等级
     * @param mobLevel 生物等级
     * @return 标签等级（1-5）
     */
    private static int calculateTagLevel(int mobLevel) {
        // 基础等级为1
        int baseLevel = 1;
        
        // 根据生物等级增加标签等级的概率
        // 生物等级越高，标签等级越高的概率越大
        if (mobLevel >= 20) {
            // 高等级生物：3-5级标签
            baseLevel = 3 + RANDOM.nextInt(3); // 3-5
        } else if (mobLevel >= 10) {
            // 中等级生物：2-4级标签
            baseLevel = 2 + RANDOM.nextInt(3); // 2-4
        } else if (mobLevel >= 5) {
            // 低中等级生物：1-3级标签
            baseLevel = 1 + RANDOM.nextInt(3); // 1-3
        } else {
            // 低等级生物：1-2级标签
            baseLevel = 1 + RANDOM.nextInt(2); // 1-2
        }
        
        return Math.min(5, Math.max(1, baseLevel)); // 确保在1-5范围内
    }
    
    /**
     * 获取生物的所有标签
     * @param entity 生物实体
     * @return 标签集合
     */
    public static Set<String> getEntityTags(Entity entity) {
        if (entity instanceof Player || ProfessionConfig.isMobExcluded(entity)) {
            return Collections.emptySet();
        }
        
        return getEntityTagLevels(entity).keySet();
    }
    
    /**
     * 获取生物的所有标签和等级
     * @param entity 生物实体
     * @return 标签和等级映射（标签ID -> 等级）
     */
    public static Map<String, Integer> getEntityTagLevels(Entity entity) {
        if (entity instanceof Player || ProfessionConfig.isMobExcluded(entity)) {
            return Collections.emptyMap();
        }
        
        UUID uuid = entity.getUUID();
        if (entityTagLevels.containsKey(uuid)) {
            return new HashMap<>(entityTagLevels.get(uuid));
        }
        
        // 如果缓存中没有，从实体标签中读取
        Map<String, Integer> tagLevels = new HashMap<>();
        for (String tag : entity.getTags()) {
            if (tag.startsWith("professioncustom:tag_") && !tag.contains("_level_")) {
                // 查找对应的等级标签
                int level = 1; // 默认等级
                for (String levelTag : entity.getTags()) {
                    if (levelTag.startsWith(tag + "_level_")) {
                        try {
                            level = Integer.parseInt(levelTag.substring((tag + "_level_").length()));
                        } catch (NumberFormatException e) {
                            level = 1;
                        }
                        break;
                    }
                }
                tagLevels.put(tag, level);
            }
        }
        
        // 更新缓存
        if (!tagLevels.isEmpty()) {
            entityTagLevels.put(uuid, tagLevels);
        }
        
        return tagLevels;
    }
    
    /**
     * 获取指定标签的等级
     * @param entity 生物实体
     * @param tagId 标签ID
     * @return 标签等级，如果没有该标签返回0
     */
    public static int getTagLevel(Entity entity, String tagId) {
        if (entity instanceof Player) {
            return 0;
        }
        
        return getEntityTagLevels(entity).getOrDefault(tagId, 0);
    }
    
    /**
     * 获取指定标签的等级（使用枚举）
     * @param entity 生物实体
     * @param tag 标签枚举
     * @return 标签等级，如果没有该标签返回0
     */
    public static int getTagLevel(Entity entity, MobTag tag) {
        return getTagLevel(entity, tag.getTagId());
    }
    
    /**
     * 检查生物是否拥有指定标签
     * @param entity 生物实体
     * @param tagId 标签ID
     * @return 是否拥有该标签
     */
    public static boolean hasTag(Entity entity, String tagId) {
        if (entity instanceof Player) {
            return false;
        }
        
        return getEntityTags(entity).contains(tagId);
    }
    
    /**
     * 检查生物是否拥有指定标签（使用枚举）
     * @param entity 生物实体
     * @param tag 标签枚举
     * @return 是否拥有该标签
     */
    public static boolean hasTag(Entity entity, MobTag tag) {
        return hasTag(entity, tag.getTagId());
    }
    
    /**
     * 获取生物标签的显示文本（带等级）
     * @param entity 生物实体
     * @return 标签显示文本列表
     */
    public static List<TagDisplayInfo> getTagDisplayInfo(Entity entity) {
        List<TagDisplayInfo> displayList = new ArrayList<>();
        
        if (entity instanceof Player) {
            return displayList;
        }
        
        Map<String, Integer> tagLevels = getEntityTagLevels(entity);
        if (tagLevels.isEmpty()) {
            return displayList;
        }
        
        for (MobTag tag : MobTag.values()) {
            String tagId = tag.getTagId();
            if (tagLevels.containsKey(tagId)) {
                int level = tagLevels.get(tagId);
                displayList.add(new TagDisplayInfo(tag, level));
            }
        }
        
        return displayList;
    }
    
    public static class TagDisplayInfo {
        private final MobTag tag;
        private final int level;
        
        public TagDisplayInfo(MobTag tag, int level) {
            this.tag = tag;
            this.level = level;
        }
        
        public MobTag getTag() {
            return tag;
        }
        
        public String getName() {
            return tag.getDisplayNameString();
        }
        
        public Component getNameComponent() {
            return tag.getDisplayName();
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDisplayText() {
            return getName() + " " + Component.translatable("tag.professioncustom.level").getString() + level;
        }
        
        public Component getDisplayComponent() {
            return Component.translatable("tag.professioncustom.level", level)
                    .append(" ")
                    .append(tag.getDisplayName());
        }
    }
    
    public static void removeEntityTags(UUID entityId) {
        entityTagLevels.remove(entityId);
    }
    
    /**
     * 更新实体的标签缓存（用于动态添加标签）
     * @param entityId 实体UUID
     * @param tagLevels 新的标签和等级映射
     */
    public static void updateEntityTagLevels(UUID entityId, Map<String, Integer> tagLevels) {
        entityTagLevels.put(entityId, new HashMap<>(tagLevels));
    }
    
    public static MobTag[] getAllTags() {
        return MobTag.values();
    }
}

