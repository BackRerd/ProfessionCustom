package site.backrer.professioncustom.profession.network;

import site.backrer.professioncustom.profession.gui.CareerGuiProcedure;
import site.backrer.professioncustom.Professioncustom;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CareerSelectButtonMessage {
	private final int buttonID, x, y, z;

	public CareerSelectButtonMessage(FriendlyByteBuf buffer) {
		this.buttonID = buffer.readInt();
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
	}

	public CareerSelectButtonMessage(int buttonID, int x, int y, int z) {
		this.buttonID = buttonID;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static void buffer(CareerSelectButtonMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}

	public static void handler(CareerSelectButtonMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
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
			CareerGuiProcedure.CareerSelectUpButtonProcedure(entity);
		}
		if (buttonID == 1) {
			CareerGuiProcedure.CareerSelectButtonDownProcedure(entity);
		}
		if (buttonID == 2) {
			CareerGuiProcedure.CareerSelectButtonProcedure(world, x, y, z, entity);
		}
	}

	public static void register() {
		Professioncustom.addNetworkMessage(CareerSelectButtonMessage.class, CareerSelectButtonMessage::buffer, CareerSelectButtonMessage::new, CareerSelectButtonMessage::handler);
	}
}
