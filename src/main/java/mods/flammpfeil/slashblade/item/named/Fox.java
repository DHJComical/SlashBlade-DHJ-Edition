package mods.flammpfeil.slashblade.item.named;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import net.minecraft.init.Enchantments;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mods.flammpfeil.slashblade.item.named.event.LoadEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by Furia on 14/07/07.
 */
public class Fox {
    static public final String nameWhite = "slashblade.named.fox.white";
    static public final String nameBlack = "slashblade.named.fox.black";
    static public final String nameWhiteReqired = nameWhite + ".reqired";
    static public final String nameBlackReqired = nameBlack + ".reqired";

    @SubscribeEvent
    public void init(LoadEvent.PreInitEvent event){

        {
            String name = nameWhite;

            ItemStack customblade = SlashBlade.findItemStack(SlashBlade.modid,"slashbladeWrapper",1);
            SlashBlade.wrapBlade.removeWrapItem(customblade);

            customblade.addEnchantment(Enchantments.KNOCKBACK,2);
            customblade.addEnchantment(Enchantments.BANE_OF_ARTHROPODS,2);
            customblade.addEnchantment(Enchantments.UNBREAKING,3);
            customblade.addEnchantment(Enchantments.LOOTING,3);
            customblade.addEnchantment(Enchantments.FIRE_ASPECT,2);

            NBTTagCompound tag = customblade.getTagCompound();

            ItemStack innerBlade = SlashBlade.findItemStack("minecraft", "wooden_sword", 1);

            SlashBlade.wrapBlade.setWrapItem(customblade,innerBlade);

            ItemSlashBladeNamed.BaseAttackModifier.set(tag, 4.0f);

            ItemSlashBladeNamed.setCurrentItemName(tag, name);
            ItemSlashBladeNamed.TrueItemName.set(tag, name);

            ItemSlashBlade.TextureName.set(tag, "named/sange/white");
            ItemSlashBlade.ModelName.set(tag, "named/sange/sange");

            ItemSlashBlade.SpecialAttackType.set(tag, 0); //0:次元斬
            ItemSlashBlade.StandbyRenderType.set(tag, 1);

            ItemSlashBladeNamed.IsDefaultBewitched.set(tag, true);

            NamedBladeManager.registerBladeSoul(tag , customblade.getDisplayName());
            customblade = SlashBlade.registerFixedBladeStack(name, customblade);

            customblade = customblade.copy();
            ItemSlashBlade.setTooltipKeys(customblade,
                    "tooltip.slashblade.sample.line1",
                    "tooltip.slashblade.sample.line2");
            String creativeStr = name+".creative";
            customblade = SlashBlade.registerFixedBladeStack(creativeStr, customblade);
            ItemSlashBladeNamed.NamedBlades.add(SlashBlade.modid + ":" + creativeStr);
        }

        {
            String name = nameBlack;

            ItemStack customblade = SlashBlade.findItemStack(SlashBlade.modid,"slashbladeWrapper",1);
            SlashBlade.wrapBlade.removeWrapItem(customblade);

            customblade.addEnchantment(Enchantments.SMITE,4);
            customblade.addEnchantment(Enchantments.KNOCKBACK,2);
            customblade.addEnchantment(Enchantments.FIRE_ASPECT, 2);

            NBTTagCompound tag = customblade.getTagCompound();

            ItemStack innerBlade = SlashBlade.findItemStack("minecraft", "wooden_sword", 1);

            SlashBlade.wrapBlade.setWrapItem(customblade, innerBlade);

            ItemSlashBladeNamed.BaseAttackModifier.set(tag, 4.0f);

            ItemSlashBladeNamed.setCurrentItemName(tag, name);
            ItemSlashBladeNamed.TrueItemName.set(tag, name);

            ItemSlashBlade.TextureName.set(tag, "named/sange/black");
            ItemSlashBlade.ModelName.set(tag, "named/sange/sange");

            ItemSlashBlade.SpecialAttackType.set(tag, 4); //4:シュンカ一段
            ItemSlashBlade.StandbyRenderType.set(tag, 1);

            ItemSlashBladeNamed.IsDefaultBewitched.set(tag,true);

            NamedBladeManager.registerBladeSoul(tag , customblade.getDisplayName());
            customblade = SlashBlade.registerFixedBladeStack(name, customblade);

            customblade = customblade.copy();
            ItemSlashBlade.setTooltipKeys(customblade,
                    "tooltip.slashblade.sample.line1",
                    "tooltip.slashblade.sample.line2");
            String creativeStr = name+".creative";
            customblade = SlashBlade.registerFixedBladeStack(creativeStr, customblade);
            ItemSlashBladeNamed.NamedBlades.add(SlashBlade.modid + ":" + creativeStr);
        }

        ItemStack innerBlade = SlashBlade.findItemStack("minecraft", "wooden_sword", 1);

        {
            ItemStack reqiredBlade = SlashBlade.findItemStack(SlashBlade.modid,"slashbladeWrapper",1);
            {
                SlashBlade.wrapBlade.setWrapItem(reqiredBlade,innerBlade);

                reqiredBlade.addEnchantment(Enchantments.LOOTING,1);
                NBTTagCompound tag = reqiredBlade.getTagCompound();
                ItemSlashBladeNamed.setCurrentItemName(tag,"wrap.bamboomod.katana");
                ItemSlashBladeNamed.BaseAttackModifier.set(tag, 4.0f);
                ItemSlashBlade.TextureName.set(tag,"BambooKatana");
                ItemSlashBlade.KillCount.set(tag,199);
                ItemSlashBlade.ProudSoul.set(tag,1000);
                ItemSlashBlade.RepairCount.set(tag,1);
            }
            reqiredBlade = SlashBlade.registerFixedBladeStack(nameWhiteReqired,reqiredBlade);
            ItemSlashBladeNamed.NamedBlades.add(SlashBlade.modid + ":" + nameWhiteReqired);
        }
        {
            ItemStack reqiredBlade = SlashBlade.findItemStack(SlashBlade.modid,"slashbladeWrapper",1);
            {
                SlashBlade.wrapBlade.setWrapItem(reqiredBlade,innerBlade);

                reqiredBlade.addEnchantment(Enchantments.SMITE,1);
                NBTTagCompound tag = reqiredBlade.getTagCompound();
                ItemSlashBladeNamed.setCurrentItemName(tag,"wrap.bamboomod.katana");
                ItemSlashBladeNamed.BaseAttackModifier.set(tag, 4.0f);
                ItemSlashBlade.TextureName.set(tag,"BambooKatana");
                ItemSlashBlade.KillCount.set(tag,199);
                ItemSlashBlade.ProudSoul.set(tag,1000);
                ItemSlashBlade.RepairCount.set(tag,1);
            }
            reqiredBlade = SlashBlade.registerFixedBladeStack(nameBlackReqired,reqiredBlade);
            ItemSlashBladeNamed.NamedBlades.add(SlashBlade.modid + ":" + nameBlackReqired);
        }
    }

