package site.backrer.professioncustom.weapon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 武器NBT工具类，用于读写武器的NBT数据
 */
public class WeaponNBTUtil {
    private static final String NBT_KEY = "ProfessionCustomWeapon";
    private static final String LEVEL_KEY = "Level";
    private static final String EXP_KEY = "Exp";
    private static final String QUALITY_KEY = "Quality";
    private static final String ATTRIBUTES_KEY = "Attributes";
    private static final String ATTRIBUTE_NAME_KEY = "Name";
    private static final String ATTRIBUTE_VALUE_KEY = "Value";
    private static final String LORE_LOADED_KEY = "LoreLoaded";
    
    /**
     * 获取或创建武器的NBT标签
     */
    private static CompoundTag getOrCreateWeaponTag(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_KEY)) {
            tag.put(NBT_KEY, new CompoundTag());
        }
        return tag.getCompound(NBT_KEY);
    }
    
    /**
     * 获取武器等级
     */
    public static int getLevel(ItemStack stack) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        return weaponTag.getInt(LEVEL_KEY);
    }
    
    /**
     * 设置武器等级
     */
    public static void setLevel(ItemStack stack, int level) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        weaponTag.putInt(LEVEL_KEY, level);
    }
    
    /**
     * 获取武器经验值
     */
    public static int getExp(ItemStack stack) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        return weaponTag.getInt(EXP_KEY);
    }
    
    /**
     * 设置武器经验值
     */
    public static void setExp(ItemStack stack, int exp) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        weaponTag.putInt(EXP_KEY, exp);
    }
    
    /**
     * 增加武器经验值
     */
    public static void addExp(ItemStack stack, int exp) {
        int currentExp = getExp(stack);
        setExp(stack, currentExp + exp);
    }
    
    /**
     * 获取武器品质
     */
    public static WeaponQuality getQuality(ItemStack stack) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        if (!weaponTag.contains(QUALITY_KEY)) {
            // 默认品质为普通
            return WeaponQuality.COMMON;
        }
        int qualityValue = weaponTag.getInt(QUALITY_KEY);
        return WeaponQuality.fromValue(qualityValue);
    }
    
    /**
     * 设置武器品质
     */
    public static void setQuality(ItemStack stack, WeaponQuality quality) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        weaponTag.putInt(QUALITY_KEY, quality.getValue());
    }
    
    /**
     * 获取所有词条
     */
    public static Map<WeaponAttribute, Double> getAttributes(ItemStack stack) {
        Map<WeaponAttribute, Double> attributes = new HashMap<>();
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        
        if (weaponTag.contains(ATTRIBUTES_KEY)) {
            ListTag attributesList = weaponTag.getList(ATTRIBUTES_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < attributesList.size(); i++) {
                CompoundTag attrTag = attributesList.getCompound(i);
                String name = attrTag.getString(ATTRIBUTE_NAME_KEY);
                double value = attrTag.getDouble(ATTRIBUTE_VALUE_KEY);
                
                try {
                    WeaponAttribute attribute = WeaponAttribute.valueOf(name);
                    attributes.put(attribute, value);
                } catch (IllegalArgumentException e) {
                    // 忽略无效的词条名称
                }
            }
        }
        
        return attributes;
    }
    
    /**
     * 设置词条
     */
    public static void setAttribute(ItemStack stack, WeaponAttribute attribute, double value) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        ListTag attributesList;
        
        if (weaponTag.contains(ATTRIBUTES_KEY)) {
            attributesList = weaponTag.getList(ATTRIBUTES_KEY, Tag.TAG_COMPOUND);
        } else {
            attributesList = new ListTag();
        }
        
        // 查找是否已存在该词条
        boolean found = false;
        for (int i = 0; i < attributesList.size(); i++) {
            CompoundTag attrTag = attributesList.getCompound(i);
            if (attrTag.getString(ATTRIBUTE_NAME_KEY).equals(attribute.name())) {
                attrTag.putDouble(ATTRIBUTE_VALUE_KEY, value);
                found = true;
                break;
            }
        }
        
        // 如果不存在，添加新词条
        if (!found) {
            CompoundTag attrTag = new CompoundTag();
            attrTag.putString(ATTRIBUTE_NAME_KEY, attribute.name());
            attrTag.putDouble(ATTRIBUTE_VALUE_KEY, value);
            attributesList.add(attrTag);
        }
        
        weaponTag.put(ATTRIBUTES_KEY, attributesList);
    }
    
    /**
     * 移除词条
     */
    public static void removeAttribute(ItemStack stack, WeaponAttribute attribute) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        if (!weaponTag.contains(ATTRIBUTES_KEY)) {
            return;
        }
        
        ListTag attributesList = weaponTag.getList(ATTRIBUTES_KEY, Tag.TAG_COMPOUND);
        for (int i = attributesList.size() - 1; i >= 0; i--) {
            CompoundTag attrTag = attributesList.getCompound(i);
            if (attrTag.getString(ATTRIBUTE_NAME_KEY).equals(attribute.name())) {
                attributesList.remove(i);
                break;
            }
        }
        
        weaponTag.put(ATTRIBUTES_KEY, attributesList);
    }
    
    /**
     * 检查Lore是否已加载
     */
    public static boolean isLoreLoaded(ItemStack stack) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        return weaponTag.getBoolean(LORE_LOADED_KEY);
    }
    
    /**
     * 设置Lore已加载标记
     */
    public static void setLoreLoaded(ItemStack stack, boolean loaded) {
        CompoundTag weaponTag = getOrCreateWeaponTag(stack);
        weaponTag.putBoolean(LORE_LOADED_KEY, loaded);
    }
    
    /**
     * 检查物品是否为武器（是否有武器NBT数据）
     */
    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_KEY);
    }
    
    /**
     * 初始化武器数据（首次创建时调用）
     */
    public static void initializeWeapon(ItemStack stack, WeaponQuality quality) {
        if (isWeapon(stack)) {
            return; // 已经初始化过了
        }
        
        setQuality(stack, quality);
        setLevel(stack, 1);
        setExp(stack, 0);
        setLoreLoaded(stack, false);
    }
    
    /**
     * 刷新武器耐久（升级时调用）
     */
    public static void refreshDurability(ItemStack stack) {
        if (stack.isDamageableItem()) {
            stack.setDamageValue(0);
        }
    }
}

