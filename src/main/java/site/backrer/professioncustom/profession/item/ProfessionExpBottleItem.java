package site.backrer.professioncustom.profession.item;

import site.backrer.professioncustom.profession.network.ModVariables;
import site.backrer.professioncustom.profession.ProfessionManager;
import site.backrer.professioncustom.profession.Profession;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class ProfessionExpBottleItem extends Item {
    
    // 随机数生成器
    private static final Random RANDOM = new Random();

    // 默认经验值范围
    private final int minExp;
    private final int maxExp;
    
    public ProfessionExpBottleItem(Properties properties) {
        super(properties);
        this.minExp = 5;
        this.maxExp = 15;
    }
    
    public ProfessionExpBottleItem(Properties properties, int minExp, int maxExp) {
        super(properties);
        this.minExp = minExp;
        this.maxExp = maxExp;
    }
    
    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(cap -> {
            System.out.println(cap.maxExperience);
        });
        // 在客户端播放投掷声音
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (RANDOM.nextFloat() * 0.4F + 0.8F));
        
        if (!level.isClientSide) {
            // 在服务器端创建并发射职业经验瓶投射物
            ProfessionExpBottleEntity expBottleEntity = new ProfessionExpBottleEntity(level, player, minExp, maxExp);
            expBottleEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.7F, 1.0F);
            level.addFreshEntity(expBottleEntity);
        }
        
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    @Override
    public @Nonnull UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.BOW;
    }
    
    @Override
    public int getUseDuration(@Nonnull ItemStack stack) {
        return 20;
    }
    
    // 职业经验瓶投射物实体类
    public static class ProfessionExpBottleEntity extends ThrowableItemProjectile {
        
        private final int minExp;
        private final int maxExp;
        
        public ProfessionExpBottleEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
            super(type, level);
            this.minExp = 5;
            this.maxExp = 15;
        }
        
        public ProfessionExpBottleEntity(Level level, LivingEntity thrower, int minExp, int maxExp) {
            super(EntityType.EXPERIENCE_BOTTLE, thrower, level);
            this.minExp = minExp;
            this.maxExp = maxExp;
        }
        
        @Override
        protected Item getDefaultItem() {
            // 由于循环引用问题，我们不能直接引用ProfessionExpBottleItem
            // 但在Minecraft的机制中，这个方法主要用于掉落和粒子效果
            // 我们可以使用一个空物品或者null，因为我们已经自定义了所有行为
            return null;
        }
        
        @Override
        protected float getGravity() {
            return 0.07F;
        }
        
        @Override
        public void handleEntityEvent(byte id) {
            if (id == 3) {
                // 当投射物破裂时，播放粒子效果
                for (int i = 0; i < 8; ++i) {
                    this.level().addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(),
                            (RANDOM.nextFloat() - 0.5D) * 0.08D, (RANDOM.nextFloat() - 0.5D) * 0.08D,
                            (RANDOM.nextFloat() - 0.5D) * 0.08D);
                }
            }
        }
        
        @Override
        protected void onHit(HitResult result) {
            // 移除super.onHit(result)调用，避免递归
            
            if (!this.level().isClientSide) {
                // 播放破裂声音
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.NEUTRAL, 0.7F, 0.7F + this.level().random.nextFloat() * 0.4F);
                
                // 生成经验值
                int expAmount = minExp + RANDOM.nextInt(maxExp - minExp + 1);
                
                // 查找周围的玩家并给予职业经验
                this.spawnExpParticles(8);
                
                // 查找附近的玩家
                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class,
                        new AABB(this.getX() - 4.0D, this.getY() - 4.0D, this.getZ() - 4.0D, 
                                this.getX() + 4.0D, this.getY() + 4.0D, this.getZ() + 4.0D));
                
                // 优先给予投掷者经验
                Player thrower = this.getOwner() instanceof Player ? (Player) this.getOwner() : null;
                if (thrower != null && nearbyPlayers.contains(thrower)) {
                    giveProfessionExp(thrower, expAmount);
                } else if (!nearbyPlayers.isEmpty()) {
                    // 如果投掷者不在附近，给予最近的玩家
                    Player nearestPlayer = findNearestPlayer(this, nearbyPlayers);
                    if (nearestPlayer != null) {
                        giveProfessionExp(nearestPlayer, expAmount);
                    }
                }
                
                // 移除实体
                this.discard();
            }
        }
        
        private void spawnExpParticles(int count) {
            for (int i = 0; i < count; ++i) {
                this.level().addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(),
                        (RANDOM.nextFloat() - 0.5D) * 0.1D, (RANDOM.nextFloat() - 0.5D) * 0.1D,
                        (RANDOM.nextFloat() - 0.5D) * 0.1D);
            }
        }
        
        private static Player findNearestPlayer(Entity center, List<Player> players) {
            Player nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            
            for (Player player : players) {
                double distance = center.distanceToSqr(player);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = player;
                }
            }
            
            return nearest;
        }
        
        private static void giveProfessionExp(Player player, int expAmount) {
            if (player instanceof ServerPlayer serverPlayer) {
                // 获取玩家的变量数据
                ModVariables.PlayerVariables variables = serverPlayer.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new ModVariables.PlayerVariables());
                
                // 检查玩家是否有职业
                if (variables.hasProfession()) {
                    // 检查玩家是否已达到职业最大等级
                    Profession profession = ProfessionManager.getProfessionByName(variables.professionName);
                    
                    if (profession != null && variables.professionLevel >= profession.getMaxLevel()) {
                        // 已达到最大等级，提示玩家
                        player.sendSystemMessage(Component.literal("您已达到" + variables.professionName + "职业的最高等级，无法获得更多经验值！"));
                    } else {
                        // 添加职业经验
                        variables.addExperience(expAmount, serverPlayer);
                        
                        // 发送经验获得消息
                        player.sendSystemMessage(Component.literal("获得了 " + expAmount + " 点职业经验！"));
                    }
                } else {
                    // 如果没有职业，提示玩家
                    player.sendSystemMessage(Component.literal("你还没有选择职业，无法获得职业经验！"));
                }
            }
        }
        
        @Override
        protected void onHitEntity(EntityHitResult result) {
            // 只调用父类方法，避免调用this.onHit()导致递归
            super.onHitEntity(result);
        }
        
        @Override
        protected void onHitBlock(BlockHitResult result) {
            // 只调用父类方法，避免调用this.onHit()导致递归
            super.onHitBlock(result);
        }
    }
}