    @SubscribeEvent
    public void postInit(LoadEvent.PostInitEvent event){

        ItemStack innerBlade = SlashBlade.findItemStack("minecraft", "wooden_sword", 1);

        ItemStack kitunebi = SlashBlade.findItemStack("BambooMod","kitunebi",1);
        if(kitunebi.isEmpty())
            return;

        ItemStack inari = SlashBlade.findItemStack("TofuCraft","foodSet",1);
        if(!inari.isEmpty())
            inari.setItemDamage(14);
        else
            inari = new ItemStack(Items.WHEAT,1);

        ItemStack proudsoul = SlashBlade.findItemStack(SlashBlade.modid,"proudsoul",1);

        {
            ItemStack blade = SlashBlade.getCustomBlade(SlashBlade.modid,nameWhite);
            ItemStack reqiredBlade = SlashBlade.getCustomBlade(nameWhiteReqired).copy();

            IRecipe recipe = new RecipeAwakeBladeFox(new ResourceLocation(SlashBlade.modid,"fox_white"),
                    blade,reqiredBlade,
                    "FPF",
                    "FXF",
                    "FIF",
                    'X', reqiredBlade,
                    'F', kitunebi,
                    'I', inari,
                    'P', proudsoul);

            SlashBlade.addRecipe(nameWhite, recipe);
        }
        {
            ItemStack blade = SlashBlade.getCustomBlade(SlashBlade.modid,nameBlack);
            ItemStack reqiredBlade = SlashBlade.getCustomBlade(nameBlackReqired).copy();

            IRecipe recipe = new RecipeAwakeBladeFox(new ResourceLocation(SlashBlade.modid,"fox_black"),
                    blade,reqiredBlade,
                    "FPF",
                    "FXF",
                    "FIF",
                    'X', reqiredBlade,
                    'F', kitunebi,
                    'I', inari,
                    'P', proudsoul);

            SlashBlade.addRecipe(nameBlack, recipe);
        }
    }
}
