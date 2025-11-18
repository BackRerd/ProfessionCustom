package site.backrer.professioncustom.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class ClientKeyMappings {

   public static final String CATEGORY = "key.categories.professioncustom";

   public static KeyMapping SHOW_LORE;

   public static void register(RegisterKeyMappingsEvent event) {
      SHOW_LORE = new KeyMapping(
         "key.professioncustom.show_lore",
         InputConstants.Type.KEYSYM,
         GLFW.GLFW_KEY_Z,
         CATEGORY
      );
      event.register(SHOW_LORE);
   }

   /**
    * 在任意界面中检查当前绑定键是否按下
    */
   public static boolean isShowLoreKeyDown() {
      if (SHOW_LORE == null) {
         return false;
      }
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.getWindow() == null) {
         return false;
      }
      InputConstants.Key key = SHOW_LORE.getKey();
      long handle = mc.getWindow().getWindow();
      return InputConstants.isKeyDown(handle, key.getValue());
   }

   public static Component getShowLoreKeyName() {
      if (SHOW_LORE == null) {
         return Component.literal("?");
      }
      return SHOW_LORE.getTranslatedKeyMessage();
   }
}
