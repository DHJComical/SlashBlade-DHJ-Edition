package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.util.ResourceLocationRaw;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

public final class BladeIdentity {
    private BladeIdentity() {
    }

    public static String getBladeId(ItemStack stack) {
        String bladeId = getRawBladeId(stack);
        if (StringUtils.isBlank(bladeId)) {
            return bladeId;
        }

        return BladeDefinitionRegistry.resolveAlias(bladeId);
    }

    public static String getBladeId(NBTTagCompound tag) {
        String bladeId = getRawBladeId(tag);
        if (StringUtils.isBlank(bladeId)) {
            return bladeId;
        }

        return BladeDefinitionRegistry.resolveAlias(bladeId);
    }

    public static String getRawBladeId(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }

        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (ItemSlashBlade.BladeId.exists(tag)) {
                String bladeId = ItemSlashBlade.BladeId.get(tag);
                if (!StringUtils.isBlank(bladeId)) {
                    return bladeId;
                }
            }

            if (ItemSlashBladeNamed.CurrentItemName.exists(tag)) {
                String legacyBladeName = ItemSlashBladeNamed.CurrentItemName.get(tag);
                if (!StringUtils.isBlank(legacyBladeName)) {
                    return legacyBladeName;
                }
            }
        }

        ResourceLocation registryName = stack.getItem().getRegistryName();
        return registryName != null ? registryName.toString() : "";
    }

    public static String getRawBladeId(NBTTagCompound tag) {
        if (tag == null) {
            return "";
        }

        if (ItemSlashBlade.BladeId.exists(tag)) {
            String bladeId = ItemSlashBlade.BladeId.get(tag);
            if (!StringUtils.isBlank(bladeId)) {
                return bladeId;
            }
        }

        if (ItemSlashBladeNamed.CurrentItemName.exists(tag)) {
            String legacyBladeName = ItemSlashBladeNamed.CurrentItemName.get(tag);
            if (!StringUtils.isBlank(legacyBladeName)) {
                return legacyBladeName;
            }
        }

        return "";
    }

    public static void ensureBladeId(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        String fallbackBladeId = getRawBladeId(stack);
        if (StringUtils.isBlank(fallbackBladeId)) {
            return;
        }

        if (!(stack.getItem() instanceof ItemSlashBlade) && !stack.hasTagCompound()) {
            return;
        }

        ensureBladeId(stack, fallbackBladeId);
    }

    public static void ensureBladeId(ItemStack stack, String fallbackBladeId) {
        if (stack.isEmpty() || StringUtils.isBlank(fallbackBladeId)) {
            return;
        }

        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(stack);
        if (!ItemSlashBlade.BladeId.exists(tag) || StringUtils.isBlank(ItemSlashBlade.BladeId.get(tag))) {
            ItemSlashBlade.BladeId.set(tag, fallbackBladeId);
        }
    }

    public static void ensureRegisteredBladeId(ItemStack stack, String modid, String name) {
        if (stack.isEmpty()) {
            return;
        }

        String fallbackBladeId = getRawBladeId(stack);
        if (StringUtils.isBlank(fallbackBladeId)) {
            String resolvedAlias = BladeDefinitionRegistry.resolveAlias(name);
            if (!StringUtils.isBlank(resolvedAlias) && !StringUtils.equals(resolvedAlias, name)) {
                fallbackBladeId = resolvedAlias;
            }
        }
        if (StringUtils.isBlank(fallbackBladeId)) {
            if (!(stack.getItem() instanceof ItemSlashBlade) && !stack.hasTagCompound()) {
                return;
            }
            fallbackBladeId = new ResourceLocationRaw(modid, name).toString();
        }

        ensureBladeId(stack, fallbackBladeId);
    }

    public static void copyBladeId(NBTTagCompound dest, NBTTagCompound src) {
        if (dest == null || src == null) {
            return;
        }

        if (ItemSlashBlade.BladeId.exists(src) && !StringUtils.isBlank(ItemSlashBlade.BladeId.get(src))) {
            ItemSlashBlade.BladeId.set(dest, BladeDefinitionRegistry.resolveAlias(ItemSlashBlade.BladeId.get(src)));
        } else if (ItemSlashBladeNamed.CurrentItemName.exists(src)) {
            String legacyBladeName = ItemSlashBladeNamed.CurrentItemName.get(src);
            if (!StringUtils.isBlank(legacyBladeName)) {
                ItemSlashBlade.BladeId.set(dest, BladeDefinitionRegistry.resolveAlias(legacyBladeName));
            }
        }
    }

    public static void syncBladeId(NBTTagCompound tag) {
        if (tag == null) {
            return;
        }

        if (ItemSlashBladeNamed.CurrentItemName.exists(tag)) {
            String legacyBladeName = ItemSlashBladeNamed.CurrentItemName.get(tag);
            if (!StringUtils.isBlank(legacyBladeName)) {
                ItemSlashBlade.BladeId.set(tag, BladeDefinitionRegistry.resolveAlias(legacyBladeName));
            }
        }
    }

    public static boolean matchesIdentity(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        return StringUtils.equals(getBladeId(left), getBladeId(right));
    }

    public static boolean matchesIdentity(ItemStack stack, String bladeId) {
        if (stack.isEmpty() || StringUtils.isBlank(bladeId)) {
            return false;
        }

        return StringUtils.equals(getBladeId(stack), bladeId);
    }

    public static boolean matchesIdentity(NBTTagCompound tag, String bladeId) {
        if (tag == null || StringUtils.isBlank(bladeId)) {
            return false;
        }

        return StringUtils.equals(getBladeId(tag), bladeId);
    }
}
