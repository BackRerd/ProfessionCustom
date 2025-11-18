package site.backrer.professioncustom.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.MobTagManager;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.weapon.WeaponAttribute;
import site.backrer.professioncustom.weapon.WeaponNBTUtil;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 生物标签事件处理器，负责处理标签相关的游戏事件
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID)
public class MobTagEventHandler {
    
    private static final Random RANDOM = new Random();
    
    // 存储生物的上次生命值百分比（用于瞬移标签）
    private static final Map<UUID, Float> lastHealthPercentages = new HashMap<>();
    
    // 存储生物的召唤次数（用于领队标签）
    private static final Map<UUID, Integer> summonCounts = new HashMap<>();
    
    /**
     * 处理生物死亡事件 - 实现自爆和复活标签效果
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 排除玩家
        if (entity instanceof Player) {
            return;
        }
        
        // 处理自爆标签
        int explosiveLevel = MobTagManager.getTagLevel(entity, MobTagManager.MobTag.EXPLOSIVE);
        if (explosiveLevel > 0) {
            handleExplosiveDeath(entity, event, explosiveLevel);
        }
        
        // 处理复活标签
        int resurrectLevel = MobTagManager.getTagLevel(entity, MobTagManager.MobTag.RESURRECT);
        if (resurrectLevel > 0) {
            handleResurrectDeath(entity, event, resurrectLevel);
        }
        
        // 注意：标签数据的清理由 MobEventHandler 统一处理，避免重复清理
    }
    
    /**
     * 处理自爆标签 - 死亡时爆炸
     * @param entity 生物实体
     * @param event 死亡事件
     * @param tagLevel 标签等级（1-5）
     */
    private static void handleExplosiveDeath(LivingEntity entity, LivingDeathEvent event, int tagLevel) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // 根据标签等级计算爆炸威力
        double basePowerCfg = site.backrer.professioncustom.MobTagConfigManager.getDouble("professioncustom:tag_explosive", "base_power", 2.0);
        double powerPerLevelCfg = site.backrer.professioncustom.MobTagConfigManager.getDouble("professioncustom:tag_explosive", "power_per_level", 0.5);
        double randomExtraBaseCfg = site.backrer.professioncustom.MobTagConfigManager.getDouble("professioncustom:tag_explosive", "random_extra_base", 1.0);
        double randomExtraPerLevelCfg = site.backrer.professioncustom.MobTagConfigManager.getDouble("professioncustom:tag_explosive", "random_extra_per_level", 0.1);

        float basePower = (float) (basePowerCfg + (tagLevel - 1) * powerPerLevelCfg);
        float randomExtraMax = (float) (randomExtraBaseCfg + tagLevel * randomExtraPerLevelCfg);
        float explosionPower = basePower + (float) (RANDOM.nextDouble() * randomExtraMax);

        // 根据等级增加火焰概率
        double fireBase = site.backrer.professioncustom.MobTagConfigManager.getDouble("professioncustom:tag_explosive", "fire_base", 0.2);
        double firePerLevel = site.backrer.professioncustom.MobTagConfigManager.getDouble("professioncustom:tag_explosive", "fire_per_level", 0.1);
        boolean causesFire = RANDOM.nextDouble() < (fireBase + (tagLevel - 1) * firePerLevel);
        
