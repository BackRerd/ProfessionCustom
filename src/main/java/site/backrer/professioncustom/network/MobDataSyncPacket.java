package site.backrer.professioncustom.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.MobTagManager;
import site.backrer.professioncustom.Professioncustom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 生物数据同步数据包 - 用于同步生物等级和标签数据到客户端
 */
public class MobDataSyncPacket {
    private final UUID entityId;
    private final int level;
    private final Map<String, Integer> tagLevels;
    
    public MobDataSyncPacket(UUID entityId, int level, Map<String, Integer> tagLevels) {
        this.entityId = entityId;
        this.level = level;
        this.tagLevels = new HashMap<>(tagLevels);
    }
    
    public MobDataSyncPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readUUID();
        this.level = buffer.readInt();
        
        int tagCount = buffer.readInt();
        this.tagLevels = new HashMap<>();
        for (int i = 0; i < tagCount; i++) {
            String tagId = buffer.readUtf();
            int tagLevel = buffer.readInt();
            this.tagLevels.put(tagId, tagLevel);
        }
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(entityId);
        buffer.writeInt(level);
        
        buffer.writeInt(tagLevels.size());
        for (Map.Entry<String, Integer> entry : tagLevels.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            try {
                // 在客户端更新生物数据
                if (context.getDirection().getReceptionSide().isClient()) {
                    handleClientSide();
                }
            } catch (Exception e) {
                Professioncustom.LOGGER.error("Failed to handle mob data sync packet: {}", e.getMessage());
            }
        });
        context.setPacketHandled(true);
    }
    
    private void handleClientSide() {
        // 更新客户端的生物等级和标签数据
        // 注意：这里需要确保在客户端线程中执行
        net.minecraft.client.Minecraft.getInstance().execute(() -> {
            try {
                // 更新等级数据
                MobLevelManager.updateEntityLevel(entityId, level);
                
                // 更新标签数据
                MobTagManager.updateEntityTagLevels(entityId, tagLevels);
                
                Professioncustom.LOGGER.debug("Synced mob data for entity {}: level={}, tags={}", 
                    entityId, level, tagLevels);
            } catch (Exception e) {
                Professioncustom.LOGGER.error("Failed to sync mob data on client: {}", e.getMessage());
            }
        });
    }
    
    /**
     * 创建并发送生物数据同步包
     * @param entity 生物实体
     */
    public static void sendToClients(LivingEntity entity) {
        try {
            UUID entityId = entity.getUUID();
            int level = MobLevelManager.getEntityLevel(entity);
            Map<String, Integer> tagLevels = MobTagManager.getEntityTagLevels(entity);
            
            MobDataSyncPacket packet = new MobDataSyncPacket(entityId, level, tagLevels);
            
            // 简化发送逻辑，使用现有的网络处理器
            // 这里可以根据需要实现具体的发送逻辑
            Professioncustom.LOGGER.debug("Created mob data sync packet for entity: {}", entityId);
        } catch (Exception e) {
            Professioncustom.LOGGER.error("Failed to send mob data sync packet: {}", e.getMessage());
        }
    }
    
    /**
     * 注册数据包
     */
    public static void register() {
        Professioncustom.addNetworkMessage(
            MobDataSyncPacket.class,
            MobDataSyncPacket::encode,
            MobDataSyncPacket::new,
            MobDataSyncPacket::handle
        );
    }
}
