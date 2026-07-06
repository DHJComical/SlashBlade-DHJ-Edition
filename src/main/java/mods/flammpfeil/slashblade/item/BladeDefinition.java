package mods.flammpfeil.slashblade.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class BladeDefinition {
    private final String bladeId;
    @Nullable
    private final ResourceLocation registryName;
    private final String translationKey;
    private final String legacyName;
    private final String modelName;
    private final String textureName;
    private final float baseAttackModifier;
    private final int maxDamage;
    private final int specialAttackType;
    private final int standbyRenderType;
    private final boolean defaultBewitched;
    private final boolean destructable;
    private final boolean broken;
    private final boolean noScabbard;
    private final boolean sealed;
    private final boolean fixedBlade;
    private final boolean legacyBridge;
    private final ItemStack repairMaterial;
    private final String[] repairOreDicMaterials;
    private final ItemStack canonicalStack;
    private final Set<String> aliases;

    private BladeDefinition(String bladeId,
                            @Nullable ResourceLocation registryName,
                            String translationKey,
                            String legacyName,
                            String modelName,
                            String textureName,
                            float baseAttackModifier,
                            int maxDamage,
                            int specialAttackType,
                            int standbyRenderType,
                            boolean defaultBewitched,
                            boolean destructable,
                            boolean broken,
                            boolean noScabbard,
                            boolean sealed,
                            boolean fixedBlade,
                            boolean legacyBridge,
                            ItemStack repairMaterial,
                            String[] repairOreDicMaterials,
                            ItemStack canonicalStack,
                            Set<String> aliases) {
        this.bladeId = bladeId;
        this.registryName = registryName;
        this.translationKey = translationKey;
        this.legacyName = legacyName;
        this.modelName = modelName;
        this.textureName = textureName;
        this.baseAttackModifier = baseAttackModifier;
        this.maxDamage = maxDamage;
        this.specialAttackType = specialAttackType;
        this.standbyRenderType = standbyRenderType;
        this.defaultBewitched = defaultBewitched;
        this.destructable = destructable;
        this.broken = broken;
        this.noScabbard = noScabbard;
        this.sealed = sealed;
        this.fixedBlade = fixedBlade;
        this.legacyBridge = legacyBridge;
        this.repairMaterial = repairMaterial.isEmpty() ? ItemStack.EMPTY : repairMaterial.copy();
        this.repairOreDicMaterials = repairOreDicMaterials == null ? new String[0] : repairOreDicMaterials.clone();
        this.canonicalStack = canonicalStack.isEmpty() ? ItemStack.EMPTY : canonicalStack.copy();
        this.aliases = Collections.unmodifiableSet(new LinkedHashSet<String>(aliases));
    }

    public static BladeDefinition fromPrototype(String bladeId, ItemStack prototype, boolean fixedBlade, boolean legacyBridge, Set<String> aliases) {
        if (prototype.isEmpty() || !(prototype.getItem() instanceof ItemSlashBlade)) {
            throw new IllegalArgumentException("prototype must be a slashblade item");
        }

        ItemSlashBlade item = (ItemSlashBlade) prototype.getItem();
        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(prototype);

        return new BladeDefinition(
                bladeId,
                prototype.getItem().getRegistryName(),
                prototype.getTranslationKey(),
                ItemSlashBladeNamed.CurrentItemName.exists(tag) ? ItemSlashBladeNamed.CurrentItemName.get(tag) : "",
                ItemSlashBlade.ModelName.exists(tag) ? ItemSlashBlade.ModelName.get(tag) : "",
                ItemSlashBlade.TextureName.exists(tag) ? ItemSlashBlade.TextureName.get(tag) : "",
                item.getBaseAttackModifiers(tag),
                prototype.getMaxDamage(),
                ItemSlashBlade.SpecialAttackType.get(tag),
                ItemSlashBlade.StandbyRenderType.get(tag),
                ItemSlashBladeNamed.IsDefaultBewitched.get(tag),
                item.isDestructable(prototype),
                ItemSlashBlade.IsBroken.get(tag),
                ItemSlashBlade.IsNoScabbard.get(tag),
                ItemSlashBlade.IsSealed.get(tag),
                fixedBlade,
                legacyBridge,
                item.getRepairMaterial(),
                item.getRepairMaterialOreDic(),
                prototype,
                aliases
        );
    }

    public BladeDefinition withAliases(Set<String> aliases) {
        return new BladeDefinition(
                bladeId,
                registryName,
                translationKey,
                legacyName,
                modelName,
                textureName,
                baseAttackModifier,
                maxDamage,
                specialAttackType,
                standbyRenderType,
                defaultBewitched,
                destructable,
                broken,
                noScabbard,
                sealed,
                fixedBlade,
                legacyBridge,
                repairMaterial,
                repairOreDicMaterials,
                canonicalStack,
                aliases
        );
    }

    public String getBladeId() {
        return bladeId;
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getLegacyName() {
        return legacyName;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTextureName() {
        return textureName;
    }

    public float getBaseAttackModifier() {
        return baseAttackModifier;
    }

    public int getMaxDamage() {
        return maxDamage;
    }

    public int getSpecialAttackType() {
        return specialAttackType;
    }

    public int getStandbyRenderType() {
        return standbyRenderType;
    }

    public boolean isDefaultBewitched() {
        return defaultBewitched;
    }

    public boolean isDestructable() {
        return destructable;
    }

    public boolean isBroken() {
        return broken;
    }

    public boolean isNoScabbard() {
        return noScabbard;
    }

    public boolean isSealed() {
        return sealed;
    }

    public boolean isFixedBlade() {
        return fixedBlade;
    }

    public boolean isLegacyBridge() {
        return legacyBridge;
    }

    public ItemStack getRepairMaterial() {
        return repairMaterial.isEmpty() ? ItemStack.EMPTY : repairMaterial.copy();
    }

    public String[] getRepairOreDicMaterials() {
        return repairOreDicMaterials.clone();
    }

    public ItemStack getCanonicalStack() {
        return canonicalStack.isEmpty() ? ItemStack.EMPTY : canonicalStack.copy();
    }

    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public String toString() {
        return "BladeDefinition{" +
                "bladeId='" + bladeId + '\'' +
                ", registryName=" + registryName +
                ", fixedBlade=" + fixedBlade +
                ", legacyBridge=" + legacyBridge +
                ", aliases=" + Arrays.toString(aliases.toArray(new String[0])) +
                '}';
    }
}
