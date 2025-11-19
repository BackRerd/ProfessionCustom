package site.backrer.professioncustom.profession.gui;

import site.backrer.professioncustom.profession.gui.inventory.CareerSelectMenu;
import site.backrer.professioncustom.profession.network.CareerSelectButtonMessage;
import site.backrer.professioncustom.Professioncustom;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;

public class CareerSelectScreen extends AbstractContainerScreen<CareerSelectMenu> {
	private final static HashMap<String, Object> guistate = CareerSelectMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	ImageButton imagebutton_3;
	ImageButton imagebutton_4;
	ImageButton imagebutton_5;

	public CareerSelectScreen(CareerSelectMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		guiGraphics.blit(ResourceLocation.fromNamespaceAndPath("professioncustom", "textures/screens/8.png"), this.leftPos + -12, this.topPos + 6, 0, 0, 198, 142, 198, 142);

		if (CareerGuiProcedure.MainSelectInfoProcedure(entity)) {
            try {
                ResourceLocation iconLocation = CareerGuiProcedure.getProfessionIcon(entity);
                if (iconLocation != null) {
                    RenderSystem.setShaderTexture(0, iconLocation);
                    guiGraphics.blit(iconLocation, this.leftPos + 77, this.topPos + 63, 0, 0, 28, 28, 28, 28);
                }
            } catch (Exception e) {
                // 如果图标加载失败，使用默认图标
                ResourceLocation defaultIcon = ResourceLocation.fromNamespaceAndPath("professioncustom", "textures/screens/2.png");
                guiGraphics.blit(defaultIcon, this.leftPos + 77, this.topPos + 63, 0, 0, 28, 28, 28, 28);
            }
        }
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font,
				CareerGuiProcedure.CareerSelectTitleProcedure(), 71, 12, -12829636, false);
		guiGraphics.drawString(this.font,
				CareerGuiProcedure.CareerNameProcedure(entity), 71, 128, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		imagebutton_3 = new ImageButton(this.leftPos + 114, this.topPos + 119, 57, 28, 0, 0, 28, ResourceLocation.fromNamespaceAndPath("professioncustom", "textures/screens/atlas/imagebutton_3.png"), 57, 56, e -> {
			if (true) {
				Professioncustom.PACKET_HANDLER.sendToServer(new CareerSelectButtonMessage(0, x, y, z));
				CareerSelectButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		});
		guistate.put("button:imagebutton_3", imagebutton_3);
		this.addRenderableWidget(imagebutton_3);
		imagebutton_4 = new ImageButton(this.leftPos + 3, this.topPos + 119, 57, 28, 0, 0, 28, ResourceLocation.fromNamespaceAndPath("professioncustom", "textures/screens/atlas/imagebutton_4.png"), 57, 56, e -> {
			if (true) {
				Professioncustom.PACKET_HANDLER.sendToServer(new CareerSelectButtonMessage(1, x, y, z));
				CareerSelectButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		});
		guistate.put("button:imagebutton_4", imagebutton_4);
		this.addRenderableWidget(imagebutton_4);
		imagebutton_5 = new ImageButton(this.leftPos + 60, this.topPos + 35, 57, 85, 0, 0, 85, ResourceLocation.fromNamespaceAndPath("professioncustom", "textures/screens/atlas/imagebutton_5.png"), 57, 170, e -> {
			if (true) {
				Professioncustom.PACKET_HANDLER.sendToServer(new CareerSelectButtonMessage(2, x, y, z));
				CareerSelectButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		});
		guistate.put("button:imagebutton_5", imagebutton_5);
		this.addRenderableWidget(imagebutton_5);
	}
}
