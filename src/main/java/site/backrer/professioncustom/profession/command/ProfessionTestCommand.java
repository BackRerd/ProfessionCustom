package site.backrer.professioncustom.profession.command;

import site.backrer.professioncustom.profession.network.ModVariables;
import site.backrer.professioncustom.profession.ProfessionManager;
import site.backrer.professioncustom.profession.Profession;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ProfessionTestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("professiontest")
            // 设置职业命令
            .then(Commands.literal("set")
                .then(Commands.argument("professionName", StringArgumentType.word())
                    .suggests((context, builder) -> 
                        SharedSuggestionProvider.suggest(
                            ProfessionManager.getAllProfessions().keySet(), 
                            builder
                        )
                    )
                    .executes(context -> {
                        Player player = context.getSource().getPlayerOrException();
                        String professionName = StringArgumentType.getString(context, "professionName");
                        
                        ModVariables.PlayerVariables variables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ModVariables.PlayerVariables());
                        variables.setProfession(professionName, player);
                        
                        player.sendSystemMessage(Component.literal("已设置职业为: " + professionName + "，等级: 1"));
                        return 1;
                    })
                )
            )
            // 添加经验命令
            .then(Commands.literal("addxp")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        Player player = context.getSource().getPlayerOrException();
                        int amount = IntegerArgumentType.getInteger(context, "amount");
                        
                        ModVariables.PlayerVariables variables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ModVariables.PlayerVariables());
                        
                        if (!variables.hasProfession()) {
                            player.sendSystemMessage(Component.literal("错误: 您还没有选择职业！"));
                            return 0;
                        }
                        
                        // 检查玩家是否已达到职业最大等级
                        Profession profession = 
                                ProfessionManager.getProfessionByName(variables.professionName);
                        
                        if (profession != null && variables.professionLevel >= profession.getMaxLevel()) {
                            player.sendSystemMessage(Component.literal("您已达到" + variables.professionName + "职业的最高等级，无法获得更多经验值！"));
                            return 0;
                        }
                        
                        boolean leveledUp = variables.addExperience(amount, player);
                        
                        if (leveledUp) {
                            player.sendSystemMessage(Component.literal("获得 " + amount + " 经验值！已升级至等级 " + variables.professionLevel));
                        } else {
                            player.sendSystemMessage(Component.literal("获得 " + amount + " 经验值！当前等级: " + variables.professionLevel + ", 经验: " + 
                                variables.currentExperience + "/" + variables.maxExperience + " (" + (int)(variables.getExperienceProgress() * 100) + "%)"));
                        }
                        
                        return 1;
                    })
                )
            )
            // 给指定半径内的玩家添加经验命令
            .then(Commands.literal("addxpradius")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
                        .executes(context -> {
                            Player commandPlayer = context.getSource().getPlayerOrException();
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            int radius = IntegerArgumentType.getInteger(context, "radius");
                            
                            // 获取指定半径内的所有玩家
                            List<Player> nearbyPlayers = commandPlayer.level().getEntitiesOfClass(
                                Player.class,
                                commandPlayer.getBoundingBox().inflate(radius),
                                p -> true
                            );
                            
                            int affectedPlayers = 0;
                            
                            // 给每个玩家添加经验
                            for (Player player : nearbyPlayers) {
                                ModVariables.PlayerVariables variables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new ModVariables.PlayerVariables());
                                
                                if (variables.hasProfession()) {
                                    // 检查玩家是否已达到职业最大等级
                                    Profession profession = 
                                            ProfessionManager.getProfessionByName(variables.professionName);
                                    
                                    if (profession != null && variables.professionLevel >= profession.getMaxLevel()) {
                                        player.sendSystemMessage(Component.literal("您已达到" + variables.professionName + "职业的最高等级，无法获得更多经验值！"));
                                    } else {
                                        variables.addExperience(amount, player);
                                        player.sendSystemMessage(Component.literal("获得 " + amount + " 点职业经验！"));
                                        affectedPlayers++;
                                    }
                                }
                            }
                            
                            commandPlayer.sendSystemMessage(Component.literal("已成功给半径 " + radius + " 格内的 " + affectedPlayers + " 名玩家添加了 " + amount + " 点职业经验！"));
                            return affectedPlayers;
                        })
                    )
                )
            )
            // 查看职业信息命令
            .then(Commands.literal("info")
                .executes(context -> {
                    Player player = context.getSource().getPlayerOrException();
                    
                    ModVariables.PlayerVariables variables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ModVariables.PlayerVariables());
                    
                    if (!variables.hasProfession()) {
                        player.sendSystemMessage(Component.literal("您还没有选择职业！"));
                        return 0;
                    }
                    
                    player.sendSystemMessage(Component.literal("===== 职业信息 ====="));
                    player.sendSystemMessage(Component.literal("职业: " + variables.professionName));
                    player.sendSystemMessage(Component.literal("等级: " + variables.professionLevel));
                    player.sendSystemMessage(Component.literal("经验值: " + variables.currentExperience + "/" + variables.maxExperience));
                    player.sendSystemMessage(Component.literal("进度: " + (int)(variables.getExperienceProgress() * 100) + "%"));
                    player.sendSystemMessage(Component.literal("=================="));
                    
                    return 1;
                })
            )
            // 帮助信息
            .executes(context -> {
                Player player = context.getSource().getPlayerOrException();
                player.sendSystemMessage(Component.literal("===== 职业测试命令帮助 ====="));
                player.sendSystemMessage(Component.literal("/professiontest set <职业名称> - 设置您的职业"));
                player.sendSystemMessage(Component.literal("/professiontest addxp <经验值> - 添加职业经验值"));
                player.sendSystemMessage(Component.literal("/professiontest info - 查看当前职业信息"));
                player.sendSystemMessage(Component.literal("==========================="));
                return 1;
            })
        );
    }
}
