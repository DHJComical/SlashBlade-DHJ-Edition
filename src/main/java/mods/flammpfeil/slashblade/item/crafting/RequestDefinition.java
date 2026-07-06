package mods.flammpfeil.slashblade.item.crafting;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.BladeStateCodec;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RequestDefinition {
    private static final RequestDefinition EMPTY = builder().build();

    private final String bladeId;
    private final int proudSoulCount;
    private final int killCount;
    private final int repairCount;
    private final Map<Enchantment, Integer> enchantments;
    @Nullable
    private final Boolean broken;
    @Nullable
    private final Boolean sealed;
    @Nullable
    private final Boolean noScabbard;
    @Nullable
    private final Boolean defaultBewitched;
    @Nullable
    private final Boolean bewitched;

    private RequestDefinition(String bladeId,
                              int proudSoulCount,
                              int killCount,
                              int repairCount,
                              Map<Enchantment, Integer> enchantments,
                              @Nullable Boolean broken,
                              @Nullable Boolean sealed,
                              @Nullable Boolean noScabbard,
                              @Nullable Boolean defaultBewitched,
                              @Nullable Boolean bewitched) {
        this.bladeId = StringUtils.defaultString(bladeId);
        this.proudSoulCount = Math.max(0, proudSoulCount);
        this.killCount = Math.max(0, killCount);
        this.repairCount = Math.max(0, repairCount);
        this.enchantments = Collections.unmodifiableMap(new LinkedHashMap<Enchantment, Integer>(enchantments));
        this.broken = broken;
        this.sealed = sealed;
        this.noScabbard = noScabbard;
        this.defaultBewitched = defaultBewitched;
        this.bewitched = bewitched;
    }

    public static RequestDefinition empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RequestDefinition fromBlade(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSlashBlade)) {
            return empty();
        }

        Builder builder = builder();

        String bladeId = BladeIdentity.getBladeId(stack);
        if (!StringUtils.isBlank(bladeId)) {
            builder.bladeId(bladeId);
        }

        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : null;
        if (tag != null) {
            int proudSoul = ItemSlashBlade.ProudSoul.get(tag);
            int killCount = ItemSlashBlade.KillCount.get(tag);
            int repairCount = ItemSlashBlade.RepairCount.get(tag);

            if (0 < proudSoul) {
                builder.proudSoul(proudSoul);
            }
            if (0 < killCount) {
                builder.killCount(killCount);
            }
            if (0 < repairCount) {
                builder.repairCount(repairCount);
            }

            if (ItemSlashBlade.IsBroken.get(tag)) {
                builder.broken(true);
            }
            if (ItemSlashBlade.IsSealed.get(tag)) {
                builder.sealed(true);
            }
            if (ItemSlashBlade.IsNoScabbard.get(tag)) {
                builder.noScabbard(true);
            }
            if (ItemSlashBladeNamed.IsDefaultBewitched.get(tag)) {
                builder.defaultBewitched(true);
            }
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            builder.enchantment(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    public String getBladeId() {
        return bladeId;
    }

    public int getProudSoulCount() {
        return proudSoulCount;
    }

    public int getKillCount() {
        return killCount;
    }

    public int getRepairCount() {
        return repairCount;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    @Nullable
    public Boolean getBroken() {
        return broken;
    }

    @Nullable
    public Boolean getSealed() {
        return sealed;
    }

    @Nullable
    public Boolean getNoScabbard() {
        return noScabbard;
    }

    @Nullable
    public Boolean getDefaultBewitched() {
        return defaultBewitched;
    }

    @Nullable
    public Boolean getBewitched() {
        return bewitched;
    }

    public boolean hasBladeId() {
        return !StringUtils.isBlank(bladeId);
    }

    public boolean isEmpty() {
        return !hasBladeId()
                && proudSoulCount <= 0
                && killCount <= 0
                && repairCount <= 0
                && enchantments.isEmpty()
                && broken == null
                && sealed == null
                && noScabbard == null
                && defaultBewitched == null
                && bewitched == null;
    }

    public ItemStack createDisplayStack() {
        ItemStack stack = ItemStack.EMPTY;

        if (hasBladeId()) {
            stack = SlashBlade.createBladeStack(bladeId);
        }

        if (stack.isEmpty() && SlashBlade.weapon != null) {
            stack = new ItemStack(SlashBlade.weapon);
        }

        if (!stack.isEmpty()) {
            initItemStack(stack);
        }

        return stack;
    }

    public void initItemStack(ItemStack blade) {
        if (blade.isEmpty() || !(blade.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        String fallbackBladeId = hasBladeId() ? bladeId : BladeIdentity.getRawBladeId(blade);
        if (!StringUtils.isBlank(fallbackBladeId)) {
            BladeStateCodec.ensureBladeState(blade, fallbackBladeId);
        } else {
            BladeStateCodec.ensureBladeState(blade);
        }

        NBTTagCompound tag = ItemSlashBlade.getItemTagCompound(blade);

        if (0 < proudSoulCount) {
            ItemSlashBlade.ProudSoul.set(tag, Math.max(ItemSlashBlade.ProudSoul.get(tag), proudSoulCount));
        }
        if (0 < killCount) {
            ItemSlashBlade.KillCount.set(tag, Math.max(ItemSlashBlade.KillCount.get(tag), killCount));
        }
        if (0 < repairCount) {
            ItemSlashBlade.RepairCount.set(tag, Math.max(ItemSlashBlade.RepairCount.get(tag), repairCount));
        }

        if (broken != null) {
            ItemSlashBlade.IsBroken.set(tag, broken.booleanValue());
            if (broken.booleanValue() && blade.getItemDamage() <= 0) {
                blade.setItemDamage(blade.getMaxDamage());
            }
        }
        if (sealed != null) {
            ItemSlashBlade.IsSealed.set(tag, sealed.booleanValue());
        }
        if (noScabbard != null) {
            ItemSlashBlade.IsNoScabbard.set(tag, noScabbard.booleanValue());
        }
        if (defaultBewitched != null) {
            ItemSlashBladeNamed.IsDefaultBewitched.set(tag, defaultBewitched.booleanValue());
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            int level = EnchantmentHelper.getEnchantmentLevel(entry.getKey(), blade);
            if (level < entry.getValue()) {
                blade.addEnchantment(entry.getKey(), entry.getValue());
            }
        }

        if (Boolean.TRUE.equals(bewitched) && !blade.isItemEnchanted()) {
            blade.addEnchantment(Enchantments.UNBREAKING, 1);
        }
    }

    public boolean test(ItemStack blade) {
        if (blade == null || blade.isEmpty()) {
            return false;
        }

        if (isEmpty()) {
            return true;
        }

        if (!(blade.getItem() instanceof ItemSlashBlade)) {
            return false;
        }

        if (hasBladeId() && !BladeIdentity.matchesIdentity(blade, bladeId)) {
            return false;
        }

        NBTTagCompound tag = blade.hasTagCompound() ? blade.getTagCompound() : null;

        if (getTagInt(tag, ItemSlashBlade.ProudSoul) < proudSoulCount) {
            return false;
        }
        if (getTagInt(tag, ItemSlashBlade.KillCount) < killCount) {
            return false;
        }
        if (getTagInt(tag, ItemSlashBlade.RepairCount) < repairCount) {
            return false;
        }

        if (broken != null && getTagBoolean(tag, ItemSlashBlade.IsBroken) != broken.booleanValue()) {
            return false;
        }
        if (sealed != null && getTagBoolean(tag, ItemSlashBlade.IsSealed) != sealed.booleanValue()) {
            return false;
        }
        if (noScabbard != null && getTagBoolean(tag, ItemSlashBlade.IsNoScabbard) != noScabbard.booleanValue()) {
            return false;
        }
        if (defaultBewitched != null && getTagBoolean(tag, ItemSlashBladeNamed.IsDefaultBewitched) != defaultBewitched.booleanValue()) {
            return false;
        }
        if (bewitched != null && isBewitched(blade, tag) != bewitched.booleanValue()) {
            return false;
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (EnchantmentHelper.getEnchantmentLevel(entry.getKey(), blade) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    private static int getTagInt(@Nullable NBTTagCompound tag, mods.flammpfeil.slashblade.util.TagPropertyAccessor.TagPropertyInteger access) {
        return tag == null ? 0 : access.get(tag);
    }

    private static int getTagInt(@Nullable NBTTagCompound tag, mods.flammpfeil.slashblade.util.TagPropertyAccessor.TagPropertyIntegerWithRange access) {
        return tag == null ? 0 : access.get(tag);
    }

    private static boolean getTagBoolean(@Nullable NBTTagCompound tag, mods.flammpfeil.slashblade.util.TagPropertyAccessor.TagPropertyBoolean access) {
        return tag != null && access.get(tag);
    }

    private static boolean isBewitched(ItemStack blade, @Nullable NBTTagCompound tag) {
        if (!blade.isItemEnchanted()) {
            return false;
        }

        if (tag != null && ItemSlashBlade.IsSealed.get(tag)) {
            return false;
        }

        return blade.hasDisplayName() || (tag != null && ItemSlashBladeNamed.IsDefaultBewitched.get(tag));
    }

    @Override
    public String toString() {
        return "RequestDefinition{" +
                "bladeId='" + bladeId + '\'' +
                ", proudSoulCount=" + proudSoulCount +
                ", killCount=" + killCount +
                ", repairCount=" + repairCount +
                ", enchantments=" + enchantments +
                ", broken=" + broken +
                ", sealed=" + sealed +
                ", noScabbard=" + noScabbard +
                ", defaultBewitched=" + defaultBewitched +
                ", bewitched=" + bewitched +
                '}';
    }

    public static final class Builder {
        private String bladeId = "";
        private int proudSoulCount = 0;
        private int killCount = 0;
        private int repairCount = 0;
        private final Map<Enchantment, Integer> enchantments = new LinkedHashMap<Enchantment, Integer>();
        @Nullable
        private Boolean broken = null;
        @Nullable
        private Boolean sealed = null;
        @Nullable
        private Boolean noScabbard = null;
        @Nullable
        private Boolean defaultBewitched = null;
        @Nullable
        private Boolean bewitched = null;

        private Builder() {
        }

        public Builder bladeId(String bladeId) {
            this.bladeId = StringUtils.defaultString(bladeId);
            return this;
        }

        public Builder proudSoul(int proudSoulCount) {
            this.proudSoulCount = Math.max(0, proudSoulCount);
            return this;
        }

        public Builder killCount(int killCount) {
            this.killCount = Math.max(0, killCount);
            return this;
        }

        public Builder repairCount(int repairCount) {
            this.repairCount = Math.max(0, repairCount);
            return this;
        }

        public Builder enchantment(Enchantment enchantment, int level) {
            if (enchantment != null && 0 < level) {
                this.enchantments.put(enchantment, level);
            }
            return this;
        }

        public Builder enchantments(Map<Enchantment, Integer> enchantments) {
            if (enchantments == null) {
                return this;
            }

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                enchantment(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder broken(boolean broken) {
            this.broken = broken;
            return this;
        }

        public Builder sealed(boolean sealed) {
            this.sealed = sealed;
            return this;
        }

        public Builder noScabbard(boolean noScabbard) {
            this.noScabbard = noScabbard;
            return this;
        }

        public Builder defaultBewitched(boolean defaultBewitched) {
            this.defaultBewitched = defaultBewitched;
            return this;
        }

        public Builder bewitched(boolean bewitched) {
            this.bewitched = bewitched;
            return this;
        }

        public RequestDefinition build() {
            return new RequestDefinition(
                    bladeId,
                    proudSoulCount,
                    killCount,
                    repairCount,
                    enchantments,
                    broken,
                    sealed,
                    noScabbard,
                    defaultBewitched,
                    bewitched
            );
        }
    }
}
