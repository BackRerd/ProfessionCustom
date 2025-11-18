package site.backrer.professioncustom.profession.gui;

import site.backrer.professioncustom.Professioncustom;
import site.backrer.professioncustom.profession.gui.inventory.CareerInfoGuiMenu;
import site.backrer.professioncustom.profession.gui.inventory.CareerSelectMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Professioncustom.MODID);
    public static final RegistryObject<MenuType<CareerSelectMenu>> CAREER_SELECT = REGISTRY.register("career_select", () -> IForgeMenuType.create(CareerSelectMenu::new));
    public static final RegistryObject<MenuType<CareerInfoGuiMenu>> CAREER_INFO_GUI = REGISTRY.register("career_info_gui", () -> IForgeMenuType.create(CareerInfoGuiMenu::new));
}
