package mods.flammpfeil.slashblade.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Furia on 2017/09/30.
 */
public class BladeIngredient extends Ingredient {
    private final RequestDefinition request;

    public BladeIngredient(ItemStack stack){
        this(stack, RequestDefinition.fromBlade(stack));
    }

    public BladeIngredient(ItemStack stack, RequestDefinition request) {
        super(createDisplayStacks(new ItemStack[]{stack}, request));
        this.request = request == null ? RequestDefinition.empty() : request;
    }

    public BladeIngredient(RequestDefinition request) {
        super(createRequestStacks(request));
        this.request = request == null ? RequestDefinition.empty() : request;
    }

    public static BladeIngredient of(ItemStack stack) {
        return new BladeIngredient(stack);
    }

    public static BladeIngredient of(RequestDefinition request) {
        return new BladeIngredient(request);
    }

    public static BladeIngredient of(ItemStack stack, RequestDefinition request) {
        return new BladeIngredient(stack, request);
    }

    public RequestDefinition getRequest() {
        return request;
    }

    @Override
    public boolean apply(@Nullable ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        if (!(input.getItem() instanceof ItemSlashBlade)) {
            return false;
        }

        return request.test(input);
    }

    public IntList getValidItemStacksPacked()
    {
        IntList matchingStacksPacked = new IntArrayList(1);

        for (ItemStack itemstack : this.getMatchingStacks())
        {
            if(itemstack.getItemDamage() == OreDictionary.WILDCARD_VALUE){
                for(int dm = 0; dm < itemstack.getMaxDamage(); dm++){
                    matchingStacksPacked.add(this.pack(itemstack, dm));
                }
            }else
                matchingStacksPacked.add(this.pack(itemstack));
        }

        matchingStacksPacked.sort(IntComparators.NATURAL_COMPARATOR);

        return matchingStacksPacked;
    }

    private int pack(ItemStack stack)
    {
        Item item = stack.getItem();
        int i = stack.getItemDamage();//item.getHasSubtypes() ? stack.getMetadata() : 0;
        return Item.REGISTRY.getIDForObject(item) << 16 | i & 65535;
    }
    private int pack(ItemStack stack,int damageg)
    {
        Item item = stack.getItem();
        int i = damageg;//item.getHasSubtypes() ? stack.getMetadata() : 0;
        return Item.REGISTRY.getIDForObject(item) << 16 | i & 65535;
    }

    private static ItemStack[] createDisplayStacks(ItemStack[] stacks, RequestDefinition request) {
        List<ItemStack> result = new ArrayList<ItemStack>(stacks.length * 2);
        RequestDefinition resolvedRequest = request == null ? RequestDefinition.empty() : request;

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            ItemStack displayStack = normalizeCoreBladeDisplayStack(stack.copy());
            resolvedRequest.initItemStack(displayStack);
            addDisplayStackVariants(result, displayStack);
        }

        if (result.isEmpty()) {
            ItemStack fallback = normalizeCoreBladeDisplayStack(resolvedRequest.createDisplayStack());
            if (!fallback.isEmpty()) {
                addDisplayStackVariants(result, fallback);
            }
        }

        return result.toArray(new ItemStack[result.size()]);
    }

    private static ItemStack[] createRequestStacks(RequestDefinition request) {
        RequestDefinition resolvedRequest = request == null ? RequestDefinition.empty() : request;

        if (resolvedRequest.hasBladeId()) {
            ItemStack displayStack = normalizeCoreBladeDisplayStack(resolvedRequest.createDisplayStack());
            if (!displayStack.isEmpty()) {
                List<ItemStack> displayStacks = new ArrayList<ItemStack>(2);
                addDisplayStackVariants(displayStacks, displayStack);
                return displayStacks.toArray(new ItemStack[displayStacks.size()]);
            }
        }

        Set<Item> items = new LinkedHashSet<Item>();
        addBladeItem(items, SlashBlade.weapon);
        addBladeItem(items, SlashBlade.bladeWood);
        addBladeItem(items, SlashBlade.bladeBambooLight);
        addBladeItem(items, SlashBlade.bladeSilverBambooLight);
        addBladeItem(items, SlashBlade.bladeWhiteSheath);
        addBladeItem(items, SlashBlade.wrapBlade);
        addBladeItem(items, SlashBlade.bladeNamed);

        for (ItemSlashBlade fixedItem : SlashBlade.getFixedBladeItems()) {
            addBladeItem(items, fixedItem);
        }

        List<ItemStack> stacks = new ArrayList<ItemStack>(items.size());
        for (Item item : items) {
            ItemStack stack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
            resolvedRequest.initItemStack(stack);
            stacks.add(stack);
        }

        if (stacks.isEmpty()) {
            ItemStack fallback = resolvedRequest.createDisplayStack();
            if (!fallback.isEmpty()) {
                stacks.add(fallback);
            }
        }

        return stacks.toArray(new ItemStack[stacks.size()]);
    }

    private static void addDisplayStackVariants(List<ItemStack> result, ItemStack displayStack) {
        if (displayStack.isEmpty()) {
            return;
        }

        result.add(displayStack);

        if (!displayStack.isItemEnchanted()) {
            return;
        }

        ItemStack namedDisplayStack = displayStack.copy();
        namedDisplayStack.setStackDisplayName(displayStack.getDisplayName());
        result.add(namedDisplayStack);
    }

    private static ItemStack normalizeCoreBladeDisplayStack(ItemStack stack) {
        if (stack.isEmpty() || SlashBlade.weapon == null || stack.getItem() == SlashBlade.weapon) {
            return stack;
        }

        if (!String.valueOf(SlashBlade.weapon.getRegistryName()).equals(BladeIdentity.getBladeId(stack))) {
            return stack;
        }

        ItemStack normalized = new ItemStack(SlashBlade.weapon, stack.getCount(), Math.min(stack.getItemDamage(), SlashBlade.weapon.getMaxDamage()));
        if (stack.hasTagCompound()) {
            normalized.setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
        }
        return normalized;
    }

    private static void addBladeItem(Set<Item> items, Item item) {
        if (item != null) {
            items.add(item);
        }
    }
}
