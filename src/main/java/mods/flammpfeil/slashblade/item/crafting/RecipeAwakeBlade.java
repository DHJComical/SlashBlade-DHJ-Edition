package mods.flammpfeil.slashblade.item.crafting;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.BladeStateCodec;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.util.TagPropertyAccessor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.Map;

public class RecipeAwakeBlade extends ShapedOreRecipe {

    ItemStack requiredStateBlade = ItemStack.EMPTY;

    public RecipeAwakeBlade(ResourceLocation loc,ItemStack result, ItemStack requiredStateBlade, Object... recipe) {
        super(loc, result, recipe);
        this.requiredStateBlade = requiredStateBlade;
    }

    <T extends Comparable<T>> int tagValueCompare(TagPropertyAccessor<T> access, NBTTagCompound reqTag, NBTTagCompound srcTag){
        return access.get(reqTag).compareTo(access.get(srcTag));
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {

        boolean result = super.matches(inv, world);

        if(result && !requiredStateBlade.isEmpty()){
            requiredStateBlade.setItemDamage(OreDictionary.WILDCARD_VALUE);
            for(int idx = 0; idx < inv.getSizeInventory(); idx++){
                ItemStack curIs = inv.getStackInSlot(idx);
                if(!curIs.isEmpty()
                        && curIs.getItem() instanceof ItemSlashBlade
                        && curIs.hasTagCompound()){



                    Map<Enchantment,Integer> oldItemEnchants = EnchantmentHelper.getEnchantments(requiredStateBlade);
                    for(Map.Entry<Enchantment,Integer> enchant: oldItemEnchants.entrySet())
                    {
                        int level = EnchantmentHelper.getEnchantmentLevel(enchant.getKey(),curIs);
                        if(level < enchant.getValue()){
                            return false;
                        }
                    }

                    NBTTagCompound reqTag = ItemSlashBlade.getItemTagCompound(requiredStateBlade);
                    NBTTagCompound srcTag = ItemSlashBlade.getItemTagCompound(curIs);

                    if(!BladeIdentity.matchesIdentity(curIs, requiredStateBlade))
                        return false;

                    if(0 < tagValueCompare(ItemSlashBlade.ProudSoul, reqTag, srcTag))
                        return false;
                    if(0 < tagValueCompare(ItemSlashBlade.KillCount, reqTag, srcTag))
                        return false;
                    if(0 < tagValueCompare(ItemSlashBlade.RepairCount, reqTag, srcTag))
                        return false;



                    break;
                }
            }
        }

        return result;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting var1) {
        ItemStack result = super.getCraftingResult(var1);

        for(int idx = 0; idx < var1.getSizeInventory(); idx++){
            ItemStack curIs = var1.getStackInSlot(idx);
            if(!curIs.isEmpty()
                    && curIs.getItem() instanceof ItemSlashBlade
                    && curIs.hasTagCompound()){

                NBTTagCompound oldTag = curIs.getTagCompound();
                oldTag = (NBTTagCompound)oldTag.copy();

                {
                    NBTTagCompound newTag;
                    newTag = ItemSlashBlade.getItemTagCompound(result);

                    String bladeId = BladeIdentity.getBladeId(result);
                    if(!bladeId.isEmpty()){
                        ItemStack tmp;
                        tmp = SlashBlade.createBladeStack(bladeId);

                        if(tmp.isEmpty() && ItemSlashBladeNamed.CurrentItemName.exists(newTag)){
                            tmp = SlashBlade.createBladeStack(ItemSlashBladeNamed.CurrentItemName.get(newTag));
                        }

                        if(!tmp.isEmpty())
                            result = tmp;
                    }
                }

                NBTTagCompound newTag;
                newTag = ItemSlashBlade.getItemTagCompound(result);

                BladeStateCodec.copyPersistentState(oldTag, newTag);
                BladeStateCodec.copyCompatibleEnchantments(curIs, result);
            }
        }

        return result;
    }

}
