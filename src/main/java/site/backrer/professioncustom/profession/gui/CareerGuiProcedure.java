package site.backrer.professioncustom.profession.gui;

import site.backrer.professioncustom.profession.gui.inventory.CareerInfoGuiMenu;
import site.backrer.professioncustom.profession.gui.inventory.CareerSelectMenu;
import site.backrer.professioncustom.profession.Profession;
import site.backrer.professioncustom.profession.ProfessionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import io.netty.buffer.Unpooled;
import java.util.List;

public class CareerGuiProcedure {
    public final static String SELECT_INT = "SelectInt";
    
    public static void CareerSelectUpButtonProcedure(Entity entity) {
        if (entity == null)
            return;
        double currentSelection = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble(SELECT_INT);
        if (currentSelection <= 0) {
            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble(SELECT_INT, ProfessionManager.getNormalProfessions().size() - 1);
        } else {
            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble(SELECT_INT, currentSelection - 1);
        }
    }
    public static void CareerSelectButtonDownProcedure(Entity entity) {
        if (entity == null)
            return;
        double currentSelection = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble(SELECT_INT);
        if (currentSelection >= ProfessionManager.getNormalProfessions().size() - 1) {
            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble(SELECT_INT, 0);
        } else {
            (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().putDouble(SELECT_INT, currentSelection + 1);
        }
    }
    public static void CareerSelectButtonProcedure(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        if (entity instanceof ServerPlayer _ent) {
            BlockPos _bpos = BlockPos.containing(x, y, z);
            NetworkHooks.openScreen((ServerPlayer) _ent, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("CareerInfoGui");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new CareerInfoGuiMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                }
            }, _bpos);
        }
    }
    public static boolean MainSelectInfoProcedure(Entity entity) {
        if (entity == null)
            return false;
        int index = (int) (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble(SELECT_INT);
        List<Profession> professions = ProfessionManager.getNormalProfessions();
        return index >= 0 && index < professions.size();
    }
    public static String CareerSelectTitleProcedure() {
        return Component.translatable("career.select.titles").getString();
    }
    public static String CareerNameProcedure(Entity entity) {
        int index = (int) (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble(SELECT_INT);
        List<Profession> professions = ProfessionManager.getNormalProfessions();
        if (index < 0 || index >= professions.size()) {
            return ""; // 返回一个默认值或错误信息
        }
        return professions.get(index).getDisplayName();
    }
    public static void ER2(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        if (entity instanceof ServerPlayer _ent) {
            BlockPos _bpos = BlockPos.containing(x, y, z);
            NetworkHooks.openScreen((ServerPlayer) _ent, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("CareerSelect");
                }

                @Override
                public @Nonnull AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) {
                    return new CareerSelectMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                }
            }, _bpos);
        }
    }
    public static @Nonnull ResourceLocation getProfessionIcon(Entity entity) {
        int index = (int) (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getDouble(SELECT_INT);
        List<Profession> professions = ProfessionManager.getNormalProfessions();
        if (index >= 0 && index < professions.size()) {
            Profession profession = professions.get(index);
            String professionName = profession.getName().toLowerCase();
            ResourceLocation iconLocation = ResourceLocation.fromNamespaceAndPath("professioncustom", "textures/screens/professions/" + professionName + ".png");
            
            // 检查资源是否存在的逻辑
            try {
                // 尝试获取资源，如果不存在会抛出异常
                net.minecraft.server.packs.resources.ResourceManager resourceManager = 
                    net.minecraft.client.Minecraft.getInstance().getResourceManager();
                if (resourceManager.getResource(iconLocation).isPresent()) {
                    return iconLocation;
                }
            } catch (Exception e) {
                // 资源不存在，使用默认图标
            }
        }
        return ResourceLocation.fromNamespaceAndPath("professioncustom", "textures/screens/2.png");
    }

    public static void ER(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        if (entity instanceof ServerPlayer _ent) {
            BlockPos _bpos = BlockPos.containing(x, y, z);
            NetworkHooks.openScreen((ServerPlayer) _ent, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("CareerInfoGui");
                }

                @Override
                public @Nonnull AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) {
                    return new CareerInfoGuiMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                }
            }, _bpos);
        }
    }
}
