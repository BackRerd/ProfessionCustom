package site.backrer.professioncustom.weapon;

import net.minecraft.world.item.ItemStack;

/**
 * 武器等级管理器
 */
public class WeaponLevelManager {
    // 基础经验值需求和倍率从配置读取
    private static int getBaseExpRequired() {
        return WeaponConfig.getBaseExpRequired();
    }

    private static double getExpMultiplier() {
        return WeaponConfig.getExpMultiplier();
    }

    private static int getMaxLevel() {
        return WeaponConfig.getMaxWeaponLevel();
    }

    /**
     * 计算升级所需经验值
     * @param level 当前等级
     * @return 升级所需经验值
     */
    public static int getExpRequiredForLevel(int level) {
        if (level <= 0) {
            return getBaseExpRequired();
        }
        return (int) (getBaseExpRequired() * Math.pow(getExpMultiplier(), level - 1));
    }
    
    /**
     * 增加武器经验值
     * @param stack 武器物品
     * @param exp 增加的经验值
     * @return 是否升级了
     */
    public static boolean addExp(ItemStack stack, int exp) {
        if (!WeaponNBTUtil.isWeapon(stack)) {
            return false;
        }
        
        int currentLevel = WeaponNBTUtil.getLevel(stack);
        int maxLevel = getMaxLevel();
        if (currentLevel >= maxLevel) {
            return false; // 已达到最大等级
        }
        
        int currentExp = WeaponNBTUtil.getExp(stack);
        int newExp = currentExp + exp;
        int expRequired = getExpRequiredForLevel(currentLevel);
        
        boolean leveledUp = false;
        
        // 检查是否可以升级
        while (newExp >= expRequired && currentLevel < maxLevel) {
            newExp -= expRequired;
            currentLevel++;
            leveledUp = true;
            expRequired = getExpRequiredForLevel(currentLevel);
        }
        
        // 更新数据
        WeaponNBTUtil.setLevel(stack, currentLevel);
        WeaponNBTUtil.setExp(stack, newExp);
        
        // 如果升级了，刷新耐久并更新词条数值
        if (leveledUp) {
            WeaponNBTUtil.refreshDurability(stack);
            WeaponTagManager.updateAttributesOnLevelUp(stack);
        }
        
        return leveledUp;
    }
    
    /**
     * 获取击杀怪物获得的经验值
     * @param mobLevel 怪物等级
     * @return 获得的经验值
     */
    public static int getExpFromKill(int mobLevel) {
        // 基础经验值 + 怪物等级加成
        return 10 + mobLevel * 2;
    }
}

