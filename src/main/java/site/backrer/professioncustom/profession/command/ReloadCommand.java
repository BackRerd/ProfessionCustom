package site.backrer.professioncustom.profession.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import site.backrer.professioncustom.profession.ItemProfessionConfigManager;
import site.backrer.professioncustom.profession.ProfessionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/**
 * 配置热加载命令，允许手动重新加载职业和物品配置
 */
public class ReloadCommand {

    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> reloadNode = Commands.literal("professioncustom")
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2)) // 需要管理员权限
                        .then(Commands.literal("all")
                                .executes(ReloadCommand::reloadAll))
                        .then(Commands.literal("professions")
                                .executes(ReloadCommand::reloadProfessions))
                        .then(Commands.literal("items")
                                .executes(ReloadCommand::reloadItems))
                        .executes(ReloadCommand::showHelp))
                .build();

        dispatcher.getRoot().addChild(reloadNode);
    }

    /**
     * 重新加载所有配置
     */
    private static int reloadAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        
        source.sendSuccess(() -> Component.literal("正在重新加载所有配置..."), true);
        
        // 重新加载职业配置
        ProfessionManager.reload(server.getResourceManager());
        
        // 重新加载物品职业配置
        ItemProfessionConfigManager.reload(server.getResourceManager());
        
        source.sendSuccess(() -> Component.literal("所有配置已成功重新加载！"), true);
        return 1;
    }

    /**
     * 只重新加载职业配置
     */
    private static int reloadProfessions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        
        source.sendSuccess(() -> Component.literal("正在重新加载职业配置..."), true);
        ProfessionManager.reload(server.getResourceManager());
        source.sendSuccess(() -> Component.literal("职业配置已成功重新加载！"), true);
        
        return 1;
    }

    /**
     * 只重新加载物品职业配置
     */
    private static int reloadItems(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        
        source.sendSuccess(() -> Component.literal("正在重新加载物品职业配置..."), true);
        ItemProfessionConfigManager.reload(server.getResourceManager());
        source.sendSuccess(() -> Component.literal("物品职业配置已成功重新加载！"), true);
        
        return 1;
    }

    /**
     * 显示命令帮助信息
     */
    private static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal("=== ProfessionCustom 配置热加载命令 ==="), false);
        source.sendSuccess(() -> Component.literal("/professioncustom reload all - 重新加载所有配置"), false);
        source.sendSuccess(() -> Component.literal("/professioncustom reload professions - 只重新加载职业配置"), false);
        source.sendSuccess(() -> Component.literal("/professioncustom reload items - 只重新加载物品职业配置"), false);
        source.sendSuccess(() -> Component.literal("========================================"), false);
        
        return 1;
    }
}
