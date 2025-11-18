package site.backrer.professioncustom.profession;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static site.backrer.professioncustom.Professioncustom.MODID;

/**
 * 物品职业显示事件监听器
 * 负责在物品的tooltip中添加职业要求信息
 */
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItemProfessionLoreHandler {
    
    /**
     * 处理物品tooltip事件，添加职业要求信息
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltips = event.getToolTip();
        TooltipFlag flag = event.getFlags();
        
        // 获取物品的资源位置
        ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemLocation == null) {
            return;
        }
        
        // 构建物品ID字符串 (modid:itemname)
        String itemId = itemLocation.toString();
        
        // 获取该物品的职业配置
        ItemProfessionConfigManager.ItemProfessionData config = ItemProfessionConfigManager.getItemConfig(itemId);
        if (config == null || !config.showInLore) {
            return;
        }
        
        List<String> professions = ItemProfessionConfigManager.getProfessionsForItem(itemId);
        if (professions.isEmpty()) {
            return;
        }
        
        // 获取格式化的颜色
        ChatFormatting color = getChatFormattingByName(config.loreColor);
        
        // 添加描述前缀
        String prefix = config.descriptionPrefix != null ? config.descriptionPrefix : "需要职业: ";
        
        // 构建职业名称列表字符串
        StringBuilder professionNames = new StringBuilder();
        for (int i = 0; i < professions.size(); i++) {
            // 获取职业显示名称
            String professionName = professions.get(i);
            Profession profession = ProfessionManager.getProfessionByName(professionName);
            String displayName = profession != null ? profession.getDisplayName() : professionName;
            
            professionNames.append(displayName);
            if (i < professions.size() - 1) {
                professionNames.append("、");
            }
        }
        
        // 创建并添加tooltip组件
        Component tooltipComponent = Component.literal(prefix + professionNames.toString())
                .withStyle(color);
        
        tooltips.add(tooltipComponent);
    }
    
    /**
     * 根据颜色名称获取对应的ChatFormatting枚举值
     * @param colorName 颜色名称字符串
     * @return ChatFormatting枚举值，如果未找到则返回默认颜色
     */
    private static ChatFormatting getChatFormattingByName(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return ChatFormatting.AQUA; // 默认颜色
        }
        
        try {
            return ChatFormatting.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 处理一些常用的颜色别名
            switch (colorName.toLowerCase()) {
                case "black": return ChatFormatting.BLACK;
                case "dark_blue": return ChatFormatting.DARK_BLUE;
                case "dark_green": return ChatFormatting.DARK_GREEN;
                case "dark_aqua": return ChatFormatting.DARK_AQUA;
                case "dark_red": return ChatFormatting.DARK_RED;
                case "dark_purple": return ChatFormatting.DARK_PURPLE;
                case "gold": return ChatFormatting.GOLD;
                case "gray": return ChatFormatting.GRAY;
                case "dark_gray": return ChatFormatting.DARK_GRAY;
                case "blue": return ChatFormatting.BLUE;
                case "green": return ChatFormatting.GREEN;
                case "aqua": return ChatFormatting.AQUA;
                case "red": return ChatFormatting.RED;
                case "light_purple": return ChatFormatting.LIGHT_PURPLE;
                case "yellow": return ChatFormatting.YELLOW;
                case "white": return ChatFormatting.WHITE;
                case "bold": return ChatFormatting.BOLD;
                case "italic": return ChatFormatting.ITALIC;
                case "underline": return ChatFormatting.UNDERLINE;
                case "strikethrough": return ChatFormatting.STRIKETHROUGH;
                case "obfuscated": return ChatFormatting.OBFUSCATED;
                default: return ChatFormatting.AQUA; // 默认颜色
            }
        }
    }
}
