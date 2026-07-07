package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.ISubtypeRegistry;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

public final class SlashBladeSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {
    public static final SlashBladeSubtypeInterpreter INSTANCE = new SlashBladeSubtypeInterpreter();

    private SlashBladeSubtypeInterpreter() {
    }

    @Override
    public String apply(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSlashBlade)) {
            return NONE;
        }

        String bladeId = BladeIdentity.getBladeId(stack);
        String subtype;
        if (!StringUtils.isBlank(bladeId)) {
            subtype = "bladeid:" + bladeId;
        } else {
            ResourceLocation registryName = stack.getItem().getRegistryName();
            subtype = registryName != null ? "registry:" + registryName.toString() : NONE;
        }

        subtype = subtype + getStateKey(stack);
        SlashBladeJeiDebug.logSubtype(stack, subtype);
        return subtype;
    }

    private String getStateKey(ItemStack stack) {
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : null;
        if (tag == null) {
            return "|state:none";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("|broken:").append(ItemSlashBlade.IsBroken.get(tag));
        builder.append("|noscabbard:").append(ItemSlashBlade.IsNoScabbard.get(tag));
        builder.append("|sealed:").append(ItemSlashBlade.IsSealed.get(tag));
        builder.append("|defaultbewitched:").append(ItemSlashBladeNamed.IsDefaultBewitched.get(tag));
        builder.append("|repair:").append(ItemSlashBlade.RepairCount.get(tag));
        builder.append("|kill:").append(ItemSlashBlade.KillCount.get(tag));
        builder.append("|proud:").append(ItemSlashBlade.ProudSoul.get(tag));
        builder.append("|enchanted:").append(stack.isItemEnchanted());
        builder.append("|named:").append(stack.hasDisplayName());
        return builder.toString();
    }
}
