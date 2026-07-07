package mods.flammpfeil.slashblade.compat.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.item.crafting.RecipeWrapBlade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class SlashBladeWrapRecipeWrapper implements IShapedCraftingRecipeWrapper {
    private final String targetKey;
    private final ItemStack target;
    private final ItemStack output;

    private SlashBladeWrapRecipeWrapper(String targetKey, ItemStack target, ItemStack output) {
        this.targetKey = targetKey;
        this.target = target.copy();
        this.output = output.copy();
    }

    static boolean canCreate(ItemStack focusStack) {
        WrappedTarget target = findWrappedTarget(focusStack);
        return target != null && !target.stack.isEmpty();
    }

    static SlashBladeWrapRecipeWrapper create(ItemStack focusStack) {
        WrappedTarget target = findWrappedTarget(focusStack);
        if (target == null || target.stack.isEmpty()) {
            return null;
        }

        return new SlashBladeWrapRecipeWrapper(target.registryKey, target.stack, focusStack);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = Arrays.asList(
                Collections.<ItemStack>emptyList(),
                Collections.<ItemStack>emptyList(),
                Collections.singletonList(SlashBlade.findItemStack(SlashBlade.modid, SlashBlade.ProudSoulStr, 1)),
                Collections.<ItemStack>emptyList(),
                Collections.singletonList(SlashBlade.findItemStack(SlashBlade.modid, "slashbladeWrapper", 1)),
                Collections.<ItemStack>emptyList(),
                Collections.singletonList(target),
                Collections.<ItemStack>emptyList(),
                Collections.<ItemStack>emptyList()
        );

        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(SlashBlade.modid, "jei_wrap_" + targetKey.replace(':', '_'));
    }

    private static WrappedTarget findWrappedTarget(ItemStack focusStack) {
        Set<String> focusKeys = collectWrapKeys(focusStack);

        for (Map.Entry<String, String> entry : RecipeWrapBlade.wrapableTextureNames.entrySet()) {
            String wrapKey = "wrap." + entry.getKey().replace(':', '.').toLowerCase(Locale.ROOT);
            if (!focusKeys.contains(wrapKey)) {
                continue;
            }

            Item item = Item.REGISTRY.getObject(new ResourceLocation(entry.getKey()));
            return item == null ? null : new WrappedTarget(entry.getKey().toLowerCase(Locale.ROOT), new ItemStack(item));
        }

        if (focusStack.hasTagCompound() && ItemSlashBlade.TextureName.exists(focusStack.getTagCompound())) {
            String texture = ItemSlashBlade.TextureName.get(focusStack.getTagCompound());
            for (Map.Entry<String, String> entry : RecipeWrapBlade.wrapableTextureNames.entrySet()) {
                if (!StringUtils.equals(texture, entry.getValue())) {
                    continue;
                }

                Item item = Item.REGISTRY.getObject(new ResourceLocation(entry.getKey()));
                return item == null ? null : new WrappedTarget(entry.getKey().toLowerCase(Locale.ROOT), new ItemStack(item));
            }
        }

        return null;
    }

    private static Set<String> collectWrapKeys(ItemStack stack) {
        Set<String> keys = new LinkedHashSet<String>();
        addWrapKey(keys, BladeIdentity.getBladeId(stack));
        addWrapKey(keys, BladeIdentity.getRawBladeId(stack));

        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (ItemSlashBladeNamed.CurrentItemName.exists(tag)) {
                addWrapKey(keys, ItemSlashBladeNamed.CurrentItemName.get(tag));
            }
            if (ItemSlashBladeNamed.TrueItemName.exists(tag)) {
                addWrapKey(keys, ItemSlashBladeNamed.TrueItemName.get(tag));
            }
        }

        if (stack.getItem().getRegistryName() != null) {
            addWrapKey(keys, stack.getItem().getRegistryName().toString());
        }

        return keys;
    }

    private static void addWrapKey(Set<String> keys, String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        String normalized = key.toLowerCase(Locale.ROOT);
        String prefix = SlashBlade.modid.toLowerCase(Locale.ROOT) + ":";
        if (normalized.startsWith(prefix)) {
            normalized = normalized.substring(prefix.length());
        }
        if (normalized.endsWith(".sample")) {
            normalized = normalized.substring(0, normalized.length() - ".sample".length());
        }

        keys.add(normalized);
        if (normalized.startsWith("wrap_")) {
            keys.add(normalized.replace('_', '.'));
        }
    }

    private static final class WrappedTarget {
        private final String registryKey;
        private final ItemStack stack;

        private WrappedTarget(String registryKey, ItemStack stack) {
            this.registryKey = registryKey;
            this.stack = stack;
        }
    }
}
