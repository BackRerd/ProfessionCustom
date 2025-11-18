package site.backrer.professioncustom.profession.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import site.backrer.professioncustom.profession.Profession;
import site.backrer.professioncustom.profession.ProfessionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 职业相关命令
 * 提供列出所有职业和查看特定职业详细信息的功能
 */
public class ProfessionCommand {
    
    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("profession")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .then(Commands.literal("list")
                    .executes(ProfessionCommand::listProfessions))
                .then(Commands.literal("info")
                    .then(Commands.argument("professionName", StringArgumentType.string())
                        .suggests((context, builder) -> 
                            SharedSuggestionProvider.suggest(
                                ProfessionManager.getAllProfessions().keySet(), 
                                builder
                            )
                        )
                        .executes(ProfessionCommand::showProfessionInfo))
                )
        );
    }
    
    /**
     * 列出所有已注册的职业
     */
    private static int listProfessions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Map<String, Profession> professions = ProfessionManager.getAllProfessions();
        
        if (professions.isEmpty()) {
            source.sendSuccess(() -> Component.literal("没有找到已注册的职业。").withStyle(ChatFormatting.RED), false);
            return 0;
        }
        
        // 发送职业列表标题
        source.sendSuccess(() -> Component.literal("已注册的职业列表：").withStyle(ChatFormatting.GOLD), false);
        
        // 分组显示：基础职业和进阶职业
        Map<Boolean, Map<String, Profession>> groupedProfessions = professions.entrySet().stream()
            .collect(Collectors.partitioningBy(
                entry -> entry.getValue().isNormal(),
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
            ));
        
        // 显示基础职业
        if (!groupedProfessions.get(true).isEmpty()) {
            source.sendSuccess(() -> Component.literal("\n基础职业：").withStyle(ChatFormatting.BLUE), false);
            groupedProfessions.get(true).forEach((name, profession) -> {
                source.sendSuccess(() -> 
                    Component.literal(" - " + name + " (" + profession.getDisplayName() + ")")
                        .withStyle(ChatFormatting.WHITE), 
                    false
                );
            });
        }
        
        // 显示进阶职业
        if (!groupedProfessions.get(false).isEmpty()) {
            source.sendSuccess(() -> Component.literal("\n进阶职业：").withStyle(ChatFormatting.BLUE), false);
            groupedProfessions.get(false).forEach((name, profession) -> {
                String upperProfessionName = profession.getUpperProfession() != null 
                    ? profession.getUpperProfession().getName() 
                    : "未知";
                source.sendSuccess(() -> 
                    Component.literal(" - " + name + " (" + profession.getDisplayName() + ") 上级: " + upperProfessionName)
                        .withStyle(ChatFormatting.WHITE), 
                    false
                );
            });
        }
        
        source.sendSuccess(() -> Component.literal("\n总计：" + professions.size() + " 个职业").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
    
    /**
     * 显示特定职业的详细信息
     */
    private static int showProfessionInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String professionName = StringArgumentType.getString(context, "professionName");
        
        Profession profession = ProfessionManager.getProfessionByName(professionName);
        
        if (profession == null) {
            source.sendSuccess(() -> 
                Component.literal("未找到名称为 '" + professionName + "' 的职业。").withStyle(ChatFormatting.RED), 
                false
            );
            return 0;
        }
        
        // 发送职业详细信息
        source.sendSuccess(() -> 
            Component.literal("===== " + profession.getDisplayName() + " =====").withStyle(ChatFormatting.GOLD), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("职业ID：").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(profession.getName()).withStyle(ChatFormatting.WHITE)), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("职业类型：").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(profession.isNormal() ? "基础职业" : "进阶职业").withStyle(ChatFormatting.WHITE)), 
            false
        );
        
        if (!profession.isNormal() && profession.getUpperProfession() != null) {
            source.sendSuccess(() -> 
                Component.literal("上级职业：").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(profession.getUpperProfession().getName() + " (" + 
                           profession.getUpperProfession().getDisplayName() + ")").withStyle(ChatFormatting.WHITE)), 
                false
            );
        }
        
        source.sendSuccess(() -> 
            Component.literal("职业等级：").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(profession.getProfessionLevel())).withStyle(ChatFormatting.WHITE)), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("最大等级：").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(profession.getMaxLevel())).withStyle(ChatFormatting.WHITE)), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("基础升级经验：").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(profession.getMaxExp())).withStyle(ChatFormatting.WHITE)), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("成长倍率：").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(profession.getMultiplier())).withStyle(ChatFormatting.WHITE)), 
            false
        );
        
        // 显示属性加成
        source.sendSuccess(() -> 
            Component.literal("\n属性加成：").withStyle(ChatFormatting.BLUE), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("生命值：+").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(profession.getHealth())).withStyle(ChatFormatting.RED)), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("护甲值：+").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(profession.getArmor())).withStyle(ChatFormatting.GREEN)), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("攻击力：+").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(profession.getDamage())).withStyle(ChatFormatting.YELLOW)), 
            false
        );
        
        source.sendSuccess(() -> 
            Component.literal("攻击速度：+").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(profession.getDamageSpeed())).withStyle(ChatFormatting.AQUA)), 
            false
        );
        
        return 1;
    }
}
