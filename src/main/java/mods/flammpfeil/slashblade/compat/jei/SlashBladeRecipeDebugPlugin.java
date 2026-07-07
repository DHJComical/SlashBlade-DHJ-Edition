package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.item.crafting.RecipeWrapBlade;
import mods.flammpfeil.slashblade.item.named.Doutanuki;
import mods.flammpfeil.slashblade.util.DummyAnvilRecipe;
import mods.flammpfeil.slashblade.util.DummyRecipeBase;
import mods.flammpfeil.slashblade.util.DummySmeltingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class SlashBladeRecipeDebugPlugin implements IRecipeRegistryPlugin {
    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        logFocus(focus, "categories");

        ItemStack focusStack = getOutputBladeFocus(focus);
        if (focusStack.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> categories = new LinkedHashSet<String>();
        for (Map.Entry<String, IRecipe> entry : SlashBlade.recipeMultimap.entries()) {
            if (matchesFocusOutput(entry.getKey(), entry.getValue(), focusStack)) {
                categories.add(getCategory(entry.getValue()));
            }
        }

        categories.remove("");
        return new java.util.ArrayList<String>(categories);
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> category, IFocus<V> focus) {
        logFocus(focus, "wrappers:" + (category == null ? "<null>" : category.getUid()));

        ItemStack focusStack = getOutputBladeFocus(focus);
        if (focusStack.isEmpty() || category == null) {
            return Collections.emptyList();
        }

        List<T> wrappers = new java.util.ArrayList<T>();
        for (Map.Entry<String, IRecipe> entry : SlashBlade.recipeMultimap.entries()) {
            IRecipe recipe = entry.getValue();
            if (!matchesFocusOutput(entry.getKey(), recipe, focusStack) || !StringUtils.equals(category.getUid(), getCategory(recipe))) {
                continue;
            }
            if (isDefaultJeiRecipeSufficient(recipe, focusStack)) {
                continue;
            }

            IRecipeWrapper wrapper = createWrapper(recipe, focusStack);
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

    private <V> ItemStack getOutputBladeFocus(IFocus<V> focus) {
        if (focus == null || focus.getMode() != IFocus.Mode.OUTPUT || !(focus.getValue() instanceof ItemStack)) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = (ItemStack) focus.getValue();
        return stack.getItem() instanceof ItemSlashBlade ? stack : ItemStack.EMPTY;
    }

    private <V> void logFocus(IFocus<V> focus, String phase) {
        if (focus == null || !(focus.getValue() instanceof ItemStack)) {
            return;
        }

        ItemStack stack = (ItemStack) focus.getValue();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        SlashBladeJeiDebug.logRecipeFocus(phase, focus.getMode().name(), stack, collectBladeKeys(stack), collectRecipeDiagnostics(stack));
    }

    private Set<String> collectBladeKeys(ItemStack stack) {
        Set<String> keys = new LinkedHashSet<String>();
        addKey(keys, BladeIdentity.getBladeId(stack));
        addKey(keys, BladeIdentity.getRawBladeId(stack));

        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (ItemSlashBlade.BladeId.exists(tag)) {
                addKey(keys, ItemSlashBlade.BladeId.get(tag));
            }
            if (ItemSlashBladeNamed.CurrentItemName.exists(tag)) {
                addKey(keys, ItemSlashBladeNamed.CurrentItemName.get(tag));
            }
            if (ItemSlashBladeNamed.TrueItemName.exists(tag)) {
                addKey(keys, ItemSlashBladeNamed.TrueItemName.get(tag));
            }
        }

        if (stack.getItem().getRegistryName() != null) {
            addKey(keys, stack.getItem().getRegistryName().toString());
        }

        return keys;
    }

    private void addKey(Set<String> keys, String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        keys.add(key);
        String slashBladePrefix = SlashBlade.modid + ":";
        if (StringUtils.startsWith(key, slashBladePrefix)) {
            addKey(keys, key.substring(slashBladePrefix.length()));
            return;
        }

        addDisplayBaseKeys(keys, key);

        String lowerKey = key.toLowerCase(Locale.ROOT);
        keys.add(lowerKey);
        if (StringUtils.startsWith(lowerKey, "wrap_")) {
            keys.add(lowerKey.replace('_', '.'));
        }
        if (StringUtils.startsWith(lowerKey, "slashblade_named_")) {
            keys.add("slashblade.named." + lowerKey.substring("slashblade_named_".length()).replace('_', '.'));
        }
    }

    private void addDisplayBaseKeys(Set<String> keys, String key) {
        String[] suffixes = new String[]{
                ".creative",
                ".reqired",
                ".required",
                ".doureqired",
                ".noscabbard",
                ".broken",
                ".directdrop",
                ".damaged",
                ".sample"
        };

        for (String suffix : suffixes) {
            if (StringUtils.endsWith(key, suffix)) {
                addKey(keys, key.substring(0, key.length() - suffix.length()));
                return;
            }
        }
    }

    private boolean matchesFocusOutput(String recipeKey, IRecipe recipe, ItemStack focusStack) {
        if (recipe instanceof Doutanuki.RecipeSheath) {
            return isRustBlade(focusStack) && !isNoScabbard(focusStack) && !isBroken(focusStack);
        }

        if (recipe instanceof Doutanuki.RecipeRepairBrokenBlade) {
            return isRustBlade(focusStack) && !isNoScabbard(focusStack) && !isBroken(focusStack);
        }

        Set<String> focusKeys = collectBladeKeys(focusStack);
        if (containsKey(focusKeys, recipeKey)) {
            return true;
        }

        if (StringUtils.equals(recipeKey, "wrap")) {
            return SlashBladeWrapRecipeWrapper.canCreate(focusStack);
        }

        if (recipe == null) {
            return false;
        }

        ItemStack output = recipe.getRecipeOutput();
        return !output.isEmpty() && intersects(focusKeys, collectBladeKeys(output));
    }

    private boolean isDefaultJeiRecipeSufficient(IRecipe recipe, ItemStack focusStack) {
        if (recipe == null || recipe instanceof DummyRecipeBase || recipe instanceof RecipeWrapBlade) {
            return false;
        }

        if (recipe instanceof Doutanuki.RecipeSheath) {
            return isRustBlade(focusStack) && !isNoScabbard(focusStack) && !isBroken(focusStack);
        }

        if (recipe instanceof Doutanuki.RecipeRepairBrokenBlade) {
            return isRustBlade(focusStack) && !isBroken(focusStack);
        }

        ItemStack output = recipe.getRecipeOutput();
        return !output.isEmpty()
                && output.getItem() == focusStack.getItem()
                && StringUtils.equals(BladeIdentity.getBladeId(output), BladeIdentity.getBladeId(focusStack));
    }

    private List<String> collectRecipeDiagnostics(ItemStack focusStack) {
        Set<String> focusKeys = collectBladeKeys(focusStack);
        Set<String> lines = new LinkedHashSet<String>();
        int recipeCount = 0;
        int directRecipeKeyMatches = 0;
        int outputMatches = 0;

        for (Map.Entry<String, IRecipe> entry : SlashBlade.recipeMultimap.entries()) {
            recipeCount++;
            String recipeKey = entry.getKey();
            IRecipe recipe = entry.getValue();

            boolean directMatch = containsKey(focusKeys, recipeKey);
            boolean outputMatch = false;
            ItemStack output = recipe == null ? ItemStack.EMPTY : recipe.getRecipeOutput();
            if (!output.isEmpty()) {
                outputMatch = intersects(focusKeys, collectBladeKeys(output));
            }

            if (directMatch) {
                directRecipeKeyMatches++;
            }
            if (outputMatch) {
                outputMatches++;
            }

            if (directMatch || outputMatch || shouldAlwaysLog(recipeKey, focusKeys)) {
                lines.add("recipe key=" + recipeKey
                        + " direct=" + directMatch
                        + " output=" + outputMatch
                        + " outputStack=" + SlashBladeJeiDebug.describeStack(output)
                        + " outputKeys=" + collectBladeKeys(output));
            }
        }

        lines.add("recipeMultimap total=" + recipeCount
                + " directRecipeKeyMatches=" + directRecipeKeyMatches
                + " outputMatches=" + outputMatches);
        addFoxDependencyDiagnostics(lines, focusKeys);
        addWrapDiagnostics(lines, focusKeys);
        return new java.util.ArrayList<String>(lines);
    }

    private void addFoxDependencyDiagnostics(Set<String> lines, Set<String> focusKeys) {
        boolean foxFocus = false;
        for (String focusKey : focusKeys) {
            foxFocus = foxFocus || StringUtils.containsIgnoreCase(focusKey, "fox");
        }
        if (!foxFocus) {
            return;
        }

        ItemStack kitunebi = SlashBlade.findItemStack("BambooMod", "kitunebi", 1);
        ItemStack katana = SlashBlade.findItemStack("BambooMod", "katana", 1);
        ItemStack sakuraKitunebi = SlashBlade.findItemStack("sakura", "kitunebi", 1);
        ItemStack sakuraKatana = SlashBlade.findItemStack("sakura", "katana", 1);
        ItemStack sakuraFriedTofu = SlashBlade.findItemStack("sakura", "tofu_fried", 1);
        lines.add("fox dependency BambooMod:kitunebi=" + SlashBladeJeiDebug.describeStack(kitunebi));
        lines.add("fox dependency BambooMod:katana=" + SlashBladeJeiDebug.describeStack(katana));
        lines.add("fox dependency sakura:kitunebi=" + SlashBladeJeiDebug.describeStack(sakuraKitunebi));
        lines.add("fox dependency sakura:katana=" + SlashBladeJeiDebug.describeStack(sakuraKatana));
        lines.add("fox dependency sakura:tofu_fried=" + SlashBladeJeiDebug.describeStack(sakuraFriedTofu));
        if (kitunebi.isEmpty() && sakuraKitunebi.isEmpty()) {
            lines.add("fox recipe status=not registered because no compatible kitunebi item was found");
        }
    }

    private void addWrapDiagnostics(Set<String> lines, Set<String> focusKeys) {
        boolean wrapFocus = false;
        for (String focusKey : focusKeys) {
            wrapFocus = wrapFocus || StringUtils.containsIgnoreCase(focusKey, "wrap");
        }
        if (!wrapFocus) {
            return;
        }

        lines.add("wrapable targets=" + RecipeWrapBlade.wrapableTextureNames);
        for (String focusKey : focusKeys) {
            String normalizedFocusKey = normalizeWrapFocusKey(focusKey);
            if (!StringUtils.startsWith(normalizedFocusKey, "wrap.")) {
                continue;
            }

            for (Map.Entry<String, String> entry : RecipeWrapBlade.wrapableTextureNames.entrySet()) {
                String expectedWrapKey = "wrap." + entry.getKey().replace(':', '.').toLowerCase(Locale.ROOT);
                if (!StringUtils.equals(normalizedFocusKey, expectedWrapKey)) {
                    continue;
                }

                Item target = Item.REGISTRY.getObject(new ResourceLocation(entry.getKey()));
                lines.add("wrap focus matched key=" + expectedWrapKey
                        + " target=" + entry.getKey()
                        + " texture=" + entry.getValue()
                        + " targetStack=" + (target == null ? "<missing item>" : SlashBladeJeiDebug.describeStack(new ItemStack(target))));
            }
        }
    }

    private String normalizeWrapFocusKey(String key) {
        if (StringUtils.isBlank(key)) {
            return "";
        }

        String normalized = key.toLowerCase(Locale.ROOT);
        String slashBladePrefix = SlashBlade.modid.toLowerCase(Locale.ROOT) + ":";
        if (StringUtils.startsWith(normalized, slashBladePrefix)) {
            normalized = normalized.substring(slashBladePrefix.length());
        }
        if (StringUtils.endsWith(normalized, ".sample")) {
            normalized = normalized.substring(0, normalized.length() - ".sample".length());
        }
        return normalized;
    }

    private boolean containsKey(Set<String> keys, String recipeKey) {
        if (StringUtils.isBlank(recipeKey)) {
            return false;
        }

        return keys.contains(recipeKey) || keys.contains(SlashBlade.modid + ":" + recipeKey);
    }

    private boolean intersects(Set<String> left, Set<String> right) {
        for (String value : left) {
            if (right.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldAlwaysLog(String recipeKey, Set<String> focusKeys) {
        if (StringUtils.containsIgnoreCase(recipeKey, "fox") || StringUtils.containsIgnoreCase(recipeKey, "wrap")) {
            return true;
        }
        for (String focusKey : focusKeys) {
            if (StringUtils.containsIgnoreCase(focusKey, "fox") || StringUtils.containsIgnoreCase(focusKey, "wrap")) {
                return true;
            }
        }
        return false;
    }

    private boolean isRustBlade(ItemStack stack) {
        return !stack.isEmpty() && BladeIdentity.matchesIdentity(stack, Doutanuki.name);
    }

    private boolean isNoScabbard(ItemStack stack) {
        return !stack.isEmpty()
                && stack.hasTagCompound()
                && ItemSlashBlade.IsNoScabbard.get(stack.getTagCompound());
    }

    private boolean isBroken(ItemStack stack) {
        return !stack.isEmpty()
                && stack.hasTagCompound()
                && ItemSlashBlade.IsBroken.get(stack.getTagCompound());
    }

    private String getCategory(IRecipe recipe) {
        if (recipe instanceof DummyRecipeBase) {
            DummyRecipeBase dummyRecipe = (DummyRecipeBase) recipe;
            switch (dummyRecipe.getRecipeType()) {
                case Crafting:
                    return VanillaRecipeCategoryUid.CRAFTING;
                case Smelting:
                    return VanillaRecipeCategoryUid.SMELTING;
                case Anvil:
                    return VanillaRecipeCategoryUid.ANVIL;
                default:
                    return "";
            }
        }

        return VanillaRecipeCategoryUid.CRAFTING;
    }

    private IRecipeWrapper createWrapper(IRecipe recipe, ItemStack focusStack) {
        if (recipe instanceof DummyAnvilRecipe) {
            return SlashBladeVanillaWrappers.createAnvilWrapper((DummyAnvilRecipe) recipe);
        }

        if (recipe instanceof DummySmeltingRecipe) {
            return SlashBladeVanillaWrappers.createSmeltingWrapper((DummySmeltingRecipe) recipe);
        }

        if (recipe instanceof RecipeWrapBlade) {
            return SlashBladeWrapRecipeWrapper.create(focusStack);
        }

        return new SlashBladeCraftingRecipeWrapper(recipe, focusStack, true);
    }
}
