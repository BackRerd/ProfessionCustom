package site.backrer.professioncustom.profession.event;

import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.profession.ItemProfessionConfigManager;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.profession.network.ModVariables;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = Professioncustom.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EquipmentEventHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        // 检查是否是玩家
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        
        // 确保在服务器端运行
        if (player.level().isClientSide) {
            return;
        }
        
        // 检查是否是护甲槽位的装备变化
        EquipmentSlot slot = event.getSlot();
        if (!isArmorSlot(slot)) {
            return;
        }
        
        // 获取新装备
        ItemStack newItem = event.getTo();
        if (newItem.isEmpty()) {
            return; // 如果是卸下装备，不需要检查
        }
        
        // 检查职业系统是否启用了职业限制
        boolean isSystemEnabled = ProfessionConfig.isProfessionSystemEnabled();
        boolean isRestrictionEnabled = ProfessionConfig.isProfessionRestrictionEnabled();
        
        if (isSystemEnabled && isRestrictionEnabled) {
            // 获取玩家的职业数据
            ModVariables.PlayerVariables variables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ModVariables.PlayerVariables());
            
            // 检查玩家是否有职业
            boolean hasProfession = variables.hasProfession();
            
            // 如果玩家没有职业，阻止穿戴任何装备
            if (!hasProfession) {
                // 立即取消装备穿戴
                ItemStack removedItem = player.getItemBySlot(slot);
                player.setItemSlot(slot, ItemStack.EMPTY);
                
                // 确保装备被正确返还给玩家或掉落
                ItemStack returnStack = removedItem.copy();
                if (!player.getInventory().add(returnStack)) {
                    player.drop(returnStack, false);
                }
                
                // 发送提示消息
                player.sendSystemMessage(Component.literal("你必须选择一个职业才能穿戴装备！"));
                return;
            }
            
            // 玩家有职业，继续检查装备限制
            // 获取装备的资源位置
            ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(newItem.getItem());
            if (itemLocation != null) {
                String itemId = itemLocation.toString();
                String playerProfession = variables.professionName;
                
                // 首先检查装备是否有职业限制配置
                List<String> requiredProfessions = ItemProfessionConfigManager.getProfessionsForItem(itemId);
                
                // 只有当装备有配置时，才进行职业匹配检查
                if (!requiredProfessions.isEmpty()) {
                    // 确保职业名称转换为小写进行比较
                    String playerProfessionLower = playerProfession.toLowerCase();
                    boolean canUse = requiredProfessions.contains(playerProfessionLower);
                    
                    if (!canUse) {
                        // 立即取消装备穿戴
                        ItemStack removedItem = player.getItemBySlot(slot);
                        player.setItemSlot(slot, ItemStack.EMPTY);
                        
                        // 确保装备被正确返还给玩家或掉落
                        ItemStack returnStack = removedItem.copy();
                        if (!player.getInventory().add(returnStack)) {
                            player.drop(returnStack, false);
                        }
                        
                        // 发送提示消息
                        player.sendSystemMessage(Component.literal("你的职业不允许穿戴这个装备！"));
                    }
                }
            }
        }
    }
    
    private static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD ||
                slot == EquipmentSlot.CHEST ||
                slot == EquipmentSlot.LEGS ||
                slot == EquipmentSlot.FEET;
    }
}