        // 延迟执行爆炸，确保实体已经死亡
        serverLevel.getServer().execute(() -> {
            serverLevel.explode(
                entity,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                explosionPower,
                causesFire,
                net.minecraft.world.level.Level.ExplosionInteraction.MOB
            );
            
            // 根据等级增加粒子数量
            int particleBase = site.backrer.professioncustom.MobTagConfigManager.getInt("professioncustom:tag_explosive", "particle_base", 15);
            int particlePerLevel = site.backrer.professioncustom.MobTagConfigManager.getInt("professioncustom:tag_explosive", "particle_per_level", 5);
            int particleCount = particleBase + tagLevel * particlePerLevel;
            for (int i = 0; i < particleCount; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * 2.0;
                double offsetY = (RANDOM.nextDouble() - 0.5) * 2.0;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 2.0;
                serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    entity.getX() + offsetX,
                    entity.getY() + offsetY,
                    entity.getZ() + offsetZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
                );
            }
        });
    }
    
    /**
     * 处理复活标签 - 死亡时有概率复活
     * @param entity 生物实体
     * @param event 死亡事件
     * @param tagLevel 标签等级（1-5）
     */
    private static void handleResurrectDeath(LivingEntity entity, LivingDeathEvent event, int tagLevel) {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        
        // 根据等级计算复活概率：等级1: 20%, 等级2: 30%, 等级3: 40%, 等级4: 50%, 等级5: 60%
        double resurrectChance = 0.2 + (tagLevel - 1) * 0.1;
        if (RANDOM.nextDouble() < resurrectChance) {
            event.setCanceled(true);
            
            // 根据等级恢复生命值：等级1: 40%, 等级2: 45%, 等级3: 50%, 等级4: 55%, 等级5: 60%
            float maxHealth = entity.getMaxHealth();
            float healthPercent = 0.4f + (tagLevel - 1) * 0.05f;
            entity.setHealth(maxHealth * healthPercent);
            
            // 根据等级调整再生效果持续时间：等级1: 3秒, 等级5: 7秒
            int regenDuration = 60 + (tagLevel - 1) * 20; // 60-140 ticks (3-7秒)
            int regenAmplifier = Math.min(2, tagLevel / 2); // 等级1-2: 0, 等级3-4: 1, 等级5: 2
            entity.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                regenDuration,
                regenAmplifier
            ));
            
            // 根据等级调整抗性提升效果：等级1: 2秒, 等级5: 5秒
            int resistanceDuration = 40 + (tagLevel - 1) * 15; // 40-100 ticks (2-5秒)
            int resistanceAmplifier = tagLevel >= 4 ? 1 : 0; // 等级4-5: 1级抗性
            entity.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                resistanceDuration,
                resistanceAmplifier
            ));
            
            // 根据等级增加粒子数量
            int particleCount = 20 + tagLevel * 5; // 等级1: 25, 等级5: 45
            if (entity.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < particleCount; i++) {
                    double offsetX = (RANDOM.nextDouble() - 0.5) * 2.0;
                    double offsetY = RANDOM.nextDouble() * 2.0;
                    double offsetZ = (RANDOM.nextDouble() - 0.5) * 2.0;
                    serverLevel.sendParticles(
                        ParticleTypes.TOTEM_OF_UNDYING,
                        entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,
                        1,
                        0.0, 0.0, 0.0,
                        0.0
                    );
                }
            }
        }
    }
    
    /**
     * 处理生物攻击事件 - 实现暴虐标签效果
     * 注意：LivingAttackEvent 无法修改伤害，需要在 LivingHurtEvent 中处理
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity 
            ? (LivingEntity) event.getSource().getEntity() 
            : null;
        
        if (attacker == null || attacker instanceof Player) {
            return;
        }
        
        // 处理暴虐标签 - 添加攻击粒子效果（伤害修改在 LivingHurtEvent 中处理）
        int brutalLevel = MobTagManager.getTagLevel(attacker, MobTagManager.MobTag.BRUTAL);
        if (brutalLevel > 0) {
            // 根据等级增加粒子数量
            int particleCount = 3 + brutalLevel * 2; // 等级1: 5, 等级5: 13
            if (attacker.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    event.getEntity().getX(),
                    event.getEntity().getY() + event.getEntity().getBbHeight() / 2,
                    event.getEntity().getZ(),
                    particleCount,
                    0.3, 0.3, 0.3,
                    0.1
                );
            }
        }
    }
    
    /**
     * 处理生物受伤事件 - 实现寒冷标签和暴虐标签效果
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity 
            ? (LivingEntity) event.getSource().getEntity() 
            : null;
        
        // 处理暴虐标签 - 增加攻击伤害
        if (attacker != null && !(attacker instanceof Player)) {
            int brutalLevel = MobTagManager.getTagLevel(attacker, MobTagManager.MobTag.BRUTAL);
            if (brutalLevel > 0) {
                float originalDamage = event.getAmount();
                // 根据等级增加伤害：等级1: 20%, 等级2: 30%, 等级3: 40%, 等级4: 50%, 等级5: 60%
                float damageMultiplier = 0.2f + (brutalLevel - 1) * 0.1f;
                float bonusDamage = originalDamage * damageMultiplier;
                event.setAmount(originalDamage + bonusDamage);
            }
            
            // 处理贪婪标签 - 攻击时掉血，但造成双倍伤害
            int greedyLevel = MobTagManager.getTagLevel(attacker, MobTagManager.MobTag.GREEDY);
            if (greedyLevel > 0) {
                float originalDamage = event.getAmount();
                // 造成双倍伤害
                event.setAmount(originalDamage * 2.0f);
                
                // 根据等级扣除自身生命值：等级1: 5%, 等级5: 15%
                float healthLossPercent = 0.05f + (greedyLevel - 1) * 0.025f;
                float maxHealth = attacker.getMaxHealth();
                float healthLoss = maxHealth * healthLossPercent;
                attacker.hurt(attacker.damageSources().magic(), healthLoss);
            }
            
            // 处理吸血标签 - 攻击时恢复生命值
            int vampireLevel = MobTagManager.getTagLevel(attacker, MobTagManager.MobTag.VAMPIRE);
            if (vampireLevel > 0) {
                float damage = event.getAmount();
                // 根据等级恢复生命值：等级1: 10%伤害, 等级5: 50%伤害
                float healPercent = 0.1f + (vampireLevel - 1) * 0.1f;
                float healAmount = damage * healPercent;
                attacker.heal(healAmount);
                
                // 添加粒子效果
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        attacker.getX(),
                        attacker.getY() + attacker.getBbHeight() / 2,
                        attacker.getZ(),
                        3,
                        0.3, 0.3, 0.3,
                        0.1
                    );
                }
            }
            
            // 处理光爆标签 - 造成失明和黑暗效果
            int lightburstLevel = MobTagManager.getTagLevel(attacker, MobTagManager.MobTag.LIGHTBURST);
            if (lightburstLevel > 0) {
                // 根据等级调整效果持续时间：等级1: 3秒, 等级5: 7秒
                int blindDuration = 60 + (lightburstLevel - 1) * 20; // 60-140 ticks
                int darkDuration = 80 + (lightburstLevel - 1) * 20; // 80-160 ticks
                
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindDuration, 0));
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, darkDuration, 0));
                
                // 添加粒子效果
                if (victim.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 15; i++) {
                        double offsetX = (RANDOM.nextDouble() - 0.5) * victim.getBbWidth();
                        double offsetY = RANDOM.nextDouble() * victim.getBbHeight();
                        double offsetZ = (RANDOM.nextDouble() - 0.5) * victim.getBbWidth();
                        serverLevel.sendParticles(
                            ParticleTypes.FLASH,
                            victim.getX() + offsetX,
                            victim.getY() + offsetY,
                            victim.getZ() + offsetZ,
                            1,
                            0.0, 0.0, 0.0,
                            0.0
                        );
                    }
                }
            }
            
            // 处理狂刃标签 - 攻击时产生范围伤害
            int frenzyLevel = MobTagManager.getTagLevel(attacker, MobTagManager.MobTag.FRENZY);
            if (frenzyLevel > 0) {
                float damage = event.getAmount();
                // 根据等级调整范围：等级1: 2格, 等级5: 4格
                float range = 2.0f + (frenzyLevel - 1) * 0.5f;
                AABB aabb = new AABB(victim.getX() - range, victim.getY() - 1, victim.getZ() - range,
                                     victim.getX() + range, victim.getY() + 2, victim.getZ() + range);
                List<LivingEntity> nearbyEntities = victim.level().getEntitiesOfClass(LivingEntity.class, aabb);
                
                for (LivingEntity nearby : nearbyEntities) {
                    if (nearby != victim && nearby != attacker && !(nearby instanceof Player)) {
                        // 造成攻击伤害
                        nearby.hurt(attacker.damageSources().mobAttack(attacker), damage);
                        
                        // 添加粒子效果
                        if (attacker.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(
                                ParticleTypes.SWEEP_ATTACK,
                                nearby.getX(),
                                nearby.getY() + nearby.getBbHeight() / 2,
                                nearby.getZ(),
                                1,
                                0.0, 0.0, 0.0,
                                0.0
                            );
                        }
                    }
                }
                
            }
        }
        
        // 处理寒冷标签 - 攻击者拥有寒冷标签时，对目标造成冰冻效果
        if (attacker != null && !(attacker instanceof Player)) {
            int coldLevel = MobTagManager.getTagLevel(attacker, MobTagManager.MobTag.COLD);
            if (coldLevel > 0) {
                // 根据等级计算触发概率：等级1: 40%, 等级2: 50%, 等级3: 60%, 等级4: 70%, 等级5: 80%
                double freezeChance = 0.4 + (coldLevel - 1) * 0.1;
                if (RANDOM.nextDouble() < freezeChance) {
                    // 根据等级调整缓慢效果：等级1: 2秒/1级, 等级5: 5秒/2级
                    int slowDuration = 40 + (coldLevel - 1) * 15; // 40-100 ticks (2-5秒)
                    int slowAmplifier = coldLevel >= 3 ? 1 : 0; // 等级3-5: 2级缓慢
                    victim.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        slowDuration,
                        slowAmplifier
                    ));
                    
                    // 根据等级增加粒子数量
                    int particleCount = 8 + coldLevel * 2; // 等级1: 10, 等级5: 18
                    if (victim.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < particleCount; i++) {
                            double offsetX = (RANDOM.nextDouble() - 0.5) * victim.getBbWidth();
                            double offsetY = RANDOM.nextDouble() * victim.getBbHeight();
                            double offsetZ = (RANDOM.nextDouble() - 0.5) * victim.getBbWidth();
                            serverLevel.sendParticles(
                                ParticleTypes.SNOWFLAKE,
                                victim.getX() + offsetX,
                                victim.getY() + offsetY,
                                victim.getZ() + offsetZ,
                                1,
                                0.0, 0.0, 0.0,
                                0.0
                            );
                        }
                    }
                }
            }
        }
        
        // 处理寒冷标签 - 拥有寒冷标签的生物周围持续造成寒冷效果
        if (!(victim instanceof Player)) {
            int coldLevel = MobTagManager.getTagLevel(victim, MobTagManager.MobTag.COLD);
            if (coldLevel > 0) {
                // 根据等级调整影响范围：等级1: 3格, 等级5: 5格
                int range = 3 + (coldLevel - 1) / 2; // 等级1-2: 3, 等级3-4: 4, 等级5: 5
                AABB aabb = new AABB(victim.getX() - range, victim.getY() - 1, victim.getZ() - range,
                                     victim.getX() + range, victim.getY() + 2, victim.getZ() + range);
                List<Player> nearbyPlayers = victim.level().getEntitiesOfClass(Player.class, aabb);
                
                // 根据等级调整触发概率：等级1: 15%, 等级5: 35%
                double auraChance = 0.15 + (coldLevel - 1) * 0.05;
                for (Player player : nearbyPlayers) {
                    if (RANDOM.nextDouble() < auraChance) {
                        // 根据等级调整效果持续时间：等级1: 1.5秒, 等级5: 3秒
                        int auraDuration = 30 + (coldLevel - 1) * 10; // 30-70 ticks (1.5-3.5秒)
                        player.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            auraDuration,
                            0
                        ));
                    }
                }
            }
            
            // 处理灵魂标签 - 免疫普通攻击，只能被爆炸/药水伤害
            int soulLevel = MobTagManager.getTagLevel(victim, MobTagManager.MobTag.SOUL);
            if (soulLevel > 0) {
                DamageSource source = event.getSource();
                // 检查伤害类型，如果不是爆炸或药水伤害，则免疫
                boolean isExplosion = source.is(DamageTypes.EXPLOSION);
                boolean isMagic = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.WITHER);
                boolean isProjectile = source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
                boolean isFire = source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA);

                // 检查攻击者是否持有带有“灵魂伤害”词条的武器
                boolean hasSoulDamage = false;
                double soulDamageValue = 0.0;
                Entity directAttacker = source.getEntity();
                if (directAttacker instanceof Player player) {
                    ItemStack weapon = player.getMainHandItem();
                    if (WeaponNBTUtil.isWeapon(weapon)) {
                        Map<WeaponAttribute, Double> attrs = WeaponNBTUtil.getAttributes(weapon);
                        if (attrs.containsKey(WeaponAttribute.SOUL_DAMAGE)) {
                            soulDamageValue = attrs.get(WeaponAttribute.SOUL_DAMAGE);
                            if (soulDamageValue > 0) {
                                hasSoulDamage = true;
                            }
                        }
                    }
                }
                
                // 只允许爆炸、魔法、投射物、火焰伤害，或者来自拥有灵魂伤害词条的武器
                if (!isExplosion && !isMagic && !isProjectile && !isFire && !hasSoulDamage) {
                    // 免疫普通攻击
                    event.setCanceled(true);
                    
                    // 添加粒子效果
                    if (victim.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 10; i++) {
                            double offsetX = (RANDOM.nextDouble() - 0.5) * victim.getBbWidth();
                            double offsetY = RANDOM.nextDouble() * victim.getBbHeight();
                            double offsetZ = (RANDOM.nextDouble() - 0.5) * victim.getBbWidth();
                            serverLevel.sendParticles(
                                ParticleTypes.SOUL,
                                victim.getX() + offsetX,
                                victim.getY() + offsetY,
                                victim.getZ() + offsetZ,
                                1,
                                0.0, 0.0, 0.0,
                                0.0
                            );
                        }
                    }
                } else if (hasSoulDamage && soulDamageValue > 0) {
                    // 如果是带有灵魂伤害词条的武器攻击，则额外附加灵魂伤害
                    float extra = (float) soulDamageValue;
                    event.setAmount(event.getAmount() + extra);
                }
            }
            
            // 处理瞬移标签 - 生命值每减少10%瞬移
            int teleportLevel = MobTagManager.getTagLevel(victim, MobTagManager.MobTag.TELEPORT);
            if (teleportLevel > 0 && attacker != null) {
                float currentHealthPercent = victim.getHealth() / victim.getMaxHealth();
                UUID victimId = victim.getUUID();
                Float lastHealthPercent = lastHealthPercentages.getOrDefault(victimId, 1.0f);
                
                // 检查生命值是否减少了10%或更多
                if (lastHealthPercent - currentHealthPercent >= 0.1f) {
                    // 瞬移到攻击者身边
                    if (victim.level() instanceof ServerLevel serverLevel) {
                        Vec3 targetPos = attacker.position();
                        Vec3 teleportPos = targetPos.add(
                            (RANDOM.nextDouble() - 0.5) * 2.0,
                            0.5,
                            (RANDOM.nextDouble() - 0.5) * 2.0
                        );
                        
                        victim.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                        
                        // 添加粒子效果
                        for (int i = 0; i < 20; i++) {
                            serverLevel.sendParticles(
                                ParticleTypes.PORTAL,
                                victim.getX(),
                                victim.getY(),
                                victim.getZ(),
                                1,
                                0.3, 0.3, 0.3,
                                0.1
                            );
                        }
                    }
                }
                
                // 更新生命值百分比
                lastHealthPercentages.put(victimId, currentHealthPercent);
            }
            
            // 处理天神标签 - 被攻击时控制目标并造成伤害
            int celestialLevel = MobTagManager.getTagLevel(victim, MobTagManager.MobTag.CELESTIAL);
            if (celestialLevel > 0 && attacker != null) {
                // 根据等级计算触发概率：等级1: 20%, 等级5: 60%
                double triggerChance = 0.2 + (celestialLevel - 1) * 0.1;
                if (RANDOM.nextDouble() < triggerChance) {
                    // 控制攻击者（悬浮）
                    attacker.addEffect(new MobEffectInstance(
                        MobEffects.LEVITATION,
                        40 + (celestialLevel - 1) * 10, // 2-3秒
                        0
                    ));
                    
                    // 在攻击者头顶生成粒子并砸下
                    if (victim.level() instanceof ServerLevel serverLevel) {
                        double targetX = attacker.getX();
                        double targetY = attacker.getY() + attacker.getBbHeight() + 2.0;
                        double targetZ = attacker.getZ();
                        
                        // 生成粒子
                        for (int i = 0; i < 30; i++) {
                            serverLevel.sendParticles(
                                ParticleTypes.CRIT,
                                targetX + (RANDOM.nextDouble() - 0.5) * 2.0,
                                targetY,
                                targetZ + (RANDOM.nextDouble() - 0.5) * 2.0,
                                1,
                                0.0, -0.2, 0.0,
                                0.1
                            );
                        }
                        
                        // 延迟造成伤害
                        serverLevel.getServer().execute(() -> {
                            float damage = victim.getMaxHealth() * (0.1f + celestialLevel * 0.05f); // 10%-35%最大生命值
                            attacker.hurt(victim.damageSources().magic(), damage);
                            
                            // 爆炸效果
                            serverLevel.explode(
                                victim,
                                targetX,
                                targetY - 1.0,
                                targetZ,
                                1.0f + celestialLevel * 0.5f,
                                false,
                                net.minecraft.world.level.Level.ExplosionInteraction.NONE
                            );
                        });
                    }
                }
            }
            
            // 处理领队标签 - 被攻击时召唤同类生物
            int leaderLevel = MobTagManager.getTagLevel(victim, MobTagManager.MobTag.LEADER);
            if (leaderLevel > 0) {
                UUID victimId = victim.getUUID();
                int currentSummonCount = summonCounts.getOrDefault(victimId, 0);
                
                // 标签等级等于召唤次数限制
                if (currentSummonCount < leaderLevel) {
                    if (victim.level() instanceof ServerLevel serverLevel) {
                        EntityType<?> entityType = victim.getType();
                        
                        // 延迟执行召唤，避免在事件处理中直接创建实体
                        serverLevel.getServer().execute(() -> {
                            try {
                                // 创建实体
                                Entity summonedEntity = entityType.create(serverLevel);
                                if (summonedEntity instanceof LivingEntity summoned) {
                                    // 获取召唤位置
                                    Vec3 summonPos = victim.position().add(
                                        (RANDOM.nextDouble() - 0.5) * 3.0,
                                        0.5,
                                        (RANDOM.nextDouble() - 0.5) * 3.0
                                    );
                                    
                                    // 设置位置
                                    summoned.moveTo(summonPos.x, summonPos.y, summonPos.z, 
                                                   victim.getYRot(), victim.getXRot());
                                    
                                    // 设置召唤生物的等级（低于原生物）
                                    int mobLevel = MobLevelManager.getEntityLevel(victim);
                                    int summonLevel = Math.max(1, mobLevel - 1 - RANDOM.nextInt(3));
                                    
                                    // 添加到世界
                                    if (serverLevel.addFreshEntity(summoned)) {
                                        // 更新召唤计数
                                        summonCounts.put(victimId, currentSummonCount + 1);
                                        
                                        // 添加粒子效果
                                        for (int i = 0; i < 20; i++) {
                                            serverLevel.sendParticles(
                                                ParticleTypes.TOTEM_OF_UNDYING,
                                                summonPos.x,
                                                summonPos.y,
                                                summonPos.z,
                                                1,
                                                0.3, 0.3, 0.3,
                                                0.1
                                            );
                                        }
                                    } else {
                                        Professioncustom.LOGGER.warn("召唤生物添加到世界失败: {}", entityType);
                                    }
                                } else {
                                    Professioncustom.LOGGER.warn("召唤的实体不是生物: {}", entityType);
                                }
                            } catch (Exception e) {
                                Professioncustom.LOGGER.error("召唤生物失败: {}", e.getMessage(), e);
                            }
                        });
                    }
                }
            }
            
            // 处理绝境标签 - 生命值<5%时获得多个词条
            int desperateLevel = MobTagManager.getTagLevel(victim, MobTagManager.MobTag.DESPERATE);
            if (desperateLevel > 0) {
                float healthPercent = victim.getHealth() / victim.getMaxHealth();
                if (healthPercent < 0.05f) {
                    // 移除绝境标签
                    victim.removeTag("professioncustom:tag_desperate");
                    MobTagManager.removeEntityTags(victim.getUUID());
                    
                    // 添加多个词条
                    Map<String, Integer> newTags = new HashMap<>();
                    newTags.put("professioncustom:tag_soul", desperateLevel);
                    newTags.put("professioncustom:tag_unmatched", desperateLevel);
                    newTags.put("professioncustom:tag_explosive", desperateLevel);
                    newTags.put("professioncustom:tag_swift", desperateLevel);
                    newTags.put("professioncustom:tag_celestial", desperateLevel);
                    newTags.put("professioncustom:tag_vampire", desperateLevel);
                    
                    // 应用新标签
                    UUID victimId = victim.getUUID();
                    
                    // 获取当前标签并更新
                    Map<String, Integer> currentTags = new HashMap<>(MobTagManager.getEntityTagLevels(victim));
                    currentTags.putAll(newTags);
                    
                    // 更新缓存
                    MobTagManager.updateEntityTagLevels(victimId, currentTags);
                    
                    // 添加实体标签
                    for (Map.Entry<String, Integer> entry : newTags.entrySet()) {
                        victim.addTag(entry.getKey());
                        victim.addTag(entry.getKey() + "_level_" + entry.getValue());
                    }
                    
                    // 触发急速和无双标签的效果（因为新添加了这些标签）
                    if (victim.level() instanceof ServerLevel) {
                        // 重新应用属性修改
                        int swiftLevel = MobTagManager.getTagLevel(victim, MobTagManager.MobTag.SWIFT);
                        if (swiftLevel > 0) {
                            double speedMultiplier = 0.2 + (swiftLevel - 1) * 0.2;
                            UUID swiftModifierId = UUID.nameUUIDFromBytes(("professioncustom-swift-" + victimId).getBytes(StandardCharsets.UTF_8));
                            safeAddPermanentModifier(
                                victim,
                                Attributes.MOVEMENT_SPEED,
                                swiftModifierId,
                                "Swift Tag Bonus",
                                speedMultiplier,
                                AttributeModifier.Operation.MULTIPLY_TOTAL
                            );
                            victim.addEffect(new MobEffectInstance(
                                MobEffects.MOVEMENT_SPEED,
                                999999,
                                swiftLevel - 1
                            ));
                        }
                        
                        int unmatchedLevel = MobTagManager.getTagLevel(victim, MobTagManager.MobTag.UNMATCHED);
                        if (unmatchedLevel > 0) {
                            double multiplier = 1.0 + unmatchedLevel;
                            if (victim.getAttribute(Attributes.MAX_HEALTH) != null) {
                                double currentMaxHealth = victim.getAttribute(Attributes.MAX_HEALTH).getValue();
                                double bonusHealth = currentMaxHealth * (multiplier - 1.0);
                                UUID healthModifierId = UUID.nameUUIDFromBytes(("professioncustom-unmatched-health-" + victimId).getBytes(StandardCharsets.UTF_8));
                                safeAddPermanentModifier(
                                    victim,
                                    Attributes.MAX_HEALTH,
                                    healthModifierId,
                                    "Unmatched Health Bonus",
                                    bonusHealth,
                                    AttributeModifier.Operation.ADDITION
                                );
                                victim.setHealth(victim.getMaxHealth());
                            }
                            if (victim.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                                double currentAttack = victim.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
                                double bonusAttack = currentAttack * (multiplier - 1.0);
                                UUID attackModifierId = UUID.nameUUIDFromBytes(("professioncustom-unmatched-attack-" + victimId).getBytes(StandardCharsets.UTF_8));
                                safeAddPermanentModifier(
                                    victim,
                                    Attributes.ATTACK_DAMAGE,
                                    attackModifierId,
                                    "Unmatched Attack Bonus",
                                    bonusAttack,
                                    AttributeModifier.Operation.ADDITION
                                );
                            }
                            if (victim.getAttribute(Attributes.ARMOR) != null) {
                                double currentArmor = victim.getAttribute(Attributes.ARMOR).getValue();
                                double bonusArmor = currentArmor * (multiplier - 1.0);
                                UUID armorModifierId = UUID.nameUUIDFromBytes(("professioncustom-unmatched-armor-" + victimId).getBytes(StandardCharsets.UTF_8));
                                safeAddPermanentModifier(
                                    victim,
                                    Attributes.ARMOR,
                                    armorModifierId,
                                    "Unmatched Armor Bonus",
                                    bonusArmor,
                                    AttributeModifier.Operation.ADDITION
                                );
                            }
                        }
                    }
                    
                    // 添加粒子效果
                    if (victim.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 50; i++) {
                            double offsetX = (RANDOM.nextDouble() - 0.5) * victim.getBbWidth() * 2;
                            double offsetY = RANDOM.nextDouble() * victim.getBbHeight() * 2;
                            double offsetZ = (RANDOM.nextDouble() - 0.5) * victim.getBbWidth() * 2;
                            serverLevel.sendParticles(
                                ParticleTypes.TOTEM_OF_UNDYING,
                                victim.getX() + offsetX,
                                victim.getY() + offsetY,
                                victim.getZ() + offsetZ,
                                1,
                                0.0, 0.0, 0.0,
                                0.0
                            );
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 安全地添加属性修改器（如果已存在则先移除）
     */
    private static void safeAddPermanentModifier(LivingEntity entity, 
                                                   net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                                   UUID modifierId,
                                                   String name,
                                                   double amount,
                                                   AttributeModifier.Operation operation) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance == null) {
            return;
        }
        
        // 先移除已存在的修改器（如果存在）
        for (AttributeModifier modifier : attributeInstance.getModifiers()) {
            if (modifier.getId().equals(modifierId)) {
                attributeInstance.removeModifier(modifier);
                break;
            }
        }
        
        // 添加新的修改器
        attributeInstance.addPermanentModifier(
            new AttributeModifier(
                modifierId,
                name,
                amount,
                operation
            )
        );
    }
    
    /**
     * 处理生物加入世界事件 - 实现急速和无双标签效果
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity) || entity instanceof Player || ProfessionConfig.isMobExcluded(livingEntity)) {
            return;
        }
        
        // 处理急速标签 - 提升移动速度
        int swiftLevel = MobTagManager.getTagLevel(livingEntity, MobTagManager.MobTag.SWIFT);
        if (swiftLevel > 0) {
            // 根据等级提升速度：等级1: 20%, 等级5: 100%
            double speedMultiplier = 0.2 + (swiftLevel - 1) * 0.2;
            UUID modifierId = UUID.nameUUIDFromBytes(("professioncustom-swift-" + livingEntity.getUUID()).getBytes(StandardCharsets.UTF_8));
            safeAddPermanentModifier(
                livingEntity,
                Attributes.MOVEMENT_SPEED,
                modifierId,
                "Swift Tag Bonus",
                speedMultiplier,
                AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            
            // 添加速度效果（视觉反馈）
            livingEntity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                999999, // 永久
                swiftLevel - 1
            ));
        }
        
        // 处理无双标签 - 自身属性翻倍
        int unmatchedLevel = MobTagManager.getTagLevel(livingEntity, MobTagManager.MobTag.UNMATCHED);
        if (unmatchedLevel > 0) {
            // 根据等级翻倍：等级1: 2倍, 等级5: 6倍
            double multiplier = 1.0 + unmatchedLevel; // 2x, 3x, 4x, 5x, 6x
            
            // 提升生命值
            if (livingEntity.getAttribute(Attributes.MAX_HEALTH) != null) {
                double currentMaxHealth = livingEntity.getAttribute(Attributes.MAX_HEALTH).getValue();
                double bonusHealth = currentMaxHealth * (multiplier - 1.0);
                UUID healthModifierId = UUID.nameUUIDFromBytes(("professioncustom-unmatched-health-" + livingEntity.getUUID()).getBytes(StandardCharsets.UTF_8));
                safeAddPermanentModifier(
                    livingEntity,
                    Attributes.MAX_HEALTH,
                    healthModifierId,
                    "Unmatched Health Bonus",
                    bonusHealth,
                    AttributeModifier.Operation.ADDITION
                );
                livingEntity.setHealth(livingEntity.getMaxHealth());
            }
            
            // 提升攻击力
            if (livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                double currentAttack = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
                double bonusAttack = currentAttack * (multiplier - 1.0);
                UUID attackModifierId = UUID.nameUUIDFromBytes(("professioncustom-unmatched-attack-" + livingEntity.getUUID()).getBytes(StandardCharsets.UTF_8));
                safeAddPermanentModifier(
                    livingEntity,
                    Attributes.ATTACK_DAMAGE,
                    attackModifierId,
                    "Unmatched Attack Bonus",
                    bonusAttack,
                    AttributeModifier.Operation.ADDITION
                );
            }
            
            // 提升护甲
            if (livingEntity.getAttribute(Attributes.ARMOR) != null) {
                double currentArmor = livingEntity.getAttribute(Attributes.ARMOR).getValue();
                double bonusArmor = currentArmor * (multiplier - 1.0);
                UUID armorModifierId = UUID.nameUUIDFromBytes(("professioncustom-unmatched-armor-" + livingEntity.getUUID()).getBytes(StandardCharsets.UTF_8));
                safeAddPermanentModifier(
                    livingEntity,
                    Attributes.ARMOR,
                    armorModifierId,
                    "Unmatched Armor Bonus",
                    bonusArmor,
                    AttributeModifier.Operation.ADDITION
                );
            }
        }
    }
    
    /**
     * 清理死亡生物的数据
     */
    @SubscribeEvent
    public static void onLivingDeathCleanup(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }
        
        UUID entityId = entity.getUUID();
        lastHealthPercentages.remove(entityId);
        summonCounts.remove(entityId);
    }
}

