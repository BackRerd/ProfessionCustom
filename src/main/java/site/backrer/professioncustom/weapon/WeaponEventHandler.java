package site.backrer.professioncustom.weapon;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.Professioncustom;

/**
 * 武器事件处理器
 * 处理武器击杀怪物、受到攻击等事件
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class WeaponEventHandler {
    
    /**
     * 处理生物死亡事件 - 武器击杀怪物获得经验
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 排除玩家
        if (entity instanceof Player) {
            return;
        }
        
        // 获取攻击者
        if (event.getSource().getEntity() instanceof Player player) {
            if (player.level().isClientSide) {
                return;
            }
            
            ItemStack weapon = player.getMainHandItem();
            if (!WeaponNBTUtil.isWeapon(weapon)) {
                return;
            }
            
            // 获取怪物等级
            int mobLevel = MobLevelManager.getEntityLevel(entity);
            
            // 计算获得的经验值
            int expGained = WeaponLevelManager.getExpFromKill(mobLevel);
            
            // 增加武器经验
            boolean leveledUp = WeaponLevelManager.addExp(weapon, expGained);
            
            // 如果升级了，更新词条数值
            if (leveledUp) {
                WeaponTagManager.updateAttributesOnLevelUp(weapon);
            }
        }
    }
    
}

