package site.backrer.professioncustom.armor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import site.backrer.professioncustom.weapon.WeaponQuality;

import java.util.HashMap;
import java.util.Map;

public class ArmorNBTUtil {
    private static final String NBT_KEY = "ProfessionCustomArmor";
    private static final String LEVEL_KEY = "Level";
    private static final String EXP_KEY = "Exp";
    private static final String QUALITY_KEY = "Quality";
    private static final String ATTRIBUTES_KEY = "Attributes";
    private static final String ATTRIBUTE_NAME_KEY = "Name";
    private static final String ATTRIBUTE_VALUE_KEY = "Value";

    private static CompoundTag getOrCreateArmorTag(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_KEY)) {
            tag.put(NBT_KEY, new CompoundTag());
        }
        return tag.getCompound(NBT_KEY);
    }

    public static int getLevel(ItemStack stack) {
        CompoundTag armorTag = getOrCreateArmorTag(stack);
        return armorTag.getInt(LEVEL_KEY);
    }

    public static void setLevel(ItemStack stack, int level) {
        CompoundTag armorTag = getOrCreateArmorTag(stack);
        armorTag.putInt(LEVEL_KEY, level);
    }

    public static int getExp(ItemStack stack) {
        CompoundTag armorTag = getOrCreateArmorTag(stack);
        return armorTag.getInt(EXP_KEY);
    }

    public static void setExp(ItemStack stack, int exp) {
        CompoundTag armorTag = getOrCreateArmorTag(stack);
        armorTag.putInt(EXP_KEY, exp);
    }

    public static void addExp(ItemStack stack, int exp) {
        int currentExp = getExp(stack);
        setExp(stack, currentExp + exp);
    }

    public static WeaponQuality getQuality(ItemStack stack) {
        CompoundTag armorTag = getOrCreateArmorTag(stack);
        if (!armorTag.contains(QUALITY_KEY)) {
            return WeaponQuality.COMMON;
        }
        int qualityValue = armorTag.getInt(QUALITY_KEY);
        return WeaponQuality.fromValue(qualityValue);
    }

    public static void setQuality(ItemStack stack, WeaponQuality quality) {
        CompoundTag armorTag = getOrCreateArmorTag(stack);
        armorTag.putInt(QUALITY_KEY, quality.getValue());
    }

    public static Map<ArmorAttribute, Double> getAttributes(ItemStack stack) {
        Map<ArmorAttribute, Double> attributes = new HashMap<>();
        CompoundTag armorTag = getOrCreateArmorTag(stack);

        if (armorTag.contains(ATTRIBUTES_KEY)) {
            ListTag attributesList = armorTag.getList(ATTRIBUTES_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < attributesList.size(); i++) {
                CompoundTag attrTag = attributesList.getCompound(i);
                String name = attrTag.getString(ATTRIBUTE_NAME_KEY);
                double value = attrTag.getDouble(ATTRIBUTE_VALUE_KEY);

                try {
                    ArmorAttribute attribute = ArmorAttribute.valueOf(name);
                    attributes.put(attribute, value);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return attributes;
    }

    public static void setAttribute(ItemStack stack, ArmorAttribute attribute, double value) {
        CompoundTag armorTag = getOrCreateArmorTag(stack);
        ListTag attributesList;

        if (armorTag.contains(ATTRIBUTES_KEY)) {
            attributesList = armorTag.getList(ATTRIBUTES_KEY, Tag.TAG_COMPOUND);
        } else {
            attributesList = new ListTag();
        }

        boolean found = false;
        for (int i = 0; i < attributesList.size(); i++) {
            CompoundTag attrTag = attributesList.getCompound(i);
            if (attrTag.getString(ATTRIBUTE_NAME_KEY).equals(attribute.name())) {
                attrTag.putDouble(ATTRIBUTE_VALUE_KEY, value);
                found = true;
                break;
            }
        }

        if (!found) {
            CompoundTag attrTag = new CompoundTag();
            attrTag.putString(ATTRIBUTE_NAME_KEY, attribute.name());
            attrTag.putDouble(ATTRIBUTE_VALUE_KEY, value);
            attributesList.add(attrTag);
        }

        armorTag.put(ATTRIBUTES_KEY, attributesList);
    }

    public static void removeAttribute(ItemStack stack, ArmorAttribute attribute) {
        CompoundTag armorTag = getOrCreateArmorTag(stack);
        if (!armorTag.contains(ATTRIBUTES_KEY)) {
            return;
        }

        ListTag attributesList = armorTag.getList(ATTRIBUTES_KEY, Tag.TAG_COMPOUND);
        for (int i = attributesList.size() - 1; i >= 0; i--) {
            CompoundTag attrTag = attributesList.getCompound(i);
            if (attrTag.getString(ATTRIBUTE_NAME_KEY).equals(attribute.name())) {
                attributesList.remove(i);
                break;
            }
        }

        armorTag.put(ATTRIBUTES_KEY, attributesList);
    }

    public static boolean isArmor(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_KEY);
    }

    public static void initializeArmor(ItemStack stack, WeaponQuality quality) {
        if (isArmor(stack)) {
            return;
        }
        setQuality(stack, quality);
        setLevel(stack, 1);
        setExp(stack, 0);
    }
}
