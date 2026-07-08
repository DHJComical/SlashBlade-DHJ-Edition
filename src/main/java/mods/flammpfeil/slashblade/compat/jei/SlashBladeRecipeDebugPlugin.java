package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

final class SlashBladeRecipeDebugPlugin implements IRecipeRegistryPlugin {
    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        logFocus(focus, "categories");
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> category, IFocus<V> focus) {
        logFocus(focus, "wrappers:" + (category == null ? "<null>" : category.getUid()));
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> category) {
        return Collections.emptyList();
    }

    private <V> void logFocus(IFocus<V> focus, String phase) {
        ItemStack stack = SlashBladeRecipeLookup.getOutputBladeFocus(focus);
        if (stack.isEmpty()) {
            return;
        }

        SlashBladeJeiDebug.logRecipeFocus(
                phase,
                focus.getMode().name(),
                stack,
                SlashBladeRecipeLookup.collectBladeKeys(stack),
                SlashBladeRecipeLookup.collectRecipeDiagnostics(stack));
    }
}
