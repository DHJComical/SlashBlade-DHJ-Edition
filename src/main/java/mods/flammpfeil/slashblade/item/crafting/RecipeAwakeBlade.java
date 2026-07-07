package mods.flammpfeil.slashblade.item.crafting;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.BladeStateCodec;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeAwakeBlade extends ShapedOreRecipe {

    private final ItemStack requiredStateBlade;
    private final RequestDefinition requestDefinition;

    public RecipeAwakeBlade(ResourceLocation loc,ItemStack result, ItemStack requiredStateBlade, Object... recipe) {
        super(loc, result, recipe);
        this.requiredStateBlade = requiredStateBlade.isEmpty() ? ItemStack.EMPTY : requiredStateBlade.copy();
        this.requestDefinition = RequestDefinition.fromBlade(requiredStateBlade);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        if (!super.matches(inv, world)) {
            return false;
        }

        if (requestDefinition.isEmpty()) {
            return true;
        }

        return !findMatchingBlade(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting var1) {
        ItemStack result = super.getCraftingResult(var1);

        ItemStack sourceBlade = findMatchingBlade(var1);
        if (sourceBlade.isEmpty()) {
            sourceBlade = findAnyBlade(var1);
        }

        if (!sourceBlade.isEmpty() && sourceBlade.hasTagCompound()) {
            NBTTagCompound oldTag = (NBTTagCompound) sourceBlade.getTagCompound().copy();

            {
                NBTTagCompound newTag = ItemSlashBlade.getItemTagCompound(result);

                String bladeId = mods.flammpfeil.slashblade.item.BladeIdentity.getBladeId(result);
                if(!bladeId.isEmpty()){
                    ItemStack tmp = SlashBlade.createBladeStack(bladeId);

                    if(tmp.isEmpty() && ItemSlashBladeNamed.CurrentItemName.exists(newTag)){
                        tmp = SlashBlade.createBladeStack(ItemSlashBladeNamed.CurrentItemName.get(newTag));
                    }

                    if(!tmp.isEmpty())
                        result = tmp;
                }
            }

            NBTTagCompound newTag = ItemSlashBlade.getItemTagCompound(result);

            BladeStateCodec.copyPersistentState(oldTag, newTag);
            BladeStateCodec.copyCompatibleEnchantments(sourceBlade, result);
        }

        return result;
    }

    public ItemStack getRequiredStateBlade() {
        return requiredStateBlade.isEmpty() ? ItemStack.EMPTY : requiredStateBlade.copy();
    }

    public RequestDefinition getRequestDefinition() {
        return requestDefinition;
    }

    private ItemStack findMatchingBlade(InventoryCrafting inv) {
        for (int idx = 0; idx < inv.getSizeInventory(); idx++) {
            ItemStack curIs = inv.getStackInSlot(idx);
            if (curIs.isEmpty() || !(curIs.getItem() instanceof ItemSlashBlade)) {
                continue;
            }

            if (requestDefinition.test(curIs)) {
                return curIs;
            }
        }

        return ItemStack.EMPTY;
    }

    private ItemStack findAnyBlade(InventoryCrafting inv) {
        for (int idx = 0; idx < inv.getSizeInventory(); idx++) {
            ItemStack curIs = inv.getStackInSlot(idx);
            if (!curIs.isEmpty() && curIs.getItem() instanceof ItemSlashBlade) {
                return curIs;
            }
        }

        return ItemStack.EMPTY;
    }
}
