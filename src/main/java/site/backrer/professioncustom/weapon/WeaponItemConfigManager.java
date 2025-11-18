package site.backrer.professioncustom.weapon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

import static site.backrer.professioncustom.Professioncustom.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class WeaponItemConfigManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "item";

    private static final Map<String, WeaponItemConfig> ITEM_CONFIGS = new HashMap<>();

    public WeaponItemConfigManager() {
        super(GSON, FOLDER);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new WeaponItemConfigManager());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ITEM_CONFIGS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            JsonElement jsonElement = entry.getValue();

            try {
                // 支持两种顶层结构：单个对象 或 对象数组
                if (jsonElement.isJsonArray()) {
                    for (JsonElement element : jsonElement.getAsJsonArray()) {
                        if (!element.isJsonObject()) {
                            continue;
                        }
                        JsonObject obj = element.getAsJsonObject();
                        WeaponItemConfig data = GSON.fromJson(obj, WeaponItemConfig.class);

                        if (data == null || data.itemId == null || data.itemId.isEmpty()) {
                            continue;
                        }

                        if (data.type == null || data.type.isEmpty()) {
                            data.type = "weapon";
                        }

                        String key = data.itemId.toLowerCase();
                        ITEM_CONFIGS.put(key, data);
                    }
                    continue;
                }

                if (!jsonElement.isJsonObject()) {
                    continue;
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                WeaponItemConfig data = GSON.fromJson(jsonObject, WeaponItemConfig.class);

                String itemId = null;
                if (data != null && data.itemId != null && !data.itemId.isEmpty()) {
                    itemId = data.itemId;
                } else {
                    String path = entry.getKey().getPath();
                    if (path.startsWith(FOLDER + "/")) {
                        String fileName = path.substring(FOLDER.length() + 1);
                        int index = fileName.indexOf('_');
                        if (index > 0) {
                            String namespace = fileName.substring(0, index);
                            String name = fileName.substring(index + 1);
                            itemId = namespace + ":" + name;
                        } else {
                            itemId = MODID + ":" + fileName;
                        }
                    }
                }

                if (itemId == null || itemId.isEmpty()) {
                    continue;
                }

                if (data == null) {
                    data = new WeaponItemConfig();
                }
                data.itemId = itemId;
                if (data.type == null || data.type.isEmpty()) {
                    data.type = "weapon";
                }

                String key = itemId.toLowerCase();
                ITEM_CONFIGS.put(key, data);
            } catch (Exception ignored) {
            }
        }
    }

    public static WeaponItemConfig getConfig(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemLocation == null) {
            return null;
        }
        String itemId = itemLocation.toString().toLowerCase();
        return ITEM_CONFIGS.get(itemId);
    }

    public static boolean isWeaponItem(ItemStack stack) {
        return getConfig(stack) != null;
    }

    public static class WeaponItemConfig {
        public String itemId;
        public String type = "weapon";
    }
}
