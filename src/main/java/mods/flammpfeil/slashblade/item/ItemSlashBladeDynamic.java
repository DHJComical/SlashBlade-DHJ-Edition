package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.LinkedHashSet;

public class ItemSlashBladeDynamic extends ItemSlashBladeNamed {
    public ItemSlashBladeDynamic(ToolMaterial par2EnumToolMaterial, float baseAttackModifiers) {
        super(par2EnumToolMaterial, baseAttackModifiers);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }

        ItemStack targetBlade = SlashBlade.findItemStack(SlashBlade.modid, "slashbladeNamed", 1);
        if (!targetBlade.isEmpty()) {
            NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(targetBlade);
            ItemSlashBlade.ProudSoul.set(tag, 1000);
            subItems.add(targetBlade);
        }

        for (String bladeName : new LinkedHashSet<String>(NamedBlades)) {
            ItemStack blade = SlashBlade.createBladeStack(bladeName);
            if (blade.isEmpty()) {
                blade = SlashBlade.getCustomBlade(bladeName);
            }

            if (blade.isEmpty()) {
                continue;
            }

            if (blade.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                blade.setItemDamage(0);
            }
            subItems.add(blade);
        }
    }
}
