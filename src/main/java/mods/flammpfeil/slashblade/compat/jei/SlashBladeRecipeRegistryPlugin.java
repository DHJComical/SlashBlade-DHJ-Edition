package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class SlashBladeRecipeRegistryPlugin implements IRecipeRegistryPlugin {
    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        ItemStack focusStack = SlashBladeRecipeLookup.getOutputBladeFocus(focus);
        if (focusStack.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> categories = new LinkedHashSet<String>();
        for (Map.Entry<String, IRecipe> entry : SlashBlade.recipeMultimap.entries()) {
            if (SlashBladeRecipeLookup.matchesFocusOutput(entry.getKey(), entry.getValue(), focusStack)) {
                categories.add(SlashBladeRecipeLookup.getCategory(entry.getValue()));
            }
        }

        categories.remove("");
        return new java.util.ArrayList<String>(categories);
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> category, IFocus<V> focus) {
        ItemStack focusStack = SlashBladeRecipeLookup.getOutputBladeFocus(focus);
        if (focusStack.isEmpty() || category == null) {
            return Collections.emptyList();
        }

        List<T> wrappers = new java.util.ArrayList<T>();
        for (Map.Entry<String, IRecipe> entry : SlashBlade.recipeMultimap.entries()) {
            IRecipe recipe = entry.getValue();
            if (!SlashBladeRecipeLookup.matchesFocusOutput(entry.getKey(), recipe, focusStack)
                    || !StringUtils.equals(category.getUid(), SlashBladeRecipeLookup.getCategory(recipe))) {
                continue;
            }
            if (SlashBladeRecipeLookup.isDefaultJeiRecipeSufficient(recipe, focusStack)) {
                continue;
            }

            IRecipeWrapper wrapper = SlashBladeRecipeLookup.createWrapper(recipe, focusStack);
            if (wrapper != null) {
                wrappers.add((T) wrapper);
            }
        }

        return wrappers;
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> category) {
        return Collections.emptyList();
    }
}
