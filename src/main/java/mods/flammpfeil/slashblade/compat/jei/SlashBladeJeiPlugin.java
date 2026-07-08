package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeWrapper;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.crafting.RecipeWrapBlade;
import mods.flammpfeil.slashblade.item.crafting.RecipeAwakeBlade;
import mods.flammpfeil.slashblade.item.named.Doutanuki;
import mods.flammpfeil.slashblade.item.named.RecipeAwakeBladeFox;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Locale;
import java.util.List;
import java.util.Map;

@JEIPlugin
public class SlashBladeJeiPlugin extends BlankModPlugin {
    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistry subtypeRegistry) {
        registerSubtypes(subtypeRegistry);
    }

    @Override
    public void registerSubtypes(@Nonnull ISubtypeRegistry subtypeRegistry) {
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item instanceof ItemSlashBlade) {
                subtypeRegistry.registerSubtypeInterpreter(item, SlashBladeSubtypeInterpreter.INSTANCE);
            }
        }

        SlashBladeJeiDebug.log("Registered SlashBlade subtype interpreters");
    }

    @Override
    public void register(IModRegistry registry) {
        registry.handleRecipes(RecipeAwakeBlade.class, new IRecipeWrapperFactory<RecipeAwakeBlade>() {
            @Override
            public IRecipeWrapper getRecipeWrapper(@Nonnull RecipeAwakeBlade recipe) {
                return new SlashBladeCraftingRecipeWrapper(recipe, recipe.getRecipeOutput());
            }
        }, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(RecipeAwakeBladeFox.class, new IRecipeWrapperFactory<RecipeAwakeBladeFox>() {
            @Override
            public IRecipeWrapper getRecipeWrapper(@Nonnull RecipeAwakeBladeFox recipe) {
                return new SlashBladeCraftingRecipeWrapper(recipe, recipe.getRecipeOutput());
            }
        }, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(Doutanuki.RecipeSheath.class, new IRecipeWrapperFactory<Doutanuki.RecipeSheath>() {
            @Override
            public IRecipeWrapper getRecipeWrapper(@Nonnull Doutanuki.RecipeSheath recipe) {
                return new SlashBladeCraftingRecipeWrapper(recipe, recipe.getRecipeOutput());
            }
        }, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(Doutanuki.RecipeRepairBrokenBlade.class, new IRecipeWrapperFactory<Doutanuki.RecipeRepairBrokenBlade>() {
            @Override
            public IRecipeWrapper getRecipeWrapper(@Nonnull Doutanuki.RecipeRepairBrokenBlade recipe) {
                return new SlashBladeCraftingRecipeWrapper(recipe, recipe.getRecipeOutput());
            }
        }, VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeRegistryPlugin(new SlashBladeRecipeRegistryPlugin());
        registry.addRecipeRegistryPlugin(new SlashBladeRecipeDebugPlugin());
        SlashBladeJeiDebug.log("Registered SlashBlade crafting recipe wrappers");
        SlashBladeJeiDebug.log("Registered SlashBlade recipe registry plugin");
        SlashBladeJeiDebug.log("Registered SlashBlade recipe debug logger");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        hideDefaultCustomWrappedCraftingRecipes(jeiRuntime.getRecipeRegistry());
        hideUnavailableGeneratedWrapAnvilRecipes(jeiRuntime.getRecipeRegistry());
    }

    private void hideDefaultCustomWrappedCraftingRecipes(IRecipeRegistry recipeRegistry) {
        IRecipeCategory craftingCategory = recipeRegistry.getRecipeCategory(VanillaRecipeCategoryUid.CRAFTING);
        if (craftingCategory == null) {
            return;
        }

        for (Map.Entry<String, IRecipe> entry : SlashBlade.recipeMultimap.entries()) {
            IRecipe recipe = entry.getValue();
            if (!hasCustomCraftingWrapper(recipe)) {
                continue;
            }

            ItemStack output = recipe.getRecipeOutput();
            if (output.isEmpty()) {
                continue;
            }

            List<IRecipeWrapper> wrappers = recipeRegistry.getRecipeWrappers(
                    craftingCategory,
                    recipeRegistry.createFocus(mezz.jei.api.recipe.IFocus.Mode.OUTPUT, output)
            );

            for (IRecipeWrapper wrapper : wrappers) {
                if (wrapper instanceof SlashBladeCraftingRecipeWrapper) {
                    continue;
                }

                if (!hasMatchingOutput(wrapper, output)) {
                    continue;
                }

                recipeRegistry.hideRecipe(wrapper, VanillaRecipeCategoryUid.CRAFTING);
                SlashBladeJeiDebug.log("Hid default SlashBlade custom-wrapped crafting recipe: "
                        + recipe.getRegistryName()
                        + " wrapper=" + wrapper.getClass().getName());
            }
        }
    }

    private boolean hasCustomCraftingWrapper(IRecipe recipe) {
        return recipe instanceof RecipeAwakeBlade
                || recipe instanceof RecipeAwakeBladeFox
                || recipe instanceof Doutanuki.RecipeSheath
                || recipe instanceof Doutanuki.RecipeRepairBrokenBlade
                || recipe instanceof SlashBladeJeiInputOverride;
    }

    private boolean hasMatchingOutput(IRecipeWrapper wrapper, ItemStack expectedOutput) {
        IIngredients ingredients = new mezz.jei.ingredients.Ingredients();
        wrapper.getIngredients(ingredients);

        List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);
        if (outputs == null) {
            return false;
        }

        for (List<ItemStack> outputList : outputs) {
            for (ItemStack output : outputList == null ? Collections.<ItemStack>emptyList() : outputList) {
                if (BladeIdentity.matchesIdentity(output, BladeIdentity.getBladeId(expectedOutput))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void hideUnavailableGeneratedWrapAnvilRecipes(IRecipeRegistry recipeRegistry) {
        IRecipeCategory anvilCategory = recipeRegistry.getRecipeCategory(VanillaRecipeCategoryUid.ANVIL);
        if (anvilCategory == null) {
            return;
        }

        for (Map.Entry<String, String> entry : RecipeWrapBlade.wrapableTextureNames.entrySet()) {
            String wrapBladeId = "wrap." + entry.getKey().replace(':', '.').toLowerCase(Locale.ROOT);
            if (RecipeWrapBlade.hasAvailableWrapTarget(wrapBladeId)) {
                continue;
            }

            hideUnavailableGeneratedWrapAnvilRecipes(recipeRegistry, anvilCategory, wrapBladeId);
            hideUnavailableGeneratedWrapAnvilRecipes(recipeRegistry, anvilCategory, wrapBladeId + ".sample");
        }
    }

    private void hideUnavailableGeneratedWrapAnvilRecipes(IRecipeRegistry recipeRegistry, IRecipeCategory anvilCategory, String bladeId) {
        ItemStack focus = SlashBlade.createBladeStack(bladeId);
        if (focus.isEmpty()) {
            return;
        }

        List<IRecipeWrapper> wrappers = recipeRegistry.getRecipeWrappers(anvilCategory, recipeRegistry.createFocus(mezz.jei.api.recipe.IFocus.Mode.OUTPUT, focus));
        for (IRecipeWrapper wrapper : wrappers) {
            if (!(wrapper instanceof AnvilRecipeWrapper) || !hasUnavailableGeneratedWrapOutput(wrapper)) {
                continue;
            }

            recipeRegistry.hideRecipe(wrapper, VanillaRecipeCategoryUid.ANVIL);
            SlashBladeJeiDebug.log("Hid unavailable generated wrap anvil recipe: " + bladeId);
        }
    }

    private boolean hasUnavailableGeneratedWrapOutput(IRecipeWrapper wrapper) {
        IIngredients ingredients = new mezz.jei.ingredients.Ingredients();
        wrapper.getIngredients(ingredients);

        List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);
        if (outputs == null) {
            return false;
        }

        for (List<ItemStack> outputList : outputs) {
            for (ItemStack output : outputList == null ? Collections.<ItemStack>emptyList() : outputList) {
                if (isUnavailableGeneratedWrapStack(output)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isUnavailableGeneratedWrapStack(ItemStack stack) {
        String bladeId = BladeIdentity.getBladeId(stack);
        return RecipeWrapBlade.isRegisteredWrapBladeId(bladeId) && !RecipeWrapBlade.hasAvailableWrapTarget(bladeId);
    }
}
