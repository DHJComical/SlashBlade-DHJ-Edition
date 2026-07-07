package mods.flammpfeil.slashblade.bridge.mixins.sjap;

import mods.flammpfeil.slashblade.compat.jei.SlashBladeJeiInputOverride;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "cn.mmf.slashblade_addon.recipes.RecipeNihil", remap = false)
public abstract class MixinRecipeNihil implements SlashBladeJeiInputOverride {
    private static final String NIHIL_BX = "flammpfeil.slashblade.named.nihilbx";

    @Shadow
    @Final
    protected ItemStack requiredBladeMain;

    @Shadow
    @Final
    protected ItemStack requiredBladeSub;

    @Shadow
    @Final
    protected int posXMain;

    @Shadow
    @Final
    protected int posYMain;

    @Shadow
    @Final
    protected int posXSub;

    @Shadow
    @Final
    protected int posYSub;

    @Inject(
            method = "<init>(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;IILnet/minecraft/item/ItemStack;IIZ[Ljava/lang/Object;)V",
            at = @At("RETURN")
    )
    private void slashblade$fixNihilRequirements(ResourceLocation location,
                                                 ItemStack result,
                                                 ItemStack requiredBladeMain,
                                                 int posXMain,
                                                 int posYMain,
                                                 ItemStack requiredBladeSub,
                                                 int posXSub,
                                                 int posYSub,
                                                 boolean remainedBladeSub,
                                                 Object[] recipe,
                                                 CallbackInfo ci) {
        if (!BladeIdentity.matchesIdentity(result, NIHIL_BX)) {
            return;
        }

        applyNihilBxRequirements(this.requiredBladeMain);
        applyNihilBxRequirements(this.requiredBladeSub);
    }

    @Inject(method = "matches", at = @At("RETURN"), cancellable = true, remap = true)
    private void slashblade$checkRequiredSubBlade(InventoryCrafting inv, World world, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        if (!hasCountRequirement(requiredBladeSub)) {
            return;
        }

        ItemStack subBlade = inv.getStackInRowAndColumn(posXSub, posYSub);
        if (!matchesCounts(subBlade, requiredBladeSub)) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public ItemStack slashblade$getJeiInputOverride(int x, int y, Ingredient ingredient) {
        if (x == posXMain && y == posYMain) {
            return copyBlade(requiredBladeMain);
        }

        if (x == posXSub && y == posYSub) {
            return copyBlade(requiredBladeSub);
        }

        return ItemStack.EMPTY;
    }

    private ItemStack copyBlade(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    private void applyNihilBxRequirements(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(stack);
        ItemSlashBlade.ProudSoul.set(tag, Math.max(ItemSlashBlade.ProudSoul.get(tag), 6500));
        ItemSlashBlade.KillCount.set(tag, Math.max(ItemSlashBlade.KillCount.get(tag), 3000));
        ItemSlashBlade.RepairCount.set(tag, Math.max(ItemSlashBlade.RepairCount.get(tag), 3));
    }

    private boolean hasCountRequirement(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(stack);
        return 0 < ItemSlashBlade.ProudSoul.get(tag)
                || 0 < ItemSlashBlade.KillCount.get(tag)
                || 0 < ItemSlashBlade.RepairCount.get(tag);
    }

    private boolean matchesCounts(ItemStack input, ItemStack required) {
        if (input == null || input.isEmpty() || !(input.getItem() instanceof ItemSlashBlade)) {
            return false;
        }

        NBTTagCompound inputTag = ItemSlashBlade.getItemTagCompound(input);
        NBTTagCompound requiredTag = ItemSlashBlade.getItemTagCompound(required);
        return ItemSlashBlade.ProudSoul.get(inputTag) >= ItemSlashBlade.ProudSoul.get(requiredTag)
                && ItemSlashBlade.KillCount.get(inputTag) >= ItemSlashBlade.KillCount.get(requiredTag)
                && ItemSlashBlade.RepairCount.get(inputTag) >= ItemSlashBlade.RepairCount.get(requiredTag);
    }
}
