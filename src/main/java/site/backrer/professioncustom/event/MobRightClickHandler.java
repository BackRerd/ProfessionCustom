package site.backrer.professioncustom.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.Professioncustom;

import java.util.List;
import java.util.UUID;

/**
 * 生物右键点击处理器，负责在玩家右键点击生物时显示NBT标签信息
 */
//@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class MobRightClickHandler {

    /**
     * 当玩家右键点击实体时触发
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        
        // 只处理生物实体
        if (target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) target;
            
            // 调用NBT标签显示功能
            showEntityNBT(livingEntity, player);
        }
    }
    
    /**
     * 显示实体的NBT标签信息
     */
    private static void showEntityNBT(LivingEntity entity, Player player) {
        try {
            // 创建一个新的CompoundTag来存储实体的NBT数据
            CompoundTag compoundTag = new CompoundTag();
            
            // 将实体数据保存到NBT中
            entity.save(compoundTag);
            
            // 发送标题消息给玩家
            player.sendSystemMessage(
                Component.literal("===== 实体NBT信息 ====").setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#00FF00"))
                )
            );
            
            // 显示实体名称
            player.sendSystemMessage(
                Component.literal("实体: " + entity.getName().getString()).setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#FFFF00"))
                )
            );
            
            // 显示主要的NBT标签
            displayNBTTags(compoundTag, entity, player);
            
            // 显示我们自定义的等级标签
            player.sendSystemMessage(
                Component.literal("\n===== 自定义标签 ====").setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#00FF00"))
                )
            );
            
            for (String tag : entity.getTags()) {
                player.sendSystemMessage(
                    Component.literal("- " + tag).setStyle(
                        Style.EMPTY.withColor(TextColor.parseColor("#FFFFFF"))
                    )
                );
            }
            
            // 显示结束消息
            player.sendSystemMessage(
                Component.literal("======================").setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#00FF00"))
                )
            );
            
        } catch (Exception e) {
            // 处理可能的错误
            player.sendSystemMessage(
                Component.literal("无法读取实体NBT数据: " + e.getMessage()).setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#FF0000"))
                )
            );
            Professioncustom.LOGGER.error("Error reading entity NBT: {}", e.getMessage());
        }
    }
    
    /**
     * 显示NBT标签的主要内容
     */
    private static void displayNBTTags(CompoundTag compoundTag, LivingEntity entity, Player player) {
        // 显示一些重要的NBT标签
        if (compoundTag.contains("Health")) {
            float health = compoundTag.getFloat("Health");
            player.sendSystemMessage(
                Component.literal("生命值: " + health).setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#FF5555"))
                )
            );
        }
        
        if (compoundTag.contains("Attributes")) {
            player.sendSystemMessage(
                Component.literal("包含属性数据").setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#AAAAAA"))
                )
            );
        }
        
            // 显示UUID
        if (compoundTag.contains("UUIDMost") && compoundTag.contains("UUIDLeast")) {
            long uuidMost = compoundTag.getLong("UUIDMost");
            long uuidLeast = compoundTag.getLong("UUIDLeast");
            player.sendSystemMessage(
                Component.literal("UUID: " + new UUID(uuidLeast, uuidMost)).setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#AAAAAA"))
                )
            );
        }
        
        // 列出所有顶层标签键，限制数量以避免信息过多
        List<String> keys = compoundTag.getAllKeys().stream().limit(10).toList();
        player.sendSystemMessage(
            Component.literal("顶层标签数量: " + compoundTag.getAllKeys().size()).setStyle(
                Style.EMPTY.withColor(TextColor.parseColor("#AAAAAA"))
            )
        );
        
        for (String key : keys) {
            player.sendSystemMessage(
                Component.literal("- " + key + ": " + getTagTypeDescription(compoundTag, key)).setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#FFFFFF"))
                )
            );
        }
        
        if (compoundTag.getAllKeys().size() > 10) {
            player.sendSystemMessage(
                Component.literal("... 更多标签未显示 (总共 " + compoundTag.getAllKeys().size() + ")").setStyle(
                    Style.EMPTY.withColor(TextColor.parseColor("#AAAAAA"))
                )
            );
        }
    }
    
    /**
     * 获取NBT标签类型的描述
     */
    private static String getTagTypeDescription(CompoundTag compoundTag, String key) {
        if (compoundTag.contains(key, CompoundTag.TAG_COMPOUND)) {
            return "复合标签";
        } else if (compoundTag.contains(key, CompoundTag.TAG_LIST)) {
            return "列表标签";
        } else if (compoundTag.contains(key, CompoundTag.TAG_STRING)) {
            return "字符串: " + compoundTag.getString(key);
        } else if (compoundTag.contains(key, CompoundTag.TAG_INT)) {
            return "整数: " + compoundTag.getInt(key);
        } else if (compoundTag.contains(key, CompoundTag.TAG_FLOAT)) {
            return "浮点数: " + compoundTag.getFloat(key);
        } else if (compoundTag.contains(key, CompoundTag.TAG_DOUBLE)) {
            return "双精度: " + compoundTag.getDouble(key);
        } else {
            return "未知类型";
        }
    }
}