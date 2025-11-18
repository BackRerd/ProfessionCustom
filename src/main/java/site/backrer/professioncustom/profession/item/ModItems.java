package site.backrer.professioncustom.profession.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import site.backrer.professioncustom.Professioncustom;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Professioncustom.MODID);
    
    // kkkk物品 - 用于打开职业选择GUI
    public static final RegistryObject<Item> KKKK = ITEMS.register("kkkk", KkkkItem::new);
    
    // 职业经验瓶 - 使用magic_item.png作为贴图
    public static final RegistryObject<Item> PROFESSION_EXP_BOTTLE = ITEMS.register("profession_exp_bottle", 
            () -> new ProfessionExpBottleItem(new Item.Properties().stacksTo(64)));

}
