package site.backrer.professioncustom.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.Professioncustom;

/**
 * 生物名称标签处理器，负责修改生物的显示名称以包含等级信息
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class MobNameTagHandler {
    
    /**
     * 当生物每次更新时，检查并修改其名称标签
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!ProfessionConfig.isMobLevelingEnabled() || !ProfessionConfig.isShowLevelNameTag()) return;
        
        LivingEntity entity = event.getEntity();
        if (ProfessionConfig.isMobExcluded(entity)) return;
        
        // 只处理攻击性生物
        if (!entity.isAttackable()) return;
        
        int level = MobLevelManager.getEntityLevel(entity);
        
        // 获取原始名称（不包含自定义标签时的名称）
        String originalName = entity.getName().getString();
        
        // 检查名称是否已经包含等级信息，避免重复添加
        if (!originalName.contains("[Lv.")) {
            // 创建新的名称：[Lv.X] 原始名称
            String newName = "[Lv." + level + "] " + originalName;
            // 设置自定义名称
            entity.setCustomName(net.minecraft.network.chat.Component.literal(newName));
            entity.setCustomNameVisible(true);
        }
    }
}