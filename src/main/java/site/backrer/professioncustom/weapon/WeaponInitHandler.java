package site.backrer.professioncustom.weapon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.armor.ArmorNBTUtil;
import site.backrer.professioncustom.armor.ArmorTagManager;

import java.util.Random;

/**
 * 武器初始化处理器
 * 在服务器端检测玩家首次拿起武器并初始化
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class WeaponInitHandler {
    private static final Random RANDOM = new Random();
    
    /**
     * 处理玩家tick事件 - 检测玩家手中的武器并初始化
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }

        EquipmentSlot slot = event.getSlot();
        ItemStack newStack = event.getTo();

        if (newStack.isEmpty()) {
            return;
        }

        boolean isConfiguredWeaponItem = WeaponItemConfigManager.isWeaponItem(newStack);

        if (!newStack.isDamageableItem() && !isConfiguredWeaponItem) {
            return;
        }

        if (slot == EquipmentSlot.MAINHAND) {
            if (newStack.getItem() instanceof ArmorItem) {
                // 主手拿的是护甲，初始化护甲词条
                initializeArmorIfNeeded(newStack);
            } else {
                // 主手拿的是其他可损坏物品，按武器处理
                initializeWeaponIfNeeded(newStack);
            }
        } else if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            // 护甲槽位装备变化时，初始化护甲词条
            initializeArmorIfNeeded(newStack);
        }
    }
    
    /**
     * 如果需要，初始化武器
     */
    private static void initializeWeaponIfNeeded(ItemStack stack) {
        // 如果已经是武器，检查是否需要加载Lore
        if (WeaponNBTUtil.isWeapon(stack)) {
            if (!WeaponNBTUtil.isLoreLoaded(stack)) {
                WeaponQuality quality = WeaponNBTUtil.getQuality(stack);
                if (quality == WeaponQuality.COMMON && WeaponNBTUtil.getLevel(stack) == 0) {
                    quality = generateRandomQuality();
                    WeaponNBTUtil.initializeWeapon(stack, quality);
                }
                WeaponTagManager.initializeAttributes(stack, WeaponNBTUtil.getQuality(stack));
                WeaponNBTUtil.setLoreLoaded(stack, true);
            }
        } else {
            // 如果不是武器，仅在主手情况下初始化为武器
            WeaponQuality quality = generateRandomQuality();
            WeaponNBTUtil.initializeWeapon(stack, quality);
            WeaponTagManager.initializeAttributes(stack, quality);
            WeaponNBTUtil.setLoreLoaded(stack, true);
        }
    }

    private static void initializeArmorIfNeeded(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem && stack.isDamageableItem()) {
            if (ArmorNBTUtil.isArmor(stack)) {
                return;
            }
            WeaponQuality quality = generateRandomQuality();
            ArmorNBTUtil.initializeArmor(stack, quality);
            ArmorTagManager.initializeAttributes(stack, quality);
        }
    }
    
    /**
     * 生成随机品质
     */
    private static WeaponQuality generateRandomQuality() {
        double rand = RANDOM.nextDouble();
        if (rand < 0.5) {
            return WeaponQuality.COMMON;
        } else if (rand < 0.75) {
            return WeaponQuality.UNCOMMON;
        } else if (rand < 0.9) {
            return WeaponQuality.RARE;
        } else if (rand < 0.97) {
            return WeaponQuality.EPIC;
        } else if (rand < 0.995) {
            return WeaponQuality.LEGENDARY;
        } else {
            return WeaponQuality.MYTHIC;
        }
    }
}

