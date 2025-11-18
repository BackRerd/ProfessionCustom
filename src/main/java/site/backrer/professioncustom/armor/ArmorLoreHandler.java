package site.backrer.professioncustom.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.client.ClientKeyMappings;
import site.backrer.professioncustom.weapon.WeaponConfig;
import site.backrer.professioncustom.weapon.WeaponQuality;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Professioncustom.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ArmorLoreHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltips = event.getToolTip();

        if (!(stack.getItem() instanceof ArmorItem)) {
            return;
        }

        if (!ArmorNBTUtil.isArmor(stack)) {
            return;
        }

        WeaponQuality quality = ArmorNBTUtil.getQuality(stack);
        int level = ArmorNBTUtil.getLevel(stack);
        int exp = ArmorNBTUtil.getExp(stack);
        int expRequired = ArmorLevelManager.getExpRequiredForLevel(level);
        Map<ArmorAttribute, Double> attributes = ArmorNBTUtil.getAttributes(stack);

        int nameIndex = 0;
        for (int i = 0; i < tooltips.size(); i++) {
            Component component = tooltips.get(i);
            if (component.getStyle().isBold() || i == 0) {
                nameIndex = i;
                break;
            }
        }

        int insertIndex = nameIndex + 1;

        tooltips.add(insertIndex++, Component.literal(""));

        Component qualityComponent = Component.literal("品质: ")
                .withStyle(ChatFormatting.GRAY)
                .append(quality.getDisplayComponent());
        tooltips.add(insertIndex++, qualityComponent);

        Component levelComponent = Component.literal("等级: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("Lv." + level).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" (" + exp + "/" + expRequired + ")").withStyle(ChatFormatting.DARK_GRAY));
        tooltips.add(insertIndex++, levelComponent);

        double armorBonusPerLevel = WeaponConfig.getArmorBonusPerLevel();
        if (level > 0 && armorBonusPerLevel > 0) {
            double totalArmorBonus = level * armorBonusPerLevel;
            Component armorBonusComponent = Component.literal("护甲加成: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("+" + String.format("%.1f", totalArmorBonus)).withStyle(ChatFormatting.BLUE));
            tooltips.add(insertIndex++, armorBonusComponent);
        }

        if (!attributes.isEmpty()) {
            tooltips.add(insertIndex++, Component.literal(""));

            Component attributesTitle = Component.literal("护甲词条:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
            tooltips.add(insertIndex++, attributesTitle);

            boolean showDescriptions = ClientKeyMappings.isShowLoreKeyDown();

            if (!showDescriptions) {
                Component keyName = ClientKeyMappings.getShowLoreKeyName();
                Component hint = Component.literal("  按 ")
                        .append(keyName.copy().withStyle(ChatFormatting.YELLOW))
                        .append(" 查看护甲词条说明")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
                tooltips.add(insertIndex++, hint);
            }

            for (Map.Entry<ArmorAttribute, Double> entry : attributes.entrySet()) {
                ArmorAttribute attribute = entry.getKey();
                double value = entry.getValue();
                Component attributeComponent = Component.literal("  ")
                        .append(attribute.getDisplayWithValue(value));
                tooltips.add(insertIndex++, attributeComponent);

                if (showDescriptions) {
                    Component descriptionComponent = Component.literal("    ")
                            .append(attribute.getDescriptionComponent());
                    tooltips.add(insertIndex++, descriptionComponent);
                }
            }

            tooltips.add(insertIndex, Component.literal(""));
        } else {
            tooltips.add(insertIndex, Component.literal(""));
        }
    }
}
