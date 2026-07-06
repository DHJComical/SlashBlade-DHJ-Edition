package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.util.TagPropertyAccessor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public final class BladeStateCodec {
    public static final int CURRENT_DEFINITION_VERSION = 1;
    public static final String DEFINITION_VERSION_TAG = "DefinitionVersion";
    public static final String PROGRESS_TAG = "Progress";
    public static final String OWNERSHIP_TAG = "Ownership";
    public static final String COMBAT_TAG = "Combat";
    public static final String SPECIAL_TAG = "Special";
    public static final String LEGACY_TAG = "Legacy";
    private static final String SPECIAL_EFFECTS_TAG = "SB.SEffect";
    private static final String DISPLAY_TAG = "display";
    private static final String RANGE_ATTACK_TYPE_TAG = "RangeAttackType";

    private static final TagPropertyAccessor<?>[] DEFINITION_ACCESSORS = {
            ItemSlashBladeNamed.CustomMaxDamage,
            ItemSlashBlade.TextureName,
            ItemSlashBlade.ModelName,
            ItemSlashBlade.SpecialAttackType,
            ItemSlashBlade.StandbyRenderType,
            ItemSlashBladeNamed.IsDefaultBewitched,
            ItemSlashBladeNamed.TrueItemName,
            ItemSlashBlade.SummonedSwordColor,
            ItemSlashBlade.IsDestructable,
            ItemSlashBlade.IsBroken,
            ItemSlashBlade.IsNoScabbard,
            ItemSlashBlade.IsSealed
    };

    private BladeStateCodec() {
    }

    public static void ensureBladeState(ItemStack stack) {
        BladeIdentity.ensureBladeId(stack);
        ensureStateLayout(ItemSlashBlade.getItemTagCompound(stack));
    }

    public static void ensureBladeState(ItemStack stack, String fallbackBladeId) {
        BladeIdentity.ensureBladeId(stack, fallbackBladeId);
        ensureStateLayout(ItemSlashBlade.getItemTagCompound(stack));
    }

    public static void ensureStateLayout(NBTTagCompound tag) {
        if (tag == null) {
            return;
        }

        syncLegacyFromGroups(tag);
        syncGroupsFromLegacy(tag);
        tag.setInteger(DEFINITION_VERSION_TAG, CURRENT_DEFINITION_VERSION);
    }

    public static void copyPersistentState(ItemStack from, ItemStack to) {
        if (from.isEmpty() || to.isEmpty()) {
            return;
        }

        copyPersistentState(ItemSlashBlade.getItemTagCompound(from), ItemSlashBlade.getItemTagCompound(to));
    }

    public static void copyPersistentState(NBTTagCompound from, NBTTagCompound to) {
        if (from == null || to == null) {
            return;
        }

        ensureStateLayout(from);
        ensureStateLayout(to);

        ItemSlashBlade.ProudSoul.copy(to, from);
        ItemSlashBlade.KillCount.copy(to, from);
        ItemSlashBlade.RepairCount.copy(to, from);
        ItemSlashBlade.PrevExp.copy(to, from);

        copyFloatTagIfPresent(from, to, ItemSlashBlade.adjustXStr);
        copyFloatTagIfPresent(from, to, ItemSlashBlade.adjustYStr);
        copyFloatTagIfPresent(from, to, ItemSlashBlade.adjustZStr);

        if (from.hasUniqueId("Owner")) {
            to.setUniqueId("Owner", from.getUniqueId("Owner"));
        }

        if (from.hasKey(SPECIAL_EFFECTS_TAG, 10)) {
            to.setTag(SPECIAL_EFFECTS_TAG, from.getCompoundTag(SPECIAL_EFFECTS_TAG).copy());
        }

        copyPrimitiveTagIfPresent(from, to, RANGE_ATTACK_TYPE_TAG);

        syncGroupsFromLegacy(to);
    }

    public static void copyDefinitionOverrides(ItemStack from, ItemStack to) {
        if (from.isEmpty() || to.isEmpty()) {
            return;
        }

        copyDefinitionOverrides(ItemSlashBlade.getItemTagCompound(from), ItemSlashBlade.getItemTagCompound(to));
    }

    public static void copyDefinitionOverrides(NBTTagCompound from, NBTTagCompound to) {
        if (from == null || to == null) {
            return;
        }

        if (ItemSlashBladeNamed.CurrentItemName.exists(from)) {
            ItemSlashBladeNamed.setCurrentItemName(to, ItemSlashBladeNamed.CurrentItemName.get(from));
        }
        BladeIdentity.copyBladeId(to, from);

        if (ItemSlashBlade.BaseAttackModifier.exists(from)) {
            ItemSlashBlade.setBaseAttackModifier(to, ItemSlashBlade.BaseAttackModifier.get(from));
        }

        for (TagPropertyAccessor<?> accessor : DEFINITION_ACCESSORS) {
            accessor.copy(to, from);
        }

        copyStringTagIfPresent(from, to, ItemSlashBladeNamed.RepairOreDicMaterialStr);
        copyStringTagIfPresent(from, to, ItemSlashBladeNamed.RepairMaterialNameStr);
        copyCompoundTagIfPresent(from, to, DISPLAY_TAG);
        copyCompoundTagIfPresent(from, to, ItemSlashBladeWrapper.WrapItemStr);
        copyCompoundTagIfPresent(from, to, SPECIAL_EFFECTS_TAG);

        syncGroupsFromLegacy(to);
    }

    public static void copyDisplayName(ItemStack from, ItemStack to) {
        if (from.isEmpty() || to.isEmpty()) {
            return;
        }

        if (from.hasDisplayName()) {
            to.setStackDisplayName(from.getDisplayName());
        }
    }

    public static void copyDamage(ItemStack from, ItemStack to) {
        if (from.isEmpty() || to.isEmpty()) {
            return;
        }

        to.setItemDamage(Math.min(from.getItemDamage(), to.getMaxDamage()));
    }

    public static void copyEnchantments(ItemStack from, ItemStack to) {
        if (from.isEmpty() || to.isEmpty()) {
            return;
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(from);
        if (!enchantments.isEmpty()) {
            EnchantmentHelper.setEnchantments(enchantments, to);
        }
    }

    public static void copyCompatibleEnchantments(ItemStack from, ItemStack to) {
        if (from.isEmpty() || to.isEmpty()) {
            return;
        }

        Map<Enchantment, Integer> destEnchantments = EnchantmentHelper.getEnchantments(to);
        Map<Enchantment, Integer> sourceEnchantments = EnchantmentHelper.getEnchantments(from);

        for (Map.Entry<Enchantment, Integer> entry : sourceEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int destLevel = destEnchantments.containsKey(enchantment) ? destEnchantments.get(enchantment) : 0;
            int mergedLevel = Math.min(Math.max(entry.getValue(), destLevel), enchantment.getMaxLevel());

            boolean canApply = enchantment.canApply(to);
            if (canApply) {
                for (Enchantment current : destEnchantments.keySet()) {
                    if (current != enchantment && !enchantment.isCompatibleWith(current)) {
                        canApply = false;
                        break;
                    }
                }
            }

            if (canApply) {
                destEnchantments.put(enchantment, mergedLevel);
            }
        }

        EnchantmentHelper.setEnchantments(destEnchantments, to);
    }

    private static void syncLegacyFromGroups(NBTTagCompound tag) {
        if (tag.hasKey(PROGRESS_TAG, 10)) {
            NBTTagCompound progress = tag.getCompoundTag(PROGRESS_TAG);
            if (!ItemSlashBlade.ProudSoul.exists(tag) && ItemSlashBlade.ProudSoul.exists(progress)) {
                ItemSlashBlade.ProudSoul.copy(tag, progress);
            }
            if (!ItemSlashBlade.KillCount.exists(tag) && ItemSlashBlade.KillCount.exists(progress)) {
                ItemSlashBlade.KillCount.copy(tag, progress);
            }
            if (!ItemSlashBlade.RepairCount.exists(tag) && ItemSlashBlade.RepairCount.exists(progress)) {
                ItemSlashBlade.RepairCount.copy(tag, progress);
            }
            if (!ItemSlashBlade.PrevExp.exists(tag) && ItemSlashBlade.PrevExp.exists(progress)) {
                ItemSlashBlade.PrevExp.copy(tag, progress);
            }
        }

        if (tag.hasKey(OWNERSHIP_TAG, 10)) {
            NBTTagCompound ownership = tag.getCompoundTag(OWNERSHIP_TAG);
            if (!tag.hasUniqueId("Owner") && ownership.hasUniqueId("Owner")) {
                tag.setUniqueId("Owner", ownership.getUniqueId("Owner"));
            }
        }

        if (tag.hasKey(COMBAT_TAG, 10)) {
            NBTTagCompound combat = tag.getCompoundTag(COMBAT_TAG);
            copyIntegerTagIfPresent(combat, tag, ItemSlashBlade.comboSeqStr);
            if (!ItemSlashBlade.IsCharged.exists(tag) && ItemSlashBlade.IsCharged.exists(combat)) {
                ItemSlashBlade.IsCharged.copy(tag, combat);
            }
            if (!ItemSlashBlade.TargetEntityId.exists(tag) && ItemSlashBlade.TargetEntityId.exists(combat)) {
                ItemSlashBlade.TargetEntityId.copy(tag, combat);
            }
            if (!ItemSlashBlade.AttackAmplifier.exists(tag) && ItemSlashBlade.AttackAmplifier.exists(combat)) {
                ItemSlashBlade.AttackAmplifier.copy(tag, combat);
            }
        }

        if (tag.hasKey(SPECIAL_TAG, 10)) {
            NBTTagCompound special = tag.getCompoundTag(SPECIAL_TAG);
            if (!tag.hasKey(SPECIAL_EFFECTS_TAG, 10) && special.hasKey(SPECIAL_EFFECTS_TAG, 10)) {
                tag.setTag(SPECIAL_EFFECTS_TAG, special.getCompoundTag(SPECIAL_EFFECTS_TAG).copy());
            }
        }

        if (tag.hasKey(LEGACY_TAG, 10)) {
            NBTTagCompound legacy = tag.getCompoundTag(LEGACY_TAG);
            if (!ItemSlashBladeNamed.CurrentItemName.exists(tag) && ItemSlashBladeNamed.CurrentItemName.exists(legacy)) {
                ItemSlashBladeNamed.CurrentItemName.copy(tag, legacy);
            }
            if (!ItemSlashBladeNamed.TrueItemName.exists(tag) && ItemSlashBladeNamed.TrueItemName.exists(legacy)) {
                ItemSlashBladeNamed.TrueItemName.copy(tag, legacy);
            }
        }
    }

    private static void syncGroupsFromLegacy(NBTTagCompound tag) {
        NBTTagCompound progress = tag.getCompoundTag(PROGRESS_TAG);
        ItemSlashBlade.ProudSoul.copy(progress, tag);
        ItemSlashBlade.KillCount.copy(progress, tag);
        ItemSlashBlade.RepairCount.copy(progress, tag);
        ItemSlashBlade.PrevExp.copy(progress, tag);
        tag.setTag(PROGRESS_TAG, progress);

        NBTTagCompound ownership = tag.getCompoundTag(OWNERSHIP_TAG);
        if (tag.hasUniqueId("Owner")) {
            ownership.setUniqueId("Owner", tag.getUniqueId("Owner"));
        }
        tag.setTag(OWNERSHIP_TAG, ownership);

        NBTTagCompound combat = tag.getCompoundTag(COMBAT_TAG);
        if (tag.hasKey(ItemSlashBlade.comboSeqStr)) {
            combat.setInteger(ItemSlashBlade.comboSeqStr, tag.getInteger(ItemSlashBlade.comboSeqStr));
        }
        ItemSlashBlade.IsCharged.copy(combat, tag);
        ItemSlashBlade.TargetEntityId.copy(combat, tag);
        ItemSlashBlade.AttackAmplifier.copy(combat, tag);
        tag.setTag(COMBAT_TAG, combat);

        NBTTagCompound special = tag.getCompoundTag(SPECIAL_TAG);
        if (tag.hasKey(SPECIAL_EFFECTS_TAG, 10)) {
            special.setTag(SPECIAL_EFFECTS_TAG, tag.getCompoundTag(SPECIAL_EFFECTS_TAG).copy());
        }
        tag.setTag(SPECIAL_TAG, special);

        NBTTagCompound legacy = tag.getCompoundTag(LEGACY_TAG);
        ItemSlashBladeNamed.CurrentItemName.copy(legacy, tag);
        ItemSlashBladeNamed.TrueItemName.copy(legacy, tag);
        tag.setTag(LEGACY_TAG, legacy);
    }

    private static void copyFloatTagIfPresent(NBTTagCompound from, NBTTagCompound to, String key) {
        if (from.hasKey(key)) {
            to.setFloat(key, from.getFloat(key));
        }
    }

    private static void copyIntegerTagIfPresent(NBTTagCompound from, NBTTagCompound to, String key) {
        if (from.hasKey(key)) {
            to.setInteger(key, from.getInteger(key));
        }
    }

    private static void copyStringTagIfPresent(NBTTagCompound from, NBTTagCompound to, String key) {
        if (from.hasKey(key)) {
            to.setString(key, from.getString(key));
        }
    }

    private static void copyCompoundTagIfPresent(NBTTagCompound from, NBTTagCompound to, String key) {
        if (from.hasKey(key, 10)) {
            to.setTag(key, from.getCompoundTag(key).copy());
        }
    }

    private static void copyPrimitiveTagIfPresent(NBTTagCompound from, NBTTagCompound to, String key) {
        if (!from.hasKey(key)) {
            return;
        }

        if (from.hasKey(key, 1)) {
            to.setByte(key, from.getByte(key));
        } else if (from.hasKey(key, 2)) {
            to.setShort(key, from.getShort(key));
        } else if (from.hasKey(key, 3)) {
            to.setInteger(key, from.getInteger(key));
        } else if (from.hasKey(key, 4)) {
            to.setLong(key, from.getLong(key));
        } else if (from.hasKey(key, 5)) {
            to.setFloat(key, from.getFloat(key));
        } else if (from.hasKey(key, 6)) {
            to.setDouble(key, from.getDouble(key));
        } else if (from.hasKey(key, 8)) {
            to.setString(key, from.getString(key));
        }
    }
}
