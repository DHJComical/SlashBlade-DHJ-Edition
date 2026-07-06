package mods.flammpfeil.slashblade.item.named;

import net.minecraft.init.Enchantments;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import mods.flammpfeil.slashblade.item.ItemSlashBladeWrapper;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.named.event.LoadEvent;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Created by Furia on 14/11/11.
 */
public class BambooMod {
    @SubscribeEvent
    public void init(LoadEvent.PreInitEvent event){
        ItemStack innerBlade = SlashBlade.findItemStack("minecraft", "wooden_sword", 1);

        ItemStack reqiredBlade = SlashBlade.findItemStack(SlashBlade.modid,"slashbladeWrapper",1);
        {
            SlashBlade.wrapBlade.setWrapItem(reqiredBlade,innerBlade);

            reqiredBlade.addEnchantment(Enchantments.LOOTING,1);
            NBTTagCompound tag = reqiredBlade.getTagCompound();
            ItemSlashBladeNamed.setCurrentItemName(tag,"wrap.bamboomod.katana");
            ItemSlashBladeNamed.BaseAttackModifier.set(tag, 4.0f);
            ItemSlashBlade.TextureName.set(tag,"BambooKatana");

            NamedBladeManager.registerBladeSoul(tag , reqiredBlade.getDisplayName());

            ItemSlashBlade.setTooltipKeys(reqiredBlade,
                    "tooltip.slashblade.sample.line1",
                    "tooltip.slashblade.sample.line2");
        }
        SlashBlade.registerFixedBladeStack("wrap.bamboomod.katana", reqiredBlade.copy());
        String reqiredStr = "wrap.BambooMod.katana.sample";
        reqiredBlade = SlashBlade.registerFixedBladeStack(reqiredStr,reqiredBlade);
        ItemSlashBladeNamed.NamedBlades.add(SlashBlade.modid + ":" + reqiredStr);
    }

    @SubscribeEvent
    public void postinit(LoadEvent.PostInitEvent event){
        ItemStack katana = SlashBlade.findItemStack("BambooMod","katana",1);

        if(Loader.isModLoaded("BambooMod") && !katana.isEmpty()){
            RecipeBambooMod recipe = new BambooMod.RecipeBambooMod();
            SlashBlade.addRecipe("wrap.BambooMod.katana.sample", recipe);
        }
    }
    /**
     * Created by Furia on 14/11/11.
     */
    public static class RecipeBambooMod extends ShapedOreRecipe {
        ItemStack proudSoul;
        ItemStack katana;
        float attackModif;

        public RecipeBambooMod() {
            super(new ResourceLocation(SlashBlade.modid,"bamboo"),
                    SlashBlade.findItemStack(SlashBlade.modid,"wrap.BambooMod.katana.sample",1),
                    "  P",
                    " S ",
                    "B  ",
                    'P', SlashBlade.findItemStack(SlashBlade.modid, SlashBlade.ProudSoulStr, 1),
                    'S', SlashBlade.findItemStack(SlashBlade.modid,"slashbladeWrapper",1),
                    'B', SlashBlade.findItemStack("BambooMod","katana",1));

            this.proudSoul = SlashBlade.findItemStack(SlashBlade.modid,SlashBlade.ProudSoulStr,1);
            this.katana = SlashBlade.findItemStack("BambooMod","katana",1);
            this.attackModif = 4.0f;
        }
    //RegisterWrapable("BambooMod:katana", "BambooKatana", 4.0f);

        @Override
        public boolean matches(InventoryCrafting cInv, World par2World)
        {
            {
                ItemStack ps = cInv.getStackInRowAndColumn(2, 0);
                if(!(!ps.isEmpty() && ps.isItemEqual(proudSoul)))
                    return false;

                ItemStack sc = cInv.getStackInRowAndColumn(1, 1);
                if(!(!sc.isEmpty() && sc.getItem() == SlashBlade.wrapBlade && !ItemSlashBladeWrapper.hasWrapedItem(sc)))
                    return false;


                ItemStack target = cInv.getStackInRowAndColumn(0, 2);
                if(!(!target.isEmpty() && target.getItem().equals(katana.getItem())))
                    return false;

                return true;
            }
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting cInv)
        {
            ItemStack scabbard = cInv.getStackInRowAndColumn(1, 1);
            if(scabbard.isEmpty()) return ItemStack.EMPTY;
            scabbard = scabbard.copy();

            ItemStack target = cInv.getStackInRowAndColumn(0, 2);
            if(target.isEmpty()) return ItemStack.EMPTY;
            target = target.copy();


            SlashBlade.wrapBlade.removeWrapItem(scabbard);

            SlashBlade.wrapBlade.setWrapItem(scabbard,target);

            NBTTagCompound tag = scabbard.getTagCompound();
            ItemSlashBladeNamed.setCurrentItemName(tag,"wrap.bamboomod.katana");
            ItemSlashBladeNamed.TextureName.set(tag,"BambooKatana");
            ItemSlashBladeNamed.BaseAttackModifier.set(tag,attackModif);

            if(target.hasDisplayName()){
                scabbard.setStackDisplayName(String.format(I18n.format("item.slashblade.wrapformat").trim(), target.getDisplayName()));
            }else if(target.isItemEnchanted()){
                scabbard.setStackDisplayName(scabbard.getDisplayName());
            }else{
                scabbard.setStackDisplayName(String.format(I18n.format("item.slashblade.wrapformat.low").trim(),target.getDisplayName()));
            }

            if(target.isItemEnchanted()){
                tag.setTag("ench",target.getTagCompound().getTag("ench"));
            }

            return scabbard;
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            NonNullList<ItemStack> stacks = super.getRemainingItems(inv);


            for(ItemStack stack : stacks){
                if(stack.getItem().equals(katana.getItem())) {
                    stack = ItemStack.EMPTY;
                    break;
                }
            }

            return stacks;
        }
    }
}
