package mods.flammpfeil.slashblade.item.named;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.item.crafting.BladeIngredient;
import mods.flammpfeil.slashblade.item.crafting.RecipeAwakeBlade;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mods.flammpfeil.slashblade.item.named.event.LoadEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by Furia on 14/07/07.
 */
public class Tizuru {
    String name = "slashblade.named.muramasa";
    String reqiredStr = name + ".reqired";
    @SubscribeEvent
    public void init(LoadEvent.PreInitEvent event){
        ItemStack customblade = new ItemStack(SlashBlade.bladeNamed,1,0);
        NBTTagCompound tag = new NBTTagCompound();
        customblade.setTagCompound(tag);

        ItemSlashBladeNamed.setCurrentItemName(tag, name);
        ItemSlashBladeNamed.CustomMaxDamage.set(tag, 50);
        ItemSlashBlade.setBaseAttackModifier(tag, 4 + Item.ToolMaterial.IRON.getAttackDamage());
        ItemSlashBlade.TextureName.set(tag, "named/muramasa/muramasa");
        ItemSlashBlade.ModelName.set(tag, "named/muramasa/muramasa");
        ItemSlashBlade.SpecialAttackType.set(tag, 1);
        ItemSlashBlade.StandbyRenderType.set(tag, 2);
        ItemSlashBladeNamed.IsDefaultBewitched.set(tag,true);

        customblade = SlashBlade.registerFixedBladeStack(name, customblade);
        ItemSlashBladeNamed.NamedBlades.add(SlashBlade.modid + ":" + name);

        ItemStack reqiredBlade = SlashBlade.findItemStack(SlashBlade.modid,"slashblade",1);
        {
            NBTTagCompound reqTag = new NBTTagCompound();
            reqiredBlade.setTagCompound(reqTag);
            ItemSlashBladeNamed.setCurrentItemName(reqTag, "slashblade.named.muramasa.required");
            ItemSlashBlade.ProudSoul.set(reqTag, 10000);
            ItemSlashBlade.RepairCount.set(reqTag,20);
        }
        reqiredBlade = SlashBlade.registerFixedBladeStack(reqiredStr,reqiredBlade);
        ItemSlashBladeNamed.NamedBlades.add(SlashBlade.modid + ":" + reqiredStr);
    }

    @SubscribeEvent
    public void postinit(LoadEvent.PostInitEvent event){

        ItemStack proudsoul = SlashBlade.findItemStack(SlashBlade.modid,SlashBlade.SphereBladeSoulStr,1);

        {
            ItemStack blade = SlashBlade.getCustomBlade(SlashBlade.modid,name);
            ItemStack reqiredBlade = SlashBlade.getCustomBlade(reqiredStr).copy();

            IRecipe recipe = new RecipeAwakeBlade(new ResourceLocation(SlashBlade.modid,"muramasa"),
                    blade,reqiredBlade,
                    "PPP",
                    "PXP",
                    "PPP",
                    'X', BladeIngredient.of(reqiredBlade),
                    'P', proudsoul);

            SlashBlade.addRecipe(name, recipe);
        }
    }
}
