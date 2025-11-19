package site.backrer.professioncustom;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import site.backrer.professioncustom.event.MobLevelAttributeListener;
import site.backrer.professioncustom.event.MobRightClickHandler;
import site.backrer.professioncustom.event.MobTagEventHandler;
import site.backrer.professioncustom.profession.ProfessionConfig;
import site.backrer.professioncustom.profession.gui.ModMenus;
import site.backrer.professioncustom.profession.item.ModItems;
import site.backrer.professioncustom.profession.command.ProfessionCommand;
import site.backrer.professioncustom.profession.command.ReloadCommand;
import site.backrer.professioncustom.profession.command.ProfessionTestCommand;
import site.backrer.professioncustom.profession.command.ReloadItemProfessionsCommand;
import site.backrer.professioncustom.weapon.WeaponEditCommand;
import site.backrer.professioncustom.weapon.WeaponConfig;
import site.backrer.professioncustom.profession.event.EquipmentEventHandler;
import site.backrer.professioncustom.profession.event.AttackEventHandler;
import site.backrer.professioncustom.client.ClientKeyMappings;
import site.backrer.professioncustom.render.EntityNameRenderer;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Professioncustom.MODID)
public class Professioncustom {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "professioncustom";
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;
    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public Professioncustom() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(MobLevelAttributeListener.class);
//        MinecraftForge.EVENT_BUS.register(MobRightClickHandler.class);
        MinecraftForge.EVENT_BUS.register(MobTagEventHandler.class);
        
        // Register data pack loader
        MinecraftForge.EVENT_BUS.register(site.backrer.professioncustom.data.DataPackLoader.class);
        
        // Register profession restriction event handlers
        MinecraftForge.EVENT_BUS.register(EquipmentEventHandler.class);
        MinecraftForge.EVENT_BUS.register(AttackEventHandler.class);
        
        // Register profession system
        ProfessionConfig.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobConfig.SPEC);
        WeaponConfig.register();
        ModMenus.REGISTRY.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("ProfessionCustom mod initialized");

        // 在一个固定位置注册所有网络消息，确保客户端和服务器的消息ID顺序一致
        event.enqueueWork(() -> {
            site.backrer.professioncustom.profession.network.CareerSelectButtonMessage.register();
            site.backrer.professioncustom.profession.network.CareerInfoGuiButtonMessage.register();
            site.backrer.professioncustom.profession.network.ModVariables.register();
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ProfessionCustom server starting");
    }

    public static <MSG> void addNetworkMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }

    private static void executeWorkQueue() {
        List<AbstractMap.SimpleEntry<Runnable, Integer>> entries = new ArrayList<>(workQueue);
        workQueue.clear();
        for (AbstractMap.SimpleEntry<Runnable, Integer> entry : entries) {
            entry.getKey().run();
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ProfessionCommand.register(event.getDispatcher());
        ReloadCommand.register(event.getDispatcher());
        ProfessionTestCommand.register(event.getDispatcher());
        ReloadItemProfessionsCommand.register(event.getDispatcher());
        WeaponEditCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void addCreative(BuildCreativeModeTabContentsEvent event) {
        event.accept(ModItems.KKKK);
        event.accept(ModItems.PROFESSION_EXP_BOTTLE);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("ProfessionCustom client setup complete");
            
            // Register menu screens
            event.enqueueWork(() -> {
                net.minecraft.client.gui.screens.MenuScreens.register(
                    site.backrer.professioncustom.profession.gui.ModMenus.CAREER_SELECT.get(),
                    site.backrer.professioncustom.profession.gui.CareerSelectScreen::new
                );
                net.minecraft.client.gui.screens.MenuScreens.register(
                    site.backrer.professioncustom.profession.gui.ModMenus.CAREER_INFO_GUI.get(),
                    site.backrer.professioncustom.profession.gui.CareerInfoGuiScreen::new
                );

                // Register client-side entity name renderer overlay
                MinecraftForge.EVENT_BUS.register(new EntityNameRenderer());
            });
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            ClientKeyMappings.register(event);
        }
    }
}
