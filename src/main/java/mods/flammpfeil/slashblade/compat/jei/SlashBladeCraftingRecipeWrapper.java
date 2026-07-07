package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.crafting.BladeIngredient;
import mods.flammpfeil.slashblade.item.crafting.RecipeAwakeBlade;
import mods.flammpfeil.slashblade.item.crafting.RequestDefinition;
import mods.flammpfeil.slashblade.item.named.Doutanuki;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class SlashBladeCraftingRecipeWrapper implements IShapedCraftingRecipeWrapper {
    private final IRecipe recipe;
    private final ItemStack output;

    SlashBladeCraftingRecipeWrapper(IRecipe recipe, ItemStack output) {
        this(recipe, output, false);
    }

    SlashBladeCraftingRecipeWrapper(IRecipe recipe, ItemStack output, boolean preserveOutputState) {
        this.recipe = recipe;
        this.output = getDisplayOutput(recipe, output, preserveOutputState);
        removeRecipeOnlyTooltip(this.output);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        java.util.ArrayList<List<ItemStack>> inputs = new java.util.ArrayList<List<ItemStack>>();
        int width = Math.max(1, getWidth());
        int index = 0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            int x = index % width;
            int y = index / width;
            ItemStack[] stacks = cleanDisplayStacks(getDisplayStacks(ingredient, x, y));
            inputs.add(stacks.length == 0 ? Collections.<ItemStack>emptyList() : Arrays.asList(stacks));
            index++;
        }

        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }

    private ItemStack[] getDisplayStacks(Ingredient ingredient, int x, int y) {
        ItemStack overrideStack = getInputOverrideStack(ingredient, x, y);
        if (!overrideStack.isEmpty()) {
            return new ItemStack[]{overrideStack};
        }

        if (ingredient instanceof BladeIngredient) {
            ItemStack[] rustBladeStacks = getRustBladeDisplayStacks();
            if (0 < rustBladeStacks.length) {
                return rustBladeStacks;
            }
        }

        if (recipe instanceof RecipeAwakeBlade && ingredient instanceof BladeIngredient) {
            ItemStack[] awakeStacks = getAwakeBladeDisplayStacks((RecipeAwakeBlade) recipe);
            if (0 < awakeStacks.length) {
                return awakeStacks;
            }
        }

        if (ingredient instanceof BladeIngredient) {
            ItemStack[] bladeStacks = getBladeIngredientDisplayStacks((BladeIngredient) ingredient);
            if (0 < bladeStacks.length) {
                return bladeStacks;
            }
        }

        return ingredient.getMatchingStacks();
    }

    private ItemStack getInputOverrideStack(Ingredient ingredient, int x, int y) {
        if (!(recipe instanceof SlashBladeJeiInputOverride)) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = ((SlashBladeJeiInputOverride) recipe).slashblade$getJeiInputOverride(x, y, ingredient);
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack normalized = normalizeBladeDisplayStack(stack.copy());
        SlashBladeJeiDebug.log("Recipe input override recipe=" + getRegistryName()
                + " xy=" + x + "," + y
                + " stack=" + SlashBladeJeiDebug.describeStack(normalized));
        return normalized;
    }

    private ItemStack[] cleanDisplayStacks(ItemStack[] stacks) {
        if (stacks.length == 0) {
            return stacks;
        }

        ItemStack[] cleanedStacks = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            if (stack == null || stack.isEmpty()) {
                cleanedStacks[i] = ItemStack.EMPTY;
                continue;
            }

            ItemStack cleanedStack = stack.copy();
            removeRecipeOnlyTooltip(cleanedStack);
            cleanedStacks[i] = cleanedStack;
        }

        return cleanedStacks;
    }

    private ItemStack[] getRustBladeDisplayStacks() {
        if (recipe instanceof Doutanuki.RecipeSheath) {
            return new ItemStack[]{createRustBlade(false, true, 0)};
        }

        if (recipe instanceof Doutanuki.RecipeRepairBrokenBlade) {
            return new ItemStack[]{createRustBlade(true, isNoScabbard(output), 1)};
        }

        if (recipe instanceof Doutanuki.RecipeDoutanuki) {
            ItemStack required = createRustBlade(false, false, 5);
            NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(required);
            ItemSlashBlade.ProudSoul.set(tag, Math.max(ItemSlashBlade.ProudSoul.get(tag), 1000));
            ItemSlashBlade.KillCount.set(tag, Math.max(ItemSlashBlade.KillCount.get(tag), 100));
            return new ItemStack[]{required};
        }

        return new ItemStack[0];
    }

    private ItemStack[] getAwakeBladeDisplayStacks(RecipeAwakeBlade awakeRecipe) {
        ItemStack required = awakeRecipe.getRequiredStateBlade();
        if (required.isEmpty()) {
            required = awakeRecipe.getRequestDefinition().createDisplayStack();
        } else {
            required = normalizeBladeDisplayStack(required);
            awakeRecipe.getRequestDefinition().initItemStack(required);
        }

        if (required.isEmpty()) {
            return new ItemStack[0];
        }

        removeRecipeOnlyTooltip(required);

        ItemStack named = required.copy();
        named.setStackDisplayName(required.getDisplayName());

        return new ItemStack[]{required, named};
    }

    private ItemStack[] getBladeIngredientDisplayStacks(BladeIngredient ingredient) {
        RequestDefinition request = ingredient.getRequest();
        ItemStack[] matchingStacks = ingredient.getMatchingStacks();
        java.util.ArrayList<ItemStack> result = new java.util.ArrayList<ItemStack>();

        if (matchingStacks != null) {
            for (ItemStack matchingStack : matchingStacks) {
                if (matchingStack == null || matchingStack.isEmpty()) {
                    continue;
                }

                ItemStack displayStack = normalizeBladeDisplayStack(matchingStack.copy());
                request.initItemStack(displayStack);
                result.add(displayStack);
            }
        }

        if (result.isEmpty()) {
            ItemStack fallback = normalizeBladeDisplayStack(request.createDisplayStack());
            if (!fallback.isEmpty()) {
                result.add(fallback);
            }
        }

        return result.toArray(new ItemStack[result.size()]);
    }

    private ItemStack normalizeBladeDisplayStack(ItemStack stack) {
        stack = normalizeCoreBladeDisplayStack(stack);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemSlashBlade && stack.getItemDamage() == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE) {
            stack.setItemDamage(0);
        }
        return stack;
    }

    private ItemStack normalizeCoreBladeDisplayStack(ItemStack stack) {
        if (stack.isEmpty() || SlashBlade.weapon == null || stack.getItem() == SlashBlade.weapon) {
            return stack;
        }

        if (!StringUtils.equals(String.valueOf(SlashBlade.weapon.getRegistryName()), BladeIdentity.getBladeId(stack))) {
            return stack;
        }

        ItemStack normalized = new ItemStack(SlashBlade.weapon, stack.getCount(), Math.min(stack.getItemDamage(), SlashBlade.weapon.getMaxDamage()));
        if (stack.hasTagCompound()) {
            normalized.setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
        }
        return normalized;
    }

    private static void removeRecipeOnlyTooltip(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }

        stack.getTagCompound().removeTag(ItemSlashBlade.TooltipKeysTag);
    }

    private static ItemStack getDisplayOutput(IRecipe recipe, ItemStack output, boolean preserveOutputState) {
        if (recipe instanceof Doutanuki.RecipeSheath) {
            return createRustBlade(false, false, 0);
        }

        if (recipe instanceof Doutanuki.RecipeRepairBrokenBlade) {
            return createRustBlade(false, preserveOutputState && isNoScabbard(output), 1);
        }

        return output.isEmpty() ? ItemStack.EMPTY : output.copy();
    }

    private static ItemStack createRustBlade(boolean broken, boolean noScabbard, int repairCount) {
        ItemStack blade = SlashBlade.createBladeStack(Doutanuki.name);
        if (blade.isEmpty()) {
            blade = SlashBlade.getCustomBlade(SlashBlade.modid, Doutanuki.name);
        }
        if (blade.isEmpty()) {
            return ItemStack.EMPTY;
        }

        blade = blade.copy();
        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(blade);
        ItemSlashBlade.IsBroken.set(tag, broken);
        ItemSlashBlade.IsNoScabbard.set(tag, noScabbard);
        ItemSlashBlade.RepairCount.set(tag, Math.max(0, repairCount));
        ItemSlashBlade.ProudSoul.set(tag, 0);
        ItemSlashBlade.KillCount.set(tag, 0);

        if (broken) {
            blade.setItemDamage(Math.max(0, blade.getMaxDamage() - 1));
        } else {
            blade.setItemDamage(0);
        }

        return blade;
    }

    private static boolean isNoScabbard(ItemStack stack) {
        return !stack.isEmpty()
                && stack.hasTagCompound()
                && ItemSlashBlade.IsNoScabbard.get(stack.getTagCompound());
    }

    @Override
    public int getWidth() {
        if (recipe instanceof ShapedOreRecipe) {
            return ((ShapedOreRecipe) recipe).getWidth();
        }
        if (recipe instanceof ShapedRecipes) {
            return ((ShapedRecipes) recipe).getWidth();
        }
        if (recipe instanceof IShapedRecipe) {
            return ((IShapedRecipe) recipe).getRecipeWidth();
        }
        return 0;
    }

    @Override
    public int getHeight() {
        if (recipe instanceof ShapedOreRecipe) {
            return ((ShapedOreRecipe) recipe).getHeight();
        }
        if (recipe instanceof ShapedRecipes) {
            return ((ShapedRecipes) recipe).getHeight();
        }
        if (recipe instanceof IShapedRecipe) {
            return ((IShapedRecipe) recipe).getRecipeHeight();
        }
        return 0;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getRegistryName();
    }
}
