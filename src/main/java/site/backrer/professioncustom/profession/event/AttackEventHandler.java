package site.backrer.professioncustom.profession.event;

import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.profession.ItemProfessionConfigManager;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.profession.network.ModVariables;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(modid = Professioncustom.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttackEventHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // 检查伤害来源是否是玩家
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker instanceof ServerPlayer player) {
                if (!attacker.level().isClientSide) {
                    // 检查职业系统是否启用了职业限制
                    boolean isSystemEnabled = ProfessionConfig.isProfessionSystemEnabled();
                    boolean isRestrictionEnabled = ProfessionConfig.isProfessionRestrictionEnabled();
                    
                    if (isSystemEnabled && isRestrictionEnabled) {
                        // 获取玩家的职业数据
                        ModVariables.PlayerVariables variables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ModVariables.PlayerVariables());
                        
                        // 检查玩家是否有职业
                        boolean hasProfession = variables.hasProfession();
                        
                        // 获取玩家手持的物品
                        ItemStack heldItem = player.getMainHandItem();
                        boolean hasItem = !heldItem.isEmpty();
                        
                        // 如果玩家没有职业但手持物品
                        if (!hasProfession && hasItem) {
                            // 取消伤害
                            event.setCanceled(true);
                            // 发送提示消息
                            player.sendSystemMessage(Component.literal("你必须选择一个职业才能使用武器！"));
                            return;
                        }
                        
                        // 如果玩家有职业并且手持物品
                        if (hasProfession && hasItem) {
                            // 获取物品的资源位置
                            ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(heldItem.getItem());
                            if (itemLocation != null) {
                                String itemId = itemLocation.toString();
                                String playerProfession = variables.professionName;
                                 
                                // 首先检查物品是否有职业限制配置
                                List<String> requiredProfessions = ItemProfessionConfigManager.getProfessionsForItem(itemId);
                                 
                                // 只有当物品有配置时，才进行职业匹配检查
                                if (!requiredProfessions.isEmpty()) {
                                    // 确保职业名称转换为小写进行比较
                                    String playerProfessionLower = playerProfession.toLowerCase();
                                    boolean canUse = requiredProfessions.contains(playerProfessionLower);
                                     
                                    if (!canUse) {
                                        // 取消伤害
                                        event.setCanceled(true);
                                        // 发送提示消息
                                        player.sendSystemMessage(Component.literal("你的职业不允许使用这个武器！"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
