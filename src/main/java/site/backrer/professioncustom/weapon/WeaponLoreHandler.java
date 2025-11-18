package site.backrer.professioncustom.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.client.ClientKeyMappings;

import java.util.List;
import java.util.Map;

/**
 * 武器Lore处理器
 * 负责在物品tooltip中显示武器信息（品质、等级、词条等）
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WeaponLoreHandler {
    
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltips = event.getToolTip();
        
        // 只显示Lore，不在这里初始化（初始化在服务器端完成）
        if (!WeaponNBTUtil.isWeapon(stack)) {
            return;
        }
        
        // 获取武器数据
        WeaponQuality quality = WeaponNBTUtil.getQuality(stack);
        int level = WeaponNBTUtil.getLevel(stack);
        int exp = WeaponNBTUtil.getExp(stack);
        int expRequired = WeaponLevelManager.getExpRequiredForLevel(level);
        Map<WeaponAttribute, Double> attributes = WeaponNBTUtil.getAttributes(stack);
        
        // 找到物品名称的位置（通常是第一个tooltip，且是粗体）
        // 在Minecraft中，tooltip的顺序是：名称 -> 自定义信息 -> 攻击力/耐久等
        // 我们需要在名称后立即插入，这样词条就会显示在名称下方，而不是攻击力描述下方
        int nameIndex = 0;
        for (int i = 0; i < tooltips.size(); i++) {
            Component component = tooltips.get(i);
            // 名称通常是第一个且是粗体
            if (component.getStyle().isBold() || i == 0) {
                nameIndex = i;
                break;
            }
        }
        
        int insertIndex = nameIndex + 1;
        
        // 添加空行（分隔符）
        tooltips.add(insertIndex++, Component.literal(""));
        
        // 添加品质显示
        Component qualityComponent = Component.literal("品质: ")
                .withStyle(ChatFormatting.GRAY)
                .append(quality.getDisplayComponent());
        tooltips.add(insertIndex++, qualityComponent);
        
        // 添加等级显示
        Component levelComponent = Component.literal("等级: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Lv." + level).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" (" + exp + "/" + expRequired + ")").withStyle(ChatFormatting.DARK_GRAY));
        tooltips.add(insertIndex++, levelComponent);

        // 在等级下方显示伤害/护甲加成（仅作为说明，不实际修改属性）
        double damageBonusPerLevel = WeaponConfig.getDamageBonusPerLevel();
        double armorBonusPerLevel = WeaponConfig.getArmorBonusPerLevel();
        if (level > 0 && (damageBonusPerLevel > 0 || armorBonusPerLevel > 0)) {
            if (stack.getItem() instanceof ArmorItem) {
                double totalArmorBonus = level * armorBonusPerLevel;
                Component armorBonusComponent = Component.literal("护甲加成: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("+" + String.format("%.1f", totalArmorBonus)).withStyle(ChatFormatting.BLUE));
                tooltips.add(insertIndex++, armorBonusComponent);
            } else {
                double totalDamageBonus = level * damageBonusPerLevel;
                Component damageBonusComponent = Component.literal("伤害加成: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("+" + String.format("%.1f", totalDamageBonus)).withStyle(ChatFormatting.RED));
                tooltips.add(insertIndex++, damageBonusComponent);
            }
        }
        
        // 如果有词条，添加词条显示
        if (!attributes.isEmpty()) {
            // 添加空行
            tooltips.add(insertIndex++, Component.literal(""));
            
            // 添加词条标题
            Component attributesTitle = Component.literal("词条:").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            tooltips.add(insertIndex++, attributesTitle);

            boolean showDescriptions = ClientKeyMappings.isShowLoreKeyDown();

            // 如果未按下详情键，添加提示行，并显示具体按键
            if (!showDescriptions) {
                Component keyName = ClientKeyMappings.getShowLoreKeyName();
                Component hint = Component.literal("  按 ")
                        .append(keyName.copy().withStyle(ChatFormatting.YELLOW))
                        .append(" 查看词条说明")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
                tooltips.add(insertIndex++, hint);
            }
            
            // 添加每个词条
            for (Map.Entry<WeaponAttribute, Double> entry : attributes.entrySet()) {
                WeaponAttribute attribute = entry.getKey();
                double value = entry.getValue();
                Component attributeComponent = Component.literal("  ")
                        .append(attribute.getDisplayWithValue(value));
                tooltips.add(insertIndex++, attributeComponent);

                // 按下 Shift 时，在每个词条下方添加说明
                if (showDescriptions) {
                    Component descriptionComponent = Component.literal("    ")
                            .append(attribute.getDescriptionComponent());
                    tooltips.add(insertIndex++, descriptionComponent);
                }
            }

            // 在词条后添加空行（与默认的tooltip分隔）
            tooltips.add(insertIndex, Component.literal(""));
        } else {
            // 没有词条也在末尾加一个空行，保持与原有逻辑接近
            tooltips.add(insertIndex, Component.literal(""));
        }
    }
}

