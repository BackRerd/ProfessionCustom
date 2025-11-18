package site.backrer.professioncustom.armor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.Professioncustom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class ArmorTickHandler {

    private static final Map<UUID, Integer> TICK_COUNTER = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        UUID id = player.getUUID();
        int tick = TICK_COUNTER.getOrDefault(id, 0) + 1;
        if (tick >= 20) {
            tick = 0;
        }
        TICK_COUNTER.put(id, tick);

        double totalRegen = 0.0;
        double totalSpeed = 0.0;
        double totalRepair = 0.0;

        for (ItemStack armorStack : player.getInventory().armor) {
            if (armorStack.isEmpty() || !(armorStack.getItem() instanceof ArmorItem)) {
                continue;
            }
            if (!ArmorNBTUtil.isArmor(armorStack)) {
                continue;
            }

            Map<ArmorAttribute, Double> attrs = ArmorNBTUtil.getAttributes(armorStack);
            if (attrs.containsKey(ArmorAttribute.REGEN)) {
                totalRegen += attrs.get(ArmorAttribute.REGEN);
            }
            if (attrs.containsKey(ArmorAttribute.SPEED)) {
                totalSpeed += attrs.get(ArmorAttribute.SPEED);
            }
            if (attrs.containsKey(ArmorAttribute.REPAIR)) {
                totalRepair += attrs.get(ArmorAttribute.REPAIR);
            }
        }

        // 每 tick 持续刷新速度效果（持续时间略长于刷新间隔）
        if (totalSpeed > 0.0) {
            int amplifier = (int) Math.max(0, totalSpeed * 5); // 数值越大速度等级越高
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, amplifier));
        }

        // 每秒执行一次：回血与修复
        if (tick == 0) {
            if (totalRegen > 0.0) {
                float heal = (float) totalRegen;
                if (heal > 0.0f) {
                    player.heal(heal);
                }
            }

            if (totalRepair > 0.0) {
                int repairAmount = Math.max(1, (int) totalRepair);
                for (ItemStack armorStack : player.getInventory().armor) {
                    if (armorStack.isEmpty() || !armorStack.isDamageableItem()) {
                        continue;
                    }
                    int currentDamage = armorStack.getDamageValue();
                    if (currentDamage > 0) {
                        int newDamage = Math.max(0, currentDamage - repairAmount);
                        armorStack.setDamageValue(newDamage);
                    }
                }
            }
        }
    }
}
