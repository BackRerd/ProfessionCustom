package site.backrer.professioncustom.armor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.Professioncustom;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class ArmorAttributeHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onLivingHurtDefence(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();

        if (!(victim instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        Map<ArmorAttribute, Double> totalAttributes = new HashMap<>();

        for (ItemStack armorStack : player.getInventory().armor) {
            if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem)) {
                continue;
            }
            if (!ArmorNBTUtil.isArmor(armorStack)) {
                continue;
            }

            Map<ArmorAttribute, Double> attrs = ArmorNBTUtil.getAttributes(armorStack);
            for (Map.Entry<ArmorAttribute, Double> e : attrs.entrySet()) {
                totalAttributes.merge(e.getKey(), e.getValue(), Double::sum);
            }

            int attackerLevel = 1;
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                attackerLevel = MobLevelManager.getEntityLevel(attacker);
            }

            int expGained = 5 + attackerLevel;
            ArmorLevelManager.addExp(armorStack, expGained);
        }

        if (totalAttributes.isEmpty()) {
            return;
        }

        float damage = event.getAmount();

        if (totalAttributes.containsKey(ArmorAttribute.DODGE)) {
            double dodgeRate = totalAttributes.get(ArmorAttribute.DODGE);
            if (RANDOM.nextDouble() * 100.0 < dodgeRate) {
                event.setCanceled(true);
                return;
            }
        }

        if (totalAttributes.containsKey(ArmorAttribute.SOUL_IMMUNE)) {
            if (!event.getSource().isIndirect()) {
                event.setCanceled(true);
                return;
            }
        }

        if (totalAttributes.containsKey(ArmorAttribute.HEAVY_ARMOR)) {
            double heavy = totalAttributes.get(ArmorAttribute.HEAVY_ARMOR);
            damage = (float) Math.max(0.0, damage - heavy);
        }

        if (totalAttributes.containsKey(ArmorAttribute.DAMAGE_LIMIT)) {
            double limit = totalAttributes.get(ArmorAttribute.DAMAGE_LIMIT);
            if (limit > 0) {
                damage = (float) Math.min(damage, limit);
            }
        }

        if (totalAttributes.containsKey(ArmorAttribute.FIXED_DAMAGE)) {
            double fixed = totalAttributes.get(ArmorAttribute.FIXED_DAMAGE);
            if (fixed > 0) {
                if (damage <= fixed * 8.0f) {
                    damage = (float) fixed;
                }
            }
        }

        if (totalAttributes.containsKey(ArmorAttribute.THORNS) && event.getSource().getEntity() instanceof LivingEntity attacker) {
            double thornsPercent = totalAttributes.get(ArmorAttribute.THORNS);
            if (thornsPercent > 0) {
                float reflect = (float) (damage * thornsPercent / 100.0);
                attacker.hurt(attacker.damageSources().thorns(player), reflect);
            }
        }

        if (totalAttributes.containsKey(ArmorAttribute.SLOW) && event.getSource().getEntity() instanceof LivingEntity attacker) {
            double slowChance = totalAttributes.get(ArmorAttribute.SLOW);
            if (RANDOM.nextDouble() * 100.0 < slowChance) {
                attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }
        }

        if (totalAttributes.containsKey(ArmorAttribute.STUN) && event.getSource().getEntity() instanceof LivingEntity attacker) {
            double stunChance = totalAttributes.get(ArmorAttribute.STUN);
            if (RANDOM.nextDouble() * 100.0 < stunChance) {
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                attacker.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
            }
        }

        if (totalAttributes.containsKey(ArmorAttribute.EXPLOSIVE) && victim.level() instanceof ServerLevel serverLevel) {
            double explosiveChance = totalAttributes.get(ArmorAttribute.EXPLOSIVE);
            if (RANDOM.nextDouble() * 100.0 < explosiveChance) {
                serverLevel.explode(null, victim.getX(), victim.getY(), victim.getZ(), 2.0f, Level.ExplosionInteraction.NONE);
            }
        }

        if (totalAttributes.containsKey(ArmorAttribute.SHOCK) && victim.level() instanceof ServerLevel serverLevel) {
            double shockChance = totalAttributes.get(ArmorAttribute.SHOCK);
            if (RANDOM.nextDouble() * 100.0 < shockChance) {
                double radius = 3.0;
                for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(radius))) {
                    if (nearby == victim) {
                        continue;
                    }
                    nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                    nearby.hurt(nearby.damageSources().mobAttack(victim), damage * 0.3f);
                }
            }
        }

        event.setAmount(damage);
    }

    /**
     * 玩家作为攻击者时，处理狂躁等进攻向护甲词条
     */
    @SubscribeEvent
    public static void onLivingHurtAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        double totalBerserk = 0.0;

        for (ItemStack armorStack : player.getInventory().armor) {
            if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem)) {
                continue;
            }
            if (!ArmorNBTUtil.isArmor(armorStack)) {
                continue;
            }

            Map<ArmorAttribute, Double> attrs = ArmorNBTUtil.getAttributes(armorStack);
            if (attrs.containsKey(ArmorAttribute.BERSERK)) {
                totalBerserk += attrs.get(ArmorAttribute.BERSERK);
            }
        }

        if (totalBerserk <= 0.0) {
            return;
        }

        // 狂躁：提升伤害并扣除自身生命
        float damage = event.getAmount();
        damage *= 2.0f; // 攻击力 x2
        event.setAmount(damage);

        // 每次攻击按狂躁总值的百分比扣除最大生命值（例如总值为1则扣1%最大生命）
        double percent = Math.min(100.0, totalBerserk);
        float selfDamage = player.getMaxHealth() * (float) (percent / 100.0);
        if (selfDamage > 0.0f) {
            player.hurt(player.damageSources().generic(), selfDamage);
        }
    }
}
