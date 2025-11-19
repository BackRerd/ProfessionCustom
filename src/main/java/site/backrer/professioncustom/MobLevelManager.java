package site.backrer.professioncustom;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.MobConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;

/**
 * 生物等级管理器，负责计算和管理生物的等级
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class MobLevelManager {
    // 存储生物的等级信息
    private static final Map<UUID, Integer> entityLevels = new HashMap<>();
    
    /**
     * 当生物生成或加入世界时，计算并设置其等级
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!ProfessionConfig.isMobLevelingEnabled()) {
            return;
        }
        
        Entity entity = event.getEntity();
        // 排除玩家实体，不为玩家设置等级
        if (entity instanceof LivingEntity && !(entity instanceof Player) && !ProfessionConfig.isMobExcluded(entity) && !entityLevels.containsKey(entity.getUUID())) {
            int level = calculateLevel(entity);
            entityLevels.put(entity.getUUID(), level);
            
            // 为生物添加等级标签
            addLevelTag(entity, level);
            
            // 延迟应用属性，确保实体完全加载
            if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.getServer().execute(() -> {
                    try {
                        site.backrer.professioncustom.level.MobAttributeCalculator.applyLevelAttributes((LivingEntity) entity);
                        if (MobConfig.enableDebugLogging) {
                            Professioncustom.LOGGER.debug("Applied level attributes for entity: {}, level: {}", 
                                entity.getType().getDescription().getString(), level);
                        }
                    } catch (Exception e) {
                        Professioncustom.LOGGER.error("Failed to apply level attributes: {}", e.getMessage());
                    }
                });
            }
        }
    }
    
    /**
     * 为生物添加等级标签
     */
    private static void addLevelTag(Entity entity, int level) {
        // 使用临时集合存储需要移除的标签，避免在遍历过程中修改集合
        java.util.List<String> tagsToRemove = new java.util.ArrayList<>();
        
        // 收集需要移除的旧等级标签
        for (String tag : entity.getTags()) {
            if (tag.startsWith("professioncustom:level_")) {
                tagsToRemove.add(tag);
            }
        }
        
        // 在遍历完成后移除标签
        for (String tag : tagsToRemove) {
            entity.removeTag(tag);
        }
        
        // 添加新的等级标签
        String levelTag = "professioncustom:level_" + level;
        entity.addTag(levelTag);
        
        // 添加通用等级标签，便于其他模组检测
        entity.addTag("professioncustom:has_level");
    }
    
    /**
     * 计算生物的等级，基于距离和维度
     */
    public static int calculateLevel(Entity entity) {
        // 排除玩家实体，为玩家返回默认等级
        if (!ProfessionConfig.isMobLevelingEnabled() || !(entity instanceof LivingEntity) || entity instanceof Player || ProfessionConfig.isMobExcluded(entity)) {
            return 1; // 默认等级
        }
        
        Level level = entity.level();
        @Nonnull BlockPos spawnPos = level.getSharedSpawnPos();
        @Nonnull BlockPos entityPos = entity.blockPosition();
        
        // 计算距离
        double distance = Math.sqrt(
            spawnPos.distSqr(entityPos)
        );
        
        // 重新计算基础等级，使等级增长更加明显
        // 修改公式：每50格增加1级，这样更容易观察到效果
        int baseLevel = (int) (distance / 50.0) + 1;
        
        // 应用维度加成
        ResourceKey<Level> dimension = level.dimension();
        // 使用Config.getDimensionLevelBonus方法获取维度加成，默认为1.0
        String dimensionId = dimension.location().toString();
        double dimensionBonus = MobConfig.getDimensionLevelBonus(dimensionId);
        // 如果配置中没有找到，默认为1.0
        if (dimensionBonus == 0.0) {
            dimensionBonus = 1.0;
        }
        
        // 计算最终等级
        int finalLevel = (int) (baseLevel * dimensionBonus);
        finalLevel = Math.max(1, finalLevel); // 确保等级至少为1
        
        return finalLevel;
    }
    
    /**
     * 获取生物的等级
     */
    public static int getEntityLevel(Entity entity) {
        // 排除玩家实体，为玩家返回默认等级
        if (!ProfessionConfig.isMobLevelingEnabled() || entity instanceof Player || ProfessionConfig.isMobExcluded(entity)) return 1;
        
        UUID uuid = entity.getUUID();
        if (!entityLevels.containsKey(uuid)) {
            // 如果没有缓存的等级，立即计算并缓存
            int level = calculateLevel(entity);
            entityLevels.put(uuid, level);
            
            // 为生物添加等级标签
            addLevelTag(entity, level);
            
            return level;
        }
        
        int level = entityLevels.get(uuid);
        
        // 确保标签与当前等级一致
        boolean hasCorrectTag = false;
        for (String tag : entity.getTags()) {
            if (tag.equals("professioncustom:level_" + level)) {
                hasCorrectTag = true;
                break;
            }
        }
        
        if (!hasCorrectTag) {
            addLevelTag(entity, level);
        }
        
        return level;
    }
    
    /**
     * 获取生物等级显示文本
     */
    public static String getLevelDisplay(Entity entity) {
        // 排除玩家实体，不显示玩家等级
        if (!ProfessionConfig.isMobLevelingEnabled() || entity instanceof Player || ProfessionConfig.isMobExcluded(entity)) {
            return "";
        }
        
        if (!ProfessionConfig.isShowExtraLevelTag()) {
            return "";
        }
        
        int level = getEntityLevel(entity);
        String display = "[Lv." + level + "]";
        return display;
    }
    
    /**
     * 清理不再存在的实体的等级数据
     */
    public static void removeEntityLevel(UUID entityId) {
        entityLevels.remove(entityId);
    }
    
    /**
     * 更新实体等级（用于网络同步）
     * @param entityId 实体UUID
     * @param level 新等级
     */
    public static void updateEntityLevel(UUID entityId, int level) {
        entityLevels.put(entityId, level);
    }
}