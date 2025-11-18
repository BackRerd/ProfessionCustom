package site.backrer.professioncustom.profession.item;

import site.backrer.professioncustom.profession.gui.CareerGuiProcedure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class KkkkItem extends Item {
    public KkkkItem() {
        super(new Properties().stacksTo(64).rarity(Rarity.COMMON));
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player entity, @Nonnull InteractionHand hand) {
        InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
        CareerGuiProcedure.ER2(world, entity.getX(), entity.getY(), entity.getZ(), entity);
        return ar;
    }

    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        super.useOn(context);
        CareerGuiProcedure.ER2(context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getPlayer());
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onEntitySwing(@Nonnull ItemStack itemstack, @Nonnull LivingEntity entity) {
        boolean retval = super.onEntitySwing(itemstack, entity);
        CareerGuiProcedure.ER(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
        return retval;
    }
}
