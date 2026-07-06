package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.util.ResourceLocationRaw;
import net.minecraft.item.ItemStack;

public class ItemSlashBladeNamedFixed extends ItemSlashBladeNamed {
    private ResourceLocationRaw modelTexture = null;
    private ResourceLocationRaw modelLocation = null;
    private boolean defaultDestructable = false;
    private boolean useDetuneSwordTraits = false;

    public ItemSlashBladeNamedFixed(ToolMaterial par2EnumToolMaterial, float baseAttackModifiers) {
        super(par2EnumToolMaterial, baseAttackModifiers);
    }

    public ItemSlashBladeNamedFixed configureFromPrototype(ItemSlashBlade sourceItem, ItemStack prototype) {
        this.defaultBaseAttackModifier = sourceItem.getBaseAttackModifiers(ItemSlashBlade.getItemTagCompound(prototype));
        this.setMaxDamage(prototype.getMaxDamage());
        this.modelTexture = sourceItem.getModelTexture(prototype);
        this.modelLocation = sourceItem.getModelLocation(prototype);
        this.defaultDestructable = sourceItem.isDestructable(prototype);
        this.useDetuneSwordTraits = sourceItem instanceof ItemSlashBladeDetune;
        return this;
    }

    @Override
    public ResourceLocationRaw getModelTexture() {
        return modelTexture != null ? modelTexture : super.getModelTexture();
    }

    @Override
    public ResourceLocationRaw getModel() {
        return modelLocation != null ? modelLocation : super.getModel();
    }

    @Override
    public boolean isDestructable(ItemStack stack) {
        if (stack.hasTagCompound() && ItemSlashBlade.IsDestructable.exists(stack.getTagCompound())) {
            return super.isDestructable(stack);
        }
        return super.isDestructable(stack) || defaultDestructable;
    }

    @Override
    public java.util.EnumSet<SwordType> getSwordType(ItemStack itemStack) {
        java.util.EnumSet<SwordType> set = super.getSwordType(itemStack);
        if (useDetuneSwordTraits) {
            set.remove(SwordType.Enchanted);
            set.remove(SwordType.Bewitched);
            set.remove(SwordType.SoulEeater);
        }
        return set;
    }
}
