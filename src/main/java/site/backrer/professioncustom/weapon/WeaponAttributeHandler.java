package site.backrer.professioncustom.weapon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.Professioncustom;

import java.util.Map;
import java.util.Random;

/**
 * 武器词条效果处理器
 * 实现各种词条的实际功能
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class WeaponAttributeHandler {
    private static final Random RANDOM = new Random();
    
    /**
     * 处理生物受伤事件 - 应用武器词条效果
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        
        // 获取攻击者
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }
        
        if (attacker.level().isClientSide) {
            return;
        }
        
        ItemStack weapon = attacker.getMainHandItem();
        if (!WeaponNBTUtil.isWeapon(weapon)) {
            return;
        }
        
        Map<WeaponAttribute, Double> attributes = WeaponNBTUtil.getAttributes(weapon);
        if (attributes.isEmpty()) {
            return;
        }
        
        float originalDamage = event.getAmount();

        // 基础伤害：原始伤害 + 等级伤害加成 + 攻击力加成(唯一的绝对值加成)
        float baseDamage = originalDamage;

        // 等级伤害加成（只要是本模组武器都生效，与词条无关）
        int weaponLevel = WeaponNBTUtil.getLevel(weapon);
        if (weaponLevel > 0) {
            double damageBonusPerLevel = WeaponConfig.getDamageBonusPerLevel();
            if (damageBonusPerLevel > 0) {
                baseDamage += (float) (weaponLevel * damageBonusPerLevel);
            }
        }

        if (attributes.containsKey(WeaponAttribute.ATTACK_DAMAGE)) {
            double attackBonus = attributes.get(WeaponAttribute.ATTACK_DAMAGE);
            baseDamage += (float) attackBonus;
        }

        // 叠加各种百分比加成(元素、灵魂等)，以倍率形式作用在基础伤害上
        double percentBonus = 0.0;

        // 处理元素伤害：视为额外伤害百分比
        if (attributes.containsKey(WeaponAttribute.FIRE_DAMAGE)) {
            double firePercent = attributes.get(WeaponAttribute.FIRE_DAMAGE); // 视为百分比
            percentBonus += firePercent;
            // 点燃目标
            victim.setSecondsOnFire(3);
        }

        if (attributes.containsKey(WeaponAttribute.ICE_DAMAGE)) {
            double icePercent = attributes.get(WeaponAttribute.ICE_DAMAGE); // 视为百分比
            percentBonus += icePercent;
            // 添加缓慢效果
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }

        if (attributes.containsKey(WeaponAttribute.SOUL_DAMAGE)) {
            double soulPercent = attributes.get(WeaponAttribute.SOUL_DAMAGE); // 视为百分比
            percentBonus += soulPercent;
            // 添加凋零效果
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        }

        float finalDamage = (float) (baseDamage * (1.0 + percentBonus / 100.0));

        // 处理暴击
        boolean isCritical = false;
        if (attributes.containsKey(WeaponAttribute.CRITICAL_RATE)) {
            double critRate = attributes.get(WeaponAttribute.CRITICAL_RATE); // 百分比
            if (RANDOM.nextDouble() * 100 < critRate) {
                isCritical = true;
                if (attributes.containsKey(WeaponAttribute.CRITICAL_DAMAGE)) {
                    double critDamagePercent = attributes.get(WeaponAttribute.CRITICAL_DAMAGE); // 百分比
                    finalDamage *= (1.0 + critDamagePercent / 100.0);
                } else {
                    finalDamage *= 1.5; // 默认暴击倍率
                }
            }
        }
        
        // 处理光爆
        if (attributes.containsKey(WeaponAttribute.LIGHTBURST)) {
            // 添加失明效果
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
        }
        
        // 处理秒伤（持续伤害）
        if (attributes.containsKey(WeaponAttribute.DAMAGE_PER_SECOND)) {
            double dpsPercent = attributes.get(WeaponAttribute.DAMAGE_PER_SECOND); // 视为秒伤百分比
            // 使用百分比近似为凋零等级
            int amplifier = (int) Math.max(0, dpsPercent / 20.0); // 每 20% 提升一级
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, amplifier));
        }
        
        // 处理吸血
        if (attributes.containsKey(WeaponAttribute.LIFESTEAL_RATE)) {
            double lifestealRate = attributes.get(WeaponAttribute.LIFESTEAL_RATE);
            if (RANDOM.nextDouble() * 100 < lifestealRate) {
                double lifestealAmount = finalDamage;
                
                // 应用吸血倍率
                if (attributes.containsKey(WeaponAttribute.LIFESTEAL_MULTIPLIER)) {
                    double lifestealMultiplier = attributes.get(WeaponAttribute.LIFESTEAL_MULTIPLIER);
                    lifestealAmount *= lifestealMultiplier;
                }
                
                // 恢复生命值
                float healthToHeal = (float) (lifestealAmount * 0.1); // 10%的伤害转化为生命值
                attacker.heal(healthToHeal);
            }
        }

        // 应用最终伤害
        event.setAmount(finalDamage);
        
        // 如果是暴击，添加粒子效果（可选）
        if (isCritical && attacker instanceof ServerPlayer) {
            // 可以在这里添加暴击粒子效果
        }
    }
}

