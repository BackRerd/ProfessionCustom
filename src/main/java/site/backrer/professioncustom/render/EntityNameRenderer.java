package site.backrer.professioncustom.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.MobLevelManager;
import site.backrer.professioncustom.MobTagManager;

public class EntityNameRenderer {

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (!ProfessionConfig.isMobLevelingEnabled()) {
            return;
        }

        if (!ProfessionConfig.isShowExtraLevelTag()) {
            return;
        }
        // 在世界渲染的最后阶段绘制
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // 检测玩家正在看向的实体
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        EntityHitResult entityHit = (EntityHitResult) hitResult;
        Entity targetEntity = entityHit.getEntity();

        // 只对生物实体显示
        if (!(targetEntity instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) targetEntity;

        renderEntityInfo(event.getPoseStack(), livingEntity, event.getPartialTick());
    }

    private void renderEntityInfo(PoseStack poseStack, LivingEntity entity, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        if (ProfessionConfig.isMobExcluded(entity)) {
            return;
        }

        // 计算实体位置
        double entityX = entity.xo + (entity.getX() - entity.xo) * partialTick;
        double entityY = entity.yo + (entity.getY() - entity.yo) * partialTick;
        double entityZ = entity.zo + (entity.getZ() - entity.zo) * partialTick;

        // 计算相机位置
        var camera = mc.gameRenderer.getMainCamera();
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        poseStack.pushPose();

        // 移动到实体头顶上方
        poseStack.translate(
                entityX - camX,
                entityY - camY + entity.getBbHeight() + 1.0,
                entityZ - camZ
        );

        // 朝向玩家
        poseStack.mulPose(camera.rotation());

        // 缩放以适应距离
        float scale = 0.025f;
        poseStack.scale(-scale, -scale, scale);

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Matrix4f matrix = poseStack.last().pose();
        int level = MobLevelManager.getEntityLevel(entity);
        
        // 准备显示的文本
        Component nameText = entity.getDisplayName();
        Component healthText = Component.literal(
                String.format("§c❤ %.1f/%.1f", entity.getHealth(), entity.getMaxHealth())
        );
        Component typeText = Component.literal(
                "§7[§6Lv§8.§d" + level + "§7]"
        );

        // 获取标签信息
        java.util.List<MobTagManager.TagDisplayInfo> tagInfos = MobTagManager.getTagDisplayInfo(entity);
        
        // 计算文本宽度用于居中
        int nameWidth = font.width(nameText);
        int healthWidth = font.width(healthText);
        int typeWidth = font.width(typeText);

        // 背景透明度
        int backgroundOpacity = 100;
        
        int currentY = 0;

        // 渲染生物名称（第一行）
        font.drawInBatch(
                nameText,
                -nameWidth / 2f,
                currentY,
                0xFFFFFF,
                false,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                backgroundOpacity << 24,
                15728880
        );
        currentY += 10;

        // 渲染生物类型（第二行）
        font.drawInBatch(
                typeText,
                -typeWidth / 2f,
                currentY,
                0xAAAAAA,
                false,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                backgroundOpacity << 24,
                15728880
        );
        currentY += 10;

        // 渲染标签信息（第三行及之后）
        if (!tagInfos.isEmpty()) {
            // 构建标签文本组件
            Component tagsLabel = Component.translatable("tag.professioncustom.tags")
                    .withStyle(net.minecraft.ChatFormatting.GRAY);
            Component tagsComponent = tagsLabel.copy().append(": ");
            
            for (int i = 0; i < tagInfos.size(); i++) {
                MobTagManager.TagDisplayInfo tagInfo = tagInfos.get(i);
                // 根据标签等级设置颜色
                net.minecraft.ChatFormatting color = getTagColorByLevel(tagInfo.getLevel());
                
                // 添加标签名称和等级
                // 使用字符串形式并应用颜色代码
                String tagNameStr = tagInfo.getName();
                String colorCode = getTagColorCode(color);
                Component tagName = Component.literal(colorCode + tagNameStr);
                Component tagLevel = Component.literal(" §7" + Component.translatable("tag.professioncustom.level").getString() + tagInfo.getLevel());
                
                tagsComponent = tagsComponent.copy().append(tagName).append(tagLevel);
                
                // 如果不是最后一个标签，添加分隔符
                if (i < tagInfos.size() - 1) {
                    tagsComponent = tagsComponent.copy().append(
                            Component.literal(" | ").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
                }
            }
            
            int tagsWidth = font.width(tagsComponent);
            
            font.drawInBatch(
                    tagsComponent,
                    -tagsWidth / 2f,
                    currentY,
                    0xDDDDDD,
                    false,
                    matrix,
                    buffer,
                    Font.DisplayMode.NORMAL,
                    backgroundOpacity << 24,
                    15728880
            );
            currentY += 10;
        }

        // 渲染血量信息（最后一行）
        font.drawInBatch(
                healthText,
                -healthWidth / 2f,
                currentY,
                0xFF5555,
                false,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                backgroundOpacity << 24,
                15728880
        );

        buffer.endBatch();
        poseStack.popPose();
    }
    
    private net.minecraft.ChatFormatting getTagColorByLevel(int level) {
        switch (level) {
            case 5:
                return net.minecraft.ChatFormatting.GOLD; // 金色 - 最高级
            case 4:
                return net.minecraft.ChatFormatting.DARK_PURPLE; // 紫色 - 高级
            case 3:
                return net.minecraft.ChatFormatting.AQUA; // 浅蓝色 - 中级
            case 2:
                return net.minecraft.ChatFormatting.GREEN; // 绿色 - 低级
            case 1:
            default:
                return net.minecraft.ChatFormatting.GRAY; // 灰色 - 最低级
        }
    }
    
    private String getTagColorCode(net.minecraft.ChatFormatting formatting) {
        switch (formatting) {
            case GOLD:
                return "§6";
            case DARK_PURPLE:
                return "§5";
            case AQUA:
                return "§b";
            case GREEN:
                return "§a";
            case GRAY:
            default:
                return "§7";
        }
    }
}