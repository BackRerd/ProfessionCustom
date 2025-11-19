package site.backrer.professioncustom.profession.network;

import site.backrer.professioncustom.profession.Profession;
import site.backrer.professioncustom.profession.ProfessionManager;
import site.backrer.professioncustom.Professioncustom;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModVariables {
    // 在主mod类中以固定顺序调用此方法注册网络消息，避免客户端和服务器消息ID顺序不一致
    public static void register() {
        Professioncustom.addNetworkMessage(PlayerVariablesSyncMessage.class, PlayerVariablesSyncMessage::buffer, PlayerVariablesSyncMessage::new, PlayerVariablesSyncMessage::handler);
    }

    @SubscribeEvent
    public static void init(RegisterCapabilitiesEvent event) {
        event.register(PlayerVariables.class);
    }

    @Mod.EventBusSubscriber
    public static class EventBusVariableHandlers {
        @SubscribeEvent
        public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.getEntity().level().isClientSide()) {
                Player player = event.getEntity();
                PlayerVariables playerVariables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables());
                playerVariables.syncPlayerVariables(player);
                
                // 玩家登录时重新应用职业属性
                if (playerVariables.hasProfession()) {
                    ProfessionManager.setAttributeByProfessionInPlayer(playerVariables.professionName, player);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
            if (!event.getEntity().level().isClientSide()) {
                Player player = event.getEntity();
                PlayerVariables playerVariables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables());
                playerVariables.syncPlayerVariables(player);
                
                // 玩家重生时重新应用职业属性
                if (playerVariables.hasProfession()) {
                    ProfessionManager.setAttributeByProfessionInPlayer(playerVariables.professionName, player);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (!event.getEntity().level().isClientSide())
                ((PlayerVariables) event.getEntity().getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
        }

        @SubscribeEvent
        public static void clonePlayer(PlayerEvent.Clone event) {
            event.getOriginal().revive();
            PlayerVariables original = ((PlayerVariables) event.getOriginal().getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
            PlayerVariables clone = ((PlayerVariables) event.getEntity().getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
            clone.skill = original.skill;
            // 复制职业相关数据
            clone.professionName = original.professionName;
            clone.professionLevel = original.professionLevel;
            clone.currentExperience = original.currentExperience;
            clone.maxExperience = original.maxExperience;
            
            // 如果是死亡重生，重新应用职业属性
            if (event.isWasDeath() && clone.hasProfession()) {
                ProfessionManager.setAttributeByProfessionInPlayer(clone.professionName, event.getEntity());
            }
        }
    }

    public static final Capability<PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerVariables>() {
    });

    @Mod.EventBusSubscriber
    private static class PlayerVariablesProvider implements ICapabilitySerializable<Tag> {
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer))
                event.addCapability(ResourceLocation.fromNamespaceAndPath("professioncustom", "player_variables"), new PlayerVariablesProvider());
        }

        private final PlayerVariables playerVariables = new PlayerVariables();
        private final LazyOptional<PlayerVariables> instance = LazyOptional.of(() -> playerVariables);

        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == ModVariables.PLAYER_VARIABLES_CAPABILITY ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public Tag serializeNBT() {
            return playerVariables.writeNBT();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            playerVariables.readNBT(nbt);
        }
    }

    public static class PlayerVariables {
        public double skill = 0;
        // 职业相关属性
        @Nonnull
        public String professionName = "";
        public int professionLevel = 1;
        public int currentExperience = 0;
        public int maxExperience = 100;

        public void syncPlayerVariables(Entity entity) {
            if (entity instanceof ServerPlayer serverPlayer)
                Professioncustom.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PlayerVariablesSyncMessage(this));
        }

        public Tag writeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putDouble("skill", skill);
            // 保存职业相关数据
            nbt.putString("professionName", professionName);
            nbt.putInt("professionLevel", professionLevel);
            nbt.putInt("currentExperience", currentExperience);
            nbt.putInt("maxExperience", maxExperience);
            return nbt;
        }

        public void readNBT(Tag tag) {
            if (!(tag instanceof CompoundTag nbt)) {
                return;
            }
            skill = nbt.getDouble("skill");
            // 读取职业相关数据
            professionName = nbt.getString("professionName");
            professionLevel = nbt.getInt("professionLevel");
            currentExperience = nbt.getInt("currentExperience");
            maxExperience = nbt.getInt("maxExperience");
        }
        
        /**
         * 为玩家添加经验值，自动处理升级
         * @param amount 要添加的经验值数量
         * @param player 玩家实体，用于发送升级消息
         * @return 是否升级
         */
        public boolean addExperience(int amount, Player player) {
            if (professionName.isEmpty()) {
                return false; // 未选择职业，无法获得经验
            }
            
            // 获取职业信息，检查是否已达到最大等级
            Profession profession = ProfessionManager.getProfessionByName(professionName);
            if (profession != null && professionLevel >= profession.getMaxLevel()) {
                // 已经达到或超过最大等级，不再增加经验值，防止溢出
                // 不执行任何操作，直接返回
                return false;
            }
            
            // 只有在玩家未达到最大等级时才增加经验值
            currentExperience += amount;
            boolean leveledUp = false;
            
            // 检查是否可以升级
            while (currentExperience >= maxExperience) {
                // 再次检查是否已达到最大等级
                if (profession != null && professionLevel >= profession.getMaxLevel()) {
                    // 达到最大等级后，将经验值设置为0，防止溢出
                    currentExperience = 0;
                    break;
                }
                levelUp(player);
                leveledUp = true;
                // 每次升级后重新获取职业信息，确保使用最新数据
                profession = ProfessionManager.getProfessionByName(professionName);
            }
            
            if (leveledUp || amount > 0) {
                syncPlayerVariables(player); // 同步数据到客户端
            }
            
            return leveledUp;
        }
        
        /**
         * 设置玩家职业
         * @param professionName 职业名称
         * @param player 玩家实体，用于同步数据
         */
        public void setProfession(String professionName, Player player) {
            this.professionName = professionName;
            this.professionLevel = 1;
            this.currentExperience = 0;
            
            // 初始最大经验值也使用正确的公式：maxExp * 当前等级(1) * (multiplier*10)
            Profession profession = ProfessionManager.getProfessionByName(professionName);
            if (profession != null) {
                this.maxExperience = (int)(profession.getMaxExp() * 1 * (profession.getMultiplier() * 10));
            } else {
                this.maxExperience = 100; // 如果无法获取职业信息，使用默认值
            }
            
            syncPlayerVariables(player); // 同步数据到客户端
            
            // 应用职业属性
            ProfessionManager.setAttributeByProfessionInPlayer(professionName, player);
        }
        
        public void clearProfession(Player player) {
            this.professionName = "";
            this.professionLevel = 1;
            this.currentExperience = 0;
            this.maxExperience = 100;
            syncPlayerVariables(player);
            ProfessionManager.clearProfessionAttributes(player);
        }
        
        /**
         * 玩家升级处理
         * @param player 玩家实体，用于发送升级消息
         */
        private void levelUp(Player player) {
            // 获取职业信息，检查是否已达到最大等级
            Profession profession = ProfessionManager.getProfessionByName(professionName);
            if (profession != null && professionLevel >= profession.getMaxLevel()) {
                // 已经达到最大等级，不再升级
                return;
            }
            
            // 扣除升级所需经验值
            currentExperience -= maxExperience;
            
            // 增加等级
            professionLevel++;
            
            // 获取职业信息，检查是否达到最大等级
            String message = "恭喜升级！您的" + professionName + "职业已提升至等级 " + professionLevel + "！";
            
            if (profession != null && professionLevel >= profession.getMaxLevel()) {
                // 达到最大等级，发送特殊提示
                message += "\n您已达到" + professionName + "职业的最高等级！";
            } else {
                // 未达到最大等级，使用正确的经验值计算公式：maxExp * 当前等级 * (multiplier*10)
                if (profession != null) {
                    maxExperience = (int)(profession.getMaxExp() * professionLevel * (profession.getMultiplier() * 10));
                } else {
                    // 如果无法获取职业信息，使用默认值
                    maxExperience = 100 * professionLevel * 10; // 假设默认multiplier为1
                }
            }
            
            // 发送升级提示消息
            @Nonnull
            Component messageComponent = Component.literal(message);
            player.sendSystemMessage(messageComponent);
            
            // 应用升级后的职业属性
            ProfessionManager.setAttributeByProfessionInPlayer(professionName, player);
        }
        
        /**
         * 检查玩家是否有职业
         * @return 是否有职业
         */
        public boolean hasProfession() {
            return !professionName.isEmpty();
        }
        
        /**
         * 获取玩家当前经验进度
         * @return 经验进度比例（0.0 - 1.0）
         */
        public float getExperienceProgress() {
            if (maxExperience <= 0) return 0.0f;
            return (float) currentExperience / maxExperience;
        }
    }

    public static class PlayerVariablesSyncMessage {
        private final PlayerVariables data;

        public PlayerVariablesSyncMessage(FriendlyByteBuf buffer) {
            this.data = new PlayerVariables();
            this.data.readNBT(buffer.readNbt());
        }

        public PlayerVariablesSyncMessage(PlayerVariables data) {
            this.data = data;
        }

        public static void buffer(PlayerVariablesSyncMessage message, FriendlyByteBuf buffer) {
            buffer.writeNbt((CompoundTag) message.data.writeNBT());
        }

        public static void handler(PlayerVariablesSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getDirection().getReceptionSide().isClient()) {
                    handleClient(message);
                }
            });
            context.setPacketHandled(true);
        }

        @OnlyIn(Dist.CLIENT)
        private static void handleClient(PlayerVariablesSyncMessage message) {
            @Nullable
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PlayerVariables variables = player.getCapability(ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables());
                variables.skill = message.data.skill;
                // 同步职业相关数据
                variables.professionName = message.data.professionName;
                variables.professionLevel = message.data.professionLevel;
                variables.currentExperience = message.data.currentExperience;
                variables.maxExperience = message.data.maxExperience;
            }
        }
    }
}
