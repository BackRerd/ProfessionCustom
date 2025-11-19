package site.backrer.professioncustom.profession.gui;

import site.backrer.professioncustom.profession.gui.inventory.CareerInfoGuiMenu;
import site.backrer.professioncustom.profession.network.CareerInfoGuiButtonMessage;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.profession.Profession;
import site.backrer.professioncustom.profession.ProfessionManager;
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
import java.util.List;

public class CareerInfoGuiScreen extends AbstractContainerScreen<CareerInfoGuiMenu> {
	private final static HashMap<String, Object> guistate = CareerInfoGuiMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	ImageButton imagebutton_que_ren_an_niu;

	public CareerInfoGuiScreen(CareerInfoGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 131;
	}

	private static final ResourceLocation texture = new ResourceLocation("professioncustom:textures/screens/career_info_gui.png");

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
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

		guiGraphics.blit(new ResourceLocation("professioncustom:textures/screens/zhu_ti_-kong_.png"), this.leftPos + -7, this.topPos + 19, 0, 0, 192, 93, 192, 93);

		guiGraphics.blit(new ResourceLocation("professioncustom:textures/screens/biao_ti_.png"), this.leftPos + 33, this.topPos + -16, 0, 0, 109, 36, 109, 36);

		guiGraphics.blit(new ResourceLocation("professioncustom:textures/screens/bei_jing_2.png"), this.leftPos + 5, this.topPos + 32, 0, 0, 110, 67, 110, 67);

		guiGraphics.blit(new ResourceLocation("professioncustom:textures/screens/xuan_ze_kuang_.png"), this.leftPos + 120, this.topPos + 31, 0, 0, 54, 69, 54, 69);

		guiGraphics.blit(new ResourceLocation("professioncustom:textures/screens/jian_tou_.png"), this.leftPos + 144, this.topPos + 37, 0, 0, 6, 4, 6, 4);

		if (CareerGuiProcedure.MainSelectInfoProcedure(entity)) {
			guiGraphics.blit(CareerGuiProcedure.getProfessionIcon(entity), this.leftPos + 133, this.topPos + 54, 0, 0, 28, 28, 28, 28);
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
		try {
			int select = (int) entity.getMainHandItem().getOrCreateTag().getDouble(CareerGuiProcedure.SELECT_INT);
			List<Profession> professions = ProfessionManager.getNormalProfessions();
			if (select < 0 || select >= professions.size()) {
				// 显示默认信息
				guiGraphics.drawString(this.font, "No Profession Selected", 10, 37, 0x404040, false);
				return;
			}
			
			Profession profession = professions.get(select);
			
			// 使用可见的颜色渲染文字
			int titleColor = 0x404040;  // 深灰色
			int textColor = 0x404040;   // 深灰色
			
			// 标题
			guiGraphics.drawString(this.font, CareerGuiProcedure.CareerSelectTitleProcedure(), 69, -6, titleColor, false);
			
			// 职业信息
			guiGraphics.drawString(this.font, 
				Component.translatable("gui.professioncustom.career_info_gui.label_health")
					.append(": " + profession.getHealth()), 10, 37, textColor, false);
			guiGraphics.drawString(this.font, 
				Component.translatable("gui.professioncustom.career_info_gui.label_armar")
					.append(": " + profession.getArmor()), 10, 46, textColor, false);
			guiGraphics.drawString(this.font, 
				Component.translatable("gui.professioncustom.career_info_gui.label_damage1")
					.append(": " + profession.getDamage()), 10, 56, textColor, false);
			guiGraphics.drawString(this.font, 
				Component.translatable("gui.professioncustom.career_info_gui.label_beilv013")
					.append(": " + profession.getMultiplier()), 10, 66, textColor, false);
			guiGraphics.drawString(this.font, 
				Component.translatable("gui.professioncustom.career_info_gui.label_exp")
					.append(": " + profession.getMaxExp()), 10, 76, textColor, false);
			guiGraphics.drawString(this.font, 
				Component.translatable("gui.professioncustom.career_info_gui.label_level35")
					.append(": " + profession.getMaxLevel()), 11, 85, textColor, false);
		} catch (Exception e) {
			// 如果出现异常，显示错误信息
			guiGraphics.drawString(this.font, "Error loading profession data", 10, 37, 0xFF0000, false);
			Professioncustom.LOGGER.error("Error rendering career info GUI labels: {}", e.getMessage());
		}
	}

	@Override
	public void init() {
		super.init();
		imagebutton_que_ren_an_niu = new ImageButton(this.leftPos + 71, this.topPos + 115, 37, 9, 0, 0, 9, new ResourceLocation("professioncustom:textures/screens/atlas/imagebutton_que_ren_an_niu.png"), 37, 18, e -> {
			if (true) {
				Professioncustom.PACKET_HANDLER.sendToServer(new CareerInfoGuiButtonMessage(0, x, y, z));
				CareerInfoGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		});

		guistate.put("button:imagebutton_que_ren_an_niu", imagebutton_que_ren_an_niu);
		this.addRenderableWidget(imagebutton_que_ren_an_niu);
	}
}
