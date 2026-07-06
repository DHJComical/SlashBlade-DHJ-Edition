package mods.flammpfeil.slashblade.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public final class BladeState {
    private final NBTTagCompound tag;

    private BladeState(NBTTagCompound tag) {
        this.tag = tag;
        BladeStateCodec.ensureStateLayout(tag);
    }

    public static BladeState of(ItemStack stack) {
        return new BladeState(ItemSlashBlade.getItemTagCompound(stack));
    }

    public static BladeState of(NBTTagCompound tag) {
        return new BladeState(tag);
    }

    public NBTTagCompound getTag() {
        return tag;
    }

    public int getProudSoul() {
        return ItemSlashBlade.ProudSoul.get(tag);
    }

    public BladeState setProudSoul(int value) {
        ItemSlashBlade.ProudSoul.set(tag, value);
        BladeStateCodec.ensureStateLayout(tag);
        return this;
    }

    public int getKillCount() {
        return ItemSlashBlade.KillCount.get(tag);
    }

    public BladeState setKillCount(int value) {
        ItemSlashBlade.KillCount.set(tag, value);
        BladeStateCodec.ensureStateLayout(tag);
        return this;
    }

    public int getRepairCount() {
        return ItemSlashBlade.RepairCount.get(tag);
    }

    public BladeState setRepairCount(int value) {
        ItemSlashBlade.RepairCount.set(tag, value);
        BladeStateCodec.ensureStateLayout(tag);
        return this;
    }

    public UUID getOwner() {
        return tag.hasUniqueId("Owner") ? tag.getUniqueId("Owner") : null;
    }

    public BladeState setOwner(UUID ownerId) {
        if (ownerId == null) {
            tag.removeTag("OwnerMost");
            tag.removeTag("OwnerLeast");
        } else {
            tag.setUniqueId("Owner", ownerId);
        }
        BladeStateCodec.ensureStateLayout(tag);
        return this;
    }
}
