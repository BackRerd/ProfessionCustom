package site.backrer.professioncustom.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.profession.ProfessionManager;

/**
 * 客户端资源重载监听注册，用于在客户端加载职业 JSON 配置，
 * 这样职业选择界面可以正确获取职业列表和显示名称/图标。
 */
@Mod.EventBusSubscriber(modid = Professioncustom.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientReloadListeners {

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        // 在客户端资源管理器中注册职业管理器
        event.registerReloadListener(ProfessionManager.getInstance());
    }
}
