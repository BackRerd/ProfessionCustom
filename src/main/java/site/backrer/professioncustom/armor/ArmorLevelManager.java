package site.backrer.professioncustom.armor;

import net.minecraft.world.item.ItemStack;
import site.backrer.professioncustom.weapon.WeaponConfig;

public class ArmorLevelManager {

    private static int getBaseExpRequired() {
        return WeaponConfig.getBaseExpRequired();
    }

    private static double getExpMultiplier() {
        return WeaponConfig.getExpMultiplier();
    }

    private static int getMaxLevel() {
        return WeaponConfig.getMaxWeaponLevel();
    }

    public static int getExpRequiredForLevel(int level) {
        if (level <= 0) {
            return getBaseExpRequired();
        }
        return (int) (getBaseExpRequired() * Math.pow(getExpMultiplier(), level - 1));
    }

    public static boolean addExp(ItemStack stack, int exp) {
        if (!ArmorNBTUtil.isArmor(stack)) {
            return false;
        }

        int currentLevel = ArmorNBTUtil.getLevel(stack);
        int maxLevel = getMaxLevel();
        if (currentLevel >= maxLevel) {
            return false;
        }

        int currentExp = ArmorNBTUtil.getExp(stack);
        int newExp = currentExp + exp;
        int expRequired = getExpRequiredForLevel(currentLevel);

        boolean leveledUp = false;

        while (newExp >= expRequired && currentLevel < maxLevel) {
            newExp -= expRequired;
            currentLevel++;
            leveledUp = true;
            expRequired = getExpRequiredForLevel(currentLevel);
        }

        ArmorNBTUtil.setLevel(stack, currentLevel);
        ArmorNBTUtil.setExp(stack, newExp);

        if (leveledUp) {
            ArmorTagManager.updateAttributesOnLevelUp(stack);
        }

        return leveledUp;
    }
}
