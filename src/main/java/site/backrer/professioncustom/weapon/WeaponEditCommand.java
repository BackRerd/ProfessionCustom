package site.backrer.professioncustom.weapon;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import site.backrer.professioncustom.armor.ArmorAttribute;
import site.backrer.professioncustom.armor.ArmorLevelManager;
import site.backrer.professioncustom.armor.ArmorNBTUtil;
import site.backrer.professioncustom.armor.ArmorTagManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static site.backrer.professioncustom.armor.ArmorNBTUtil.isArmor;
import static site.backrer.professioncustom.weapon.WeaponNBTUtil.isWeapon;

/**
 * 武器编辑指令
 * 用于修改手中物品的等级、经验、品质以及词条数量/数值
 */
public class WeaponEditCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("weaponedit")
                .requires(source -> source.hasPermission(2))
                .executes(WeaponEditCommand::showHelp)
                // 设置等级
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(WeaponEditCommand::setLevel)))
                // 设置当前经验
                .then(Commands.literal("setexp")
                        .then(Commands.argument("exp", IntegerArgumentType.integer(0))
                                .executes(WeaponEditCommand::setExp)))
                // 增加经验（会自动判定升级）
                .then(Commands.literal("addexp")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(WeaponEditCommand::addExp)))
                // 设置品质
                .then(Commands.literal("setquality")
                        .then(Commands.argument("quality", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(WeaponQuality.values()).map(q -> q.name().toLowerCase(Locale.ROOT)),
                                        builder
                                ))
                                .executes(WeaponEditCommand::setQuality)))
                // 重设随机词条并指定数量
                .then(Commands.literal("setattrcount")
                        .then(Commands.argument("count", IntegerArgumentType.integer(0, 10))
                                .executes(WeaponEditCommand::setAttributeCount)))
                // 随机新增 N 条词条（不会清空已有）
                .then(Commands.literal("addattr")
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                                .executes(WeaponEditCommand::addRandomAttributes)))
                // 设置单个词条数值（武器/护甲通用）
                .then(Commands.literal("setattr")
                        .then(Commands.argument("attribute", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    // 补全里同时给出武器词条和护甲词条的枚举名（小写）
                                    List<String> names = new ArrayList<>();
                                    for (WeaponAttribute attr : WeaponAttribute.values()) {
                                        names.add(attr.name().toLowerCase(Locale.ROOT));
                                    }
                                    for (ArmorAttribute attr : ArmorAttribute.values()) {
                                        names.add(attr.name().toLowerCase(Locale.ROOT));
                                    }
                                    return SharedSuggestionProvider.suggest(names, builder);
                                })
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                        .executes(WeaponEditCommand::setAttribute))))
                // 删除指定词条（武器/护甲通用）
                .then(Commands.literal("delattr")
                        .then(Commands.argument("attribute", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    List<String> names = new ArrayList<>();
                                    for (WeaponAttribute attr : WeaponAttribute.values()) {
                                        names.add(attr.name().toLowerCase(Locale.ROOT));
                                    }
                                    for (ArmorAttribute attr : ArmorAttribute.values()) {
                                        names.add(attr.name().toLowerCase(Locale.ROOT));
                                    }
                                    return SharedSuggestionProvider.suggest(names, builder);
                                })
                                .executes(WeaponEditCommand::deleteAttribute)))
                .then(Commands.literal("help").executes(WeaponEditCommand::showHelp))
        );
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();

        player.sendSystemMessage(Component.literal("===== /weaponedit 帮助 ====="));
        player.sendSystemMessage(Component.literal("/weaponedit setlevel <等级> - 设置手中武器/护甲的等级"));
        player.sendSystemMessage(Component.literal("/weaponedit setexp <经验值> - 设置手中武器/护甲当前经验(不自动升级)"));
        player.sendSystemMessage(Component.literal("/weaponedit addexp <数值> - 为手中武器/护甲增加经验并自动判定升级"));
        player.sendSystemMessage(Component.literal("/weaponedit setquality <品质> - 设置品质 (common/uncommon/rare/epic/legendary/mythic)"));
        player.sendSystemMessage(Component.literal("/weaponedit setattrcount <数量> - 重新随机生成并限制当前词条数量"));
        player.sendSystemMessage(Component.literal("/weaponedit addattr <数量> - 在不清空原有词条的前提下随机新增若干词条"));
        player.sendSystemMessage(Component.literal("/weaponedit setattr <词条名> <数值> - 设置指定词条数值 (武器/护甲)"));
        player.sendSystemMessage(Component.literal("/weaponedit delattr <词条名> - 删除指定词条 (武器/护甲)"));
        player.sendSystemMessage(Component.literal("============================"));

        return 1;
    }

    private static ItemStack getHeldItem(Player player) {
        return player.getMainHandItem();
    }

    private static int setLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        int level = IntegerArgumentType.getInteger(context, "level");

        ItemStack stack = getHeldItem(player);
        if (!stack.isDamageableItem()) {
            player.sendSystemMessage(Component.literal("请手持一个可作为武器或护甲的物品"));
            return 0;
        }

        if (isWeapon(stack)) {
            WeaponNBTUtil.setLevel(stack, level);
            WeaponTagManager.updateAttributesOnLevelUp(stack);
            player.sendSystemMessage(Component.literal("已将武器等级设置为: " + level));
        } else if (isArmor(stack)) {
            ArmorNBTUtil.setLevel(stack, level);
            player.sendSystemMessage(Component.literal("已将护甲等级设置为: " + level));
        } else {
            player.sendSystemMessage(Component.literal("该物品尚未初始化为武器或护甲"));
        }
        return 1;
    }

    private static int setExp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        int exp = IntegerArgumentType.getInteger(context, "exp");

        ItemStack stack = getHeldItem(player);
        if (!stack.isDamageableItem()) {
            player.sendSystemMessage(Component.literal("请手持一个可作为武器或护甲的物品"));
            return 0;
        }
        if (isWeapon(stack)) {
            WeaponNBTUtil.setExp(stack, exp);
            int level = WeaponNBTUtil.getLevel(stack);
            int required = WeaponLevelManager.getExpRequiredForLevel(level);
            player.sendSystemMessage(Component.literal("已将当前武器经验设置为: " + exp + " / " + required));
        } else if (isArmor(stack)) {
            ArmorNBTUtil.setExp(stack, exp);
            int level = ArmorNBTUtil.getLevel(stack);
            int required = ArmorLevelManager.getExpRequiredForLevel(level);
            player.sendSystemMessage(Component.literal("已将当前护甲经验设置为: " + exp + " / " + required));
        } else {
            player.sendSystemMessage(Component.literal("该物品尚未初始化为武器或护甲"));
        }
        return 1;
    }

    private static int addExp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(context, "amount");

        ItemStack stack = getHeldItem(player);
        if (!stack.isDamageableItem()) {
            player.sendSystemMessage(Component.literal("请手持一个可作为武器或护甲的物品"));
            return 0;
        }
        if (isWeapon(stack)) {
            boolean leveledUp = WeaponLevelManager.addExp(stack, amount);
            int level = WeaponNBTUtil.getLevel(stack);
            int exp = WeaponNBTUtil.getExp(stack);
            int required = WeaponLevelManager.getExpRequiredForLevel(level);

            if (leveledUp) {
                player.sendSystemMessage(Component.literal("武器获得 " + amount + " 经验并升级至 Lv." + level + "，当前经验: " + exp + "/" + required));
            } else {
                player.sendSystemMessage(Component.literal("武器获得 " + amount + " 经验，当前 Lv." + level + "，经验: " + exp + "/" + required));
            }
        } else if (isArmor(stack)) {
            boolean leveledUp = ArmorLevelManager.addExp(stack, amount);
            int level = ArmorNBTUtil.getLevel(stack);
            int exp = ArmorNBTUtil.getExp(stack);
            int required = ArmorLevelManager.getExpRequiredForLevel(level);

            if (leveledUp) {
                player.sendSystemMessage(Component.literal("护甲获得 " + amount + " 经验并升级至 Lv." + level + "，当前经验: " + exp + "/" + required));
            } else {
                player.sendSystemMessage(Component.literal("护甲获得 " + amount + " 经验，当前 Lv." + level + "，经验: " + exp + "/" + required));
            }
        } else {
            player.sendSystemMessage(Component.literal("该物品尚未初始化为武器或护甲"));
        }

        return 1;
    }

    private static int setQuality(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        String qualityName = StringArgumentType.getString(context, "quality");

        ItemStack stack = getHeldItem(player);
        if (!stack.isDamageableItem()) {
            player.sendSystemMessage(Component.literal("请手持一个可作为武器或护甲的物品"));
            return 0;
        }
        WeaponQuality quality = null;
        for (WeaponQuality q : WeaponQuality.values()) {
            if (q.name().equalsIgnoreCase(qualityName)) {
                quality = q;
                break;
            }
        }

        if (quality == null) {
            player.sendSystemMessage(Component.literal("无效的品质: " + qualityName));
            return 0;
        }

        if (isWeapon(stack)) {
            WeaponNBTUtil.setQuality(stack, quality);
            WeaponTagManager.updateAttributesOnLevelUp(stack);
            player.sendSystemMessage(Component.literal("已将武器品质设置为: " + quality.getDisplayName()));
        } else if (isArmor(stack)) {
            ArmorNBTUtil.setQuality(stack, quality);
            ArmorTagManager.updateAttributesOnLevelUp(stack);
            player.sendSystemMessage(Component.literal("已将护甲品质设置为: " + quality.getDisplayName()));
        } else {
            player.sendSystemMessage(Component.literal("该物品尚未初始化为武器或护甲"));
        }
        return 1;
    }

    private static int setAttributeCount(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        int count = IntegerArgumentType.getInteger(context, "count");

        ItemStack stack = getHeldItem(player);
        if (!stack.isDamageableItem() || !isWeapon(stack)) {
            player.sendSystemMessage(Component.literal("请手持一个可作为武器的物品"));
            return 0;
        }

        // 先清空原有词条
        Map<WeaponAttribute, Double> existing = WeaponNBTUtil.getAttributes(stack);
        for (WeaponAttribute attr : existing.keySet()) {
            WeaponNBTUtil.removeAttribute(stack, attr);
        }

        WeaponQuality quality = WeaponNBTUtil.getQuality(stack);
        int level = WeaponNBTUtil.getLevel(stack);
        Map<WeaponAttribute, Double> newAttrs = WeaponTagManager.generateRandomAttributes(stack, quality, level);

        int added = 0;
        for (Map.Entry<WeaponAttribute, Double> entry : newAttrs.entrySet()) {
            if (added >= count) break;
            WeaponNBTUtil.setAttribute(stack, entry.getKey(), entry.getValue());
            added++;
        }

        player.sendSystemMessage(Component.literal("已重新生成词条，当前词条数量: " + added));
        return 1;
    }

    private static int addRandomAttributes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        int count = IntegerArgumentType.getInteger(context, "count");

        ItemStack stack = getHeldItem(player);
        if (!stack.isDamageableItem() || !isWeapon(stack)) {
            player.sendSystemMessage(Component.literal("请手持一个可作为武器的物品"));
            return 0;
        }

        WeaponQuality quality = WeaponNBTUtil.getQuality(stack);
        int level = WeaponNBTUtil.getLevel(stack);

        Map<WeaponAttribute, Double> existing = WeaponNBTUtil.getAttributes(stack);
        Map<WeaponAttribute, Double> candidates = WeaponTagManager.generateRandomAttributes(stack, quality, level);

        int maxAttributes = WeaponConfig.getMaxAttributesForQuality(quality);
        int added = 0;
        for (Map.Entry<WeaponAttribute, Double> entry : candidates.entrySet()) {
            if (added >= count) break;
            if (existing.size() + added >= maxAttributes) break;

            WeaponAttribute attr = entry.getKey();
            if (existing.containsKey(attr)) {
                continue;
            }
            WeaponNBTUtil.setAttribute(stack, attr, entry.getValue());
            added++;
        }

        player.sendSystemMessage(Component.literal("已随机新增词条数量: " + added + " / 最大 " + maxAttributes));
        return 1;
    }

    private static int setAttribute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        String attrName = StringArgumentType.getString(context, "attribute");
        double value = DoubleArgumentType.getDouble(context, "value");

        ItemStack stack = getHeldItem(player);
        if (!stack.isDamageableItem()) {
            player.sendSystemMessage(Component.literal("请手持一个可作为武器或护甲的物品"));
            return 0;
        }
        if (isWeapon(stack)) {
            WeaponAttribute attribute = null;
            for (WeaponAttribute a : WeaponAttribute.values()) {
                if (a.name().equalsIgnoreCase(attrName)) {
                    attribute = a;
                    break;
                }
            }

            if (attribute == null) {
                player.sendSystemMessage(Component.literal("无效的武器词条: " + attrName));
                return 0;
            }

            WeaponNBTUtil.setAttribute(stack, attribute, value);
            player.sendSystemMessage(Component.literal("已设置武器词条 " + attribute.getDisplayName() + " 数值为: " + value));
        } else if (isArmor(stack)) {
            ArmorAttribute attribute = null;
            for (ArmorAttribute a : ArmorAttribute.values()) {
                if (a.name().equalsIgnoreCase(attrName)) {
                    attribute = a;
                    break;
                }
            }

            if (attribute == null) {
                player.sendSystemMessage(Component.literal("无效的护甲词条: " + attrName));
                return 0;
            }

            ArmorNBTUtil.setAttribute(stack, attribute, value);
            player.sendSystemMessage(Component.literal("已设置护甲词条 " + attribute.getDisplayName() + " 数值为: " + value));
        }
        return 1;
    }

    private static int deleteAttribute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();
        String attrName = StringArgumentType.getString(context, "attribute");

        ItemStack stack = getHeldItem(player);
        if (!stack.isDamageableItem()) {
            player.sendSystemMessage(Component.literal("请手持一个可作为武器或护甲的物品"));
            return 0;
        }

        if (isWeapon(stack)) {
            WeaponAttribute attribute = null;
            for (WeaponAttribute a : WeaponAttribute.values()) {
                if (a.name().equalsIgnoreCase(attrName)) {
                    attribute = a;
                    break;
                }
            }

            if (attribute == null) {
                player.sendSystemMessage(Component.literal("无效的武器词条: " + attrName));
                return 0;
            }

            Map<WeaponAttribute, Double> existing = WeaponNBTUtil.getAttributes(stack);
            if (!existing.containsKey(attribute)) {
                player.sendSystemMessage(Component.literal("当前武器上没有该词条"));
                return 0;
            }

            WeaponNBTUtil.removeAttribute(stack, attribute);
            player.sendSystemMessage(Component.literal("已删除武器词条: " + attribute.getDisplayName()));
        } else if (isArmor(stack)) {
            ArmorAttribute attribute = null;
            for (ArmorAttribute a : ArmorAttribute.values()) {
                if (a.name().equalsIgnoreCase(attrName)) {
                    attribute = a;
                    break;
                }
            }

            if (attribute == null) {
                player.sendSystemMessage(Component.literal("无效的护甲词条: " + attrName));
                return 0;
            }

            Map<ArmorAttribute, Double> existing = ArmorNBTUtil.getAttributes(stack);
            if (!existing.containsKey(attribute)) {
                player.sendSystemMessage(Component.literal("当前护甲上没有该词条"));
                return 0;
            }

            ArmorNBTUtil.removeAttribute(stack, attribute);
            player.sendSystemMessage(Component.literal("已删除护甲词条: " + attribute.getDisplayName()));
        }
        return 1;
    }
}
