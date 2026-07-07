package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.plugins.vanilla.anvil.AnvilRecipeWrapper;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipe;
import mods.flammpfeil.slashblade.util.DummyAnvilRecipe;
import mods.flammpfeil.slashblade.util.DummySmeltingRecipe;

import java.util.Collections;

final class SlashBladeVanillaWrappers {
    private SlashBladeVanillaWrappers() {
    }

    static AnvilRecipeWrapper createAnvilWrapper(DummyAnvilRecipe recipe) {
        return new AnvilRecipeWrapper(Collections.singletonList(recipe.left),
                Collections.singletonList(recipe.right),
                Collections.singletonList(recipe.getRecipeOutput()));
    }

    static SmeltingRecipe createSmeltingWrapper(DummySmeltingRecipe recipe) {
        return new SmeltingRecipe(Collections.singletonList(recipe.input), recipe.getRecipeOutput());
    }
}
