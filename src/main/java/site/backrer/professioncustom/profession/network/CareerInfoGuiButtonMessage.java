package site.backrer.professioncustom.profession.network;

import site.backrer.professioncustom.profession.gui.CareerGuiProcedure;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.profession.ProfessionHelper;
import site.backrer.professioncustom.profession.ProfessionManager;
import site.backrer.professioncustom.profession.Profession;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CareerInfoGuiButtonMessage {
	private final int buttonID, x, y, z;

	public CareerInfoGuiButtonMessage(FriendlyByteBuf buffer) {
		this.buttonID = buffer.readInt();
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
	}

	public CareerInfoGuiButtonMessage(int buttonID, int x, int y, int z) {
		this.buttonID = buttonID;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static void buffer(CareerInfoGuiButtonMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}

	public static void handler(CareerInfoGuiButtonMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			Player entity = context.getSender();
			int buttonID = message.buttonID;
			int x = message.x;
			int y = message.y;
			int z = message.z;
			handleButtonAction(entity, buttonID, x, y, z);
		});
		context.setPacketHandled(true);
	}

	public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
		Level world = entity.level();
		// security measure to prevent arbitrary chunk generation
		if (!world.hasChunkAt(new BlockPos(x, y, z)))
			return;
		if (buttonID == 0) {
			//功能实现
			if (ProfessionHelper.getPlayerProfession(entity) != null){
				entity.displayClientMessage(Component.literal((Component.translatable("messages.button1.msg").getString())), false);
				return;
			}
			int select = (int) entity.getMainHandItem().getOrCreateTag().getDouble(CareerGuiProcedure.SELECT_INT);
			List<Profession> professions = ProfessionManager.getNormalProfessions();
			if (select < 0 || select >= professions.size()) {
				return;
			}
			Profession profession = professions.get(select);
			ProfessionHelper.setPlayerProfession(entity,profession.getName());
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		Professioncustom.addNetworkMessage(CareerInfoGuiButtonMessage.class, CareerInfoGuiButtonMessage::buffer, CareerInfoGuiButtonMessage::new, CareerInfoGuiButtonMessage::handler);
	}
}
