package site.backrer.professioncustom.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.MobTagManager;
import site.backrer.professioncustom.Professioncustom;

/**
 * 生物事件处理器，负责处理生物相关的事件
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class MobEventHandler {
    
    /**
     * 当生物死亡时，清理其等级数据和标签以防止内存泄漏
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity != null) {
            // 使用临时集合存储需要移除的标签，避免在遍历过程中修改集合
            java.util.List<String> tagsToRemove = new java.util.ArrayList<>();
            
            // 收集需要移除的标签（包括等级标签和生物标签）
            for (String tag : entity.getTags()) {
                if (tag.startsWith("professioncustom:level_") || 
                    tag.equals("professioncustom:has_level") ||
                    tag.startsWith("professioncustom:tag_") ||
                    tag.equals("professioncustom:has_tag")) {
                    tagsToRemove.add(tag);
                }
            }
            
            // 在遍历完成后移除标签
            for (String tag : tagsToRemove) {
                entity.removeTag(tag);
            }
            
            // 移除实体的等级数据
            MobLevelManager.removeEntityLevel(entity.getUUID());
            
            // 移除实体的标签数据
            MobTagManager.removeEntityTags(entity.getUUID());
        }
    }
}