package mods.flammpfeil.slashblade.item.named;

import net.minecraft.init.Enchantments;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.item.crafting.BladeIngredient;
import mods.flammpfeil.slashblade.item.crafting.RecipeAwakeBlade;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.named.event.LoadEvent;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by Furia on 15/02/12.
 */
public class Tagayasan {

    public static final String Tagayasan = "slashblade.named.tagayasan";

    @SubscribeEvent()
    public void init(LoadEvent.PreInitEvent event){
        ItemStack itemSphereBladeSoul = SlashBlade.findItemStack(SlashBlade.modid, SlashBlade.SphereBladeSoulStr , 1);

        ItemStack customblade = new ItemStack(SlashBlade.bladeNamed,1,0);
        NBTTagCompound tag = new NBTTagCompound();
        customblade.setTagCompound(tag);

        customblade.addEnchantment(Enchantments.UNBREAKING,3);
        customblade.addEnchantment(Enchantments.SMITE,3);
        String name = Tagayasan;
        ItemSlashBladeNamed.IsDefaultBewitched.set(tag,true);
        ItemSlashBladeNamed.setCurrentItemName(tag, name);
        ItemSlashBladeNamed.CustomMaxDamage.set(tag, 70);
        ItemSlashBlade.setBaseAttackModifier(tag, 4 + Item.ToolMaterial.IRON.getAttackDamage());
        ItemSlashBlade.TextureName.set(tag,"named/tagayasan");
        ItemSlashBlade.SpecialAttackType.set(tag, 1);
        ItemSlashBlade.StandbyRenderType.set(tag, 1);

        tag.setString(ItemSlashBladeNamed.RepairMaterialNameStr,"iron_ingot");

        customblade = SlashBlade.registerFixedBladeStack(name, customblade);
        ItemSlashBladeNamed.NamedBlades.add(name);
        {
            ItemStack reqiredBlade = new ItemStack(SlashBlade.bladeWood);
            NBTTagCompound reqTag = ItemSlashBlade.getItemTagCompound(reqiredBlade);
            ItemSlashBladeNamed.setCurrentItemName(reqTag, "slashblade.named.tagayasan.required");
            ItemSlashBlade.KillCount.set(reqTag,1000);

            name = "slashblade.tagayasan.reqired";
            reqiredBlade = SlashBlade.registerFixedBladeStack(name, reqiredBlade);
            ItemSlashBladeNamed.NamedBlades.add(name);

            SlashBlade.addRecipe(Tagayasan,
                    new RecipeAwakeBlade(new ResourceLocation(SlashBlade.modid,"tagayasan"),
                            customblade,
                    reqiredBlade,
                    "XEX",
                    "PBP",
                    "XEX",
                    'X',itemSphereBladeSoul,
                    'B', BladeIngredient.of(reqiredBlade),
                    'P',new ItemStack(Items.ENDER_PEARL),
                    'E',new ItemStack(Items.ENDER_EYE)));
        }
    }
}
