package site.backrer.professioncustom.profession.command;

import site.backrer.professioncustom.profession.network.ModVariables;
import site.backrer.professioncustom.profession.ProfessionManager;
import site.backrer.professioncustom.profession.Profession;
import site.backrer.professioncustom.profession.ProfessionHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
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
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            Player target = EntityArgument.getPlayer(context, "target");
                            String professionName = StringArgumentType.getString(context, "professionName");

                            boolean success = ProfessionHelper.setPlayerProfession(target, professionName);
                            if (success) {
                                target.sendSystemMessage(Component.literal("已设置职业为: " + professionName + "，等级: 1"));
                                if (context.getSource().isPlayer() && context.getSource().getPlayer() != null && context.getSource().getPlayer() != target) {
                                    context.getSource().sendSuccess(() -> Component.literal("已为 " + target.getName().getString() + " 设置职业为: " + professionName + "，等级: 1"), false);
                                }
                                return 1;
                            } else {
                                context.getSource().sendFailure(Component.literal("设置职业失败，可能是职业不存在。"));
                                return 0;
                            }
                        })
                    )
                    .executes(context -> {
                        Player player = context.getSource().getPlayerOrException();
                        String professionName = StringArgumentType.getString(context, "professionName");

                        boolean success = ProfessionHelper.setPlayerProfession(player, professionName);
                        if (success) {
                            player.sendSystemMessage(Component.literal("已设置职业为: " + professionName + "，等级: 1"));
                            return 1;
                        } else {
                            player.sendSystemMessage(Component.literal("设置职业失败，可能是职业不存在。"));
                            return 0;
                        }
                    })
                )
            )
            .then(Commands.literal("reset")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> {
                        Player target = EntityArgument.getPlayer(context, "target");
                        ModVariables.PlayerVariables variables = target.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                .orElse(new ModVariables.PlayerVariables());

                        if (!variables.hasProfession()) {
                            context.getSource().sendFailure(Component.literal(target.getName().getString() + " 当前没有职业，无需重置。"));
                            return 0;
                        }

                        variables.clearProfession(target);
                        target.sendSystemMessage(Component.literal("您的职业已被重置，现在处于无职业状态。"));
                        if (context.getSource().isPlayer() && context.getSource().getPlayer() != null && context.getSource().getPlayer() != target) {
                            context.getSource().sendSuccess(() -> Component.literal("已将 " + target.getName().getString() + " 的职业重置为无职业状态。"), false);
                        }
                        return 1;
                    })
                )
                .executes(context -> {
                    Player player = context.getSource().getPlayerOrException();
                    ModVariables.PlayerVariables variables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                            .orElse(new ModVariables.PlayerVariables());

                    if (!variables.hasProfession()) {
                        player.sendSystemMessage(Component.literal("您当前没有职业，无需重置。"));
                        return 0;
                    }

                    variables.clearProfession(player);
                    player.sendSystemMessage(Component.literal("您的职业已被重置，现在处于无职业状态。"));
                    return 1;
                })
            )
            // 添加经验命令
            .then(Commands.literal("addxp")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            Player target = EntityArgument.getPlayer(context, "target");
                            int amount = IntegerArgumentType.getInteger(context, "amount");

                            ModVariables.PlayerVariables variables = target.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ModVariables.PlayerVariables());

                            if (!variables.hasProfession()) {
                                target.sendSystemMessage(Component.literal("错误: 您还没有选择职业！"));
                                return 0;
                            }

                            // 检查玩家是否已达到职业最大等级
                            Profession profession = 
                                    ProfessionManager.getProfessionByName(variables.professionName);

                            if (profession != null && variables.professionLevel >= profession.getMaxLevel()) {
                                target.sendSystemMessage(Component.literal("您已达到" + variables.professionName + "职业的最高等级，无法获得更多经验值！"));
                                return 0;
                            }

                            boolean leveledUp = variables.addExperience(amount, target);

                            if (leveledUp) {
                                target.sendSystemMessage(Component.literal("获得 " + amount + " 经验值！已升级至等级 " + variables.professionLevel));
                            } else {
                                target.sendSystemMessage(Component.literal("获得 " + amount + " 经验值！当前等级: " + variables.professionLevel + ", 经验: " + 
                                    variables.currentExperience + "/" + variables.maxExperience + " (" + (int)(variables.getExperienceProgress() * 100) + "%)"));
                            }

                            if (context.getSource().isPlayer() && context.getSource().getPlayer() != null && context.getSource().getPlayer() != target) {
                                context.getSource().sendSuccess(() -> Component.literal("已为 " + target.getName().getString() + " 添加 " + amount + " 点职业经验。"), false);
                            }

                            return 1;
                        })
                    )
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
                player.sendSystemMessage(Component.literal("/professiontest set <职业名称> [玩家] - 设置指定玩家（默认自己）的职业"));
                player.sendSystemMessage(Component.literal("/professiontest addxp <经验值> [玩家] - 为指定玩家（默认自己）添加职业经验值"));
                player.sendSystemMessage(Component.literal("/professiontest reset [玩家] - 重置指定玩家（默认自己）的职业为无职业状态"));
                player.sendSystemMessage(Component.literal("/professiontest info - 查看当前职业信息"));
                player.sendSystemMessage(Component.literal("==========================="));
                return 1;
            })
        );
    }
}
