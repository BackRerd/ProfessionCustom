package site.backrer.professioncustom.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.level.MobAttributeCalculator;
import site.backrer.professioncustom.profession.ProfessionConfig;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

/**
 * 生物等级属性监听器 - 负责在适当的时机为生物应用等级相关的属性修改
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobLevelAttributeListener {
    
    /**
     * 手动为生物设置等级并立即应用属性变化
     * 可以从其他地方调用，比如当生物等级提升时
     */
    public static void setEntityLevelAndApplyAttributes(LivingEntity entity, int level) {
        if (entity instanceof Player) {
            Professioncustom.LOGGER.debug("Skipping level setting for player entity: {}", entity.getClass().getSimpleName());
            return;
        }
        if (ProfessionConfig.isMobExcluded(entity)) {
            return;
        }
        
        // 使用反射方式设置等级，因为MobLevelManager中没有直接的setter方法
        try {
            Field entityLevelsField = MobLevelManager.class.getDeclaredField("entityLevels");
            entityLevelsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Integer> entityLevels = (Map<UUID, Integer>) entityLevelsField.get(null);
            entityLevels.put(entity.getUUID(), level);
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to set entity level: {}", e.getMessage());
        }
        
        MobAttributeCalculator.applyLevelAttributes(entity);
    }
}