package site.backrer.professioncustom.profession.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import site.backrer.professioncustom.profession.ItemProfessionConfigManager;

public class ReloadItemProfessionsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reloaditemprofessions")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .executes(context -> {
                    return reloadItemProfessions(context.getSource());
                }));
    }
    
    private static int reloadItemProfessions(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        
        if (player.getServer() == null) {
            source.sendFailure((Component) Component.literal("服务器未找到！"));
            return 0;
        }
        
        try {
            // 重新加载物品职业配置
            ItemProfessionConfigManager.reload(player.getServer().getResourceManager());
            
            source.sendSuccess(() -> Component.literal("物品职业配置已重新加载！"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("重新加载物品职业配置时出错: " + e.getMessage()));
            return 0;
        }
    }
}
