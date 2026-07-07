package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.crafting.RecipeAwakeBlade;
import mods.flammpfeil.slashblade.item.named.Doutanuki;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@JEIPlugin
public class SlashBladeJeiPlugin extends BlankModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        registerSubtypes(subtypeRegistry);
    }

    @Override
    public void registerSubtypes(ISubtypeRegistry subtypeRegistry) {
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
            public IRecipeWrapper getRecipeWrapper(RecipeAwakeBlade recipe) {
                return new SlashBladeCraftingRecipeWrapper(recipe, recipe.getRecipeOutput());
            }
        }, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(Doutanuki.RecipeSheath.class, new IRecipeWrapperFactory<Doutanuki.RecipeSheath>() {
            @Override
            public IRecipeWrapper getRecipeWrapper(Doutanuki.RecipeSheath recipe) {
                return new SlashBladeCraftingRecipeWrapper(recipe, recipe.getRecipeOutput());
            }
        }, VanillaRecipeCategoryUid.CRAFTING);
        registry.handleRecipes(Doutanuki.RecipeRepairBrokenBlade.class, new IRecipeWrapperFactory<Doutanuki.RecipeRepairBrokenBlade>() {
            @Override
            public IRecipeWrapper getRecipeWrapper(Doutanuki.RecipeRepairBrokenBlade recipe) {
                return new SlashBladeCraftingRecipeWrapper(recipe, recipe.getRecipeOutput());
            }
        }, VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeRegistryPlugin(new SlashBladeRecipeDebugPlugin());
        SlashBladeJeiDebug.log("Registered SlashBlade crafting recipe wrappers");
        SlashBladeJeiDebug.log("Registered SlashBlade recipe debug plugin");
    }
}
