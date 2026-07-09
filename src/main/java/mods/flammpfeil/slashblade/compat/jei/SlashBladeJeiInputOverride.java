package mods.flammpfeil.slashblade.compat.jei;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public interface SlashBladeJeiInputOverride {
    ItemStack slashblade$getJeiInputOverride(int x, int y, Ingredient ingredient);

    default ItemStack slashblade$getJeiInheritanceSource() {
        return ItemStack.EMPTY;
    }
}
