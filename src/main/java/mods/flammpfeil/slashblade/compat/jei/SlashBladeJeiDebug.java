package mods.flammpfeil.slashblade.compat.jei;

import mods.flammpfeil.slashblade.item.BladeIdentity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBladeNamed;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class SlashBladeJeiDebug {
    private static final Logger LOGGER = LogManager.getLogger("SlashBlade-JEI-Debug");
    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("slashblade.jei.debug", "true"));
    private static final Set<String> loggedSubtypes = new HashSet<String>();
    private static final Set<String> loggedRecipeFocuses = new HashSet<String>();

    private SlashBladeJeiDebug() {
    }

    static void log(String message) {
        if (ENABLED) {
            LOGGER.info(message);
        }
    }

    static void logSubtype(ItemStack stack, String subtype) {
        if (!ENABLED) {
            return;
        }

        String fingerprint = "subtype|" + describeStack(stack) + "|" + subtype;
        if (!loggedSubtypes.add(fingerprint)) {
            return;
        }

        LOGGER.info("Subtype {} -> {}", subtype, describeStack(stack));
    }

    static void logRecipeFocus(String phase, String mode, ItemStack stack, Set<String> keys, List<String> recipeDiagnostics) {
        if (!ENABLED) {
            return;
        }

        String fingerprint = "focus|" + phase + "|" + mode + "|" + describeStack(stack) + "|" + keys;
        if (!loggedRecipeFocuses.add(fingerprint)) {
            return;
        }

        LOGGER.info("Recipe focus phase={} mode={} stack={} keys={}", phase, mode, describeStack(stack), keys);
        for (String line : recipeDiagnostics) {
            LOGGER.info("Recipe diagnostic {}", line);
        }
    }

    static String describeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "<empty>";
        }

        ResourceLocation registryName = stack.getItem().getRegistryName();
        StringBuilder builder = new StringBuilder();
        builder.append(registryName == null ? "<unregistered>" : registryName.toString());
        builder.append("@").append(stack.getMetadata());
        builder.append("x").append(stack.getCount());
        builder.append(" rawBladeId=").append(BladeIdentity.getRawBladeId(stack));
        builder.append(" bladeId=").append(BladeIdentity.getBladeId(stack));

        if (stack.hasTagCompound()) {
            appendTagSummary(builder, stack.getTagCompound());
        } else {
            builder.append(" nbt=<none>");
        }

        return builder.toString();
    }

    private static void appendTagSummary(StringBuilder builder, NBTTagCompound tag) {
        builder.append(" nbtKeys=").append(tag.getKeySet());
        if (ItemSlashBlade.BladeId.exists(tag)) {
            builder.append(" BladeId=").append(ItemSlashBlade.BladeId.get(tag));
        }
        if (ItemSlashBladeNamed.CurrentItemName.exists(tag)) {
            builder.append(" CurrentItemName=").append(ItemSlashBladeNamed.CurrentItemName.get(tag));
        }
        if (ItemSlashBladeNamed.TrueItemName.exists(tag)) {
            builder.append(" TrueItemName=").append(ItemSlashBladeNamed.TrueItemName.get(tag));
        }
        if (ItemSlashBlade.TextureName.exists(tag)) {
            builder.append(" TextureName=").append(ItemSlashBlade.TextureName.get(tag));
        }
        if (ItemSlashBlade.ModelName.exists(tag)) {
            builder.append(" ModelName=").append(ItemSlashBlade.ModelName.get(tag));
        }
        builder.append(" isBroken=").append(ItemSlashBlade.IsBroken.get(tag));
        builder.append(" isNoScabbard=").append(ItemSlashBlade.IsNoScabbard.get(tag));
        builder.append(" isSealed=").append(ItemSlashBlade.IsSealed.get(tag));
        builder.append(" repair=").append(ItemSlashBlade.RepairCount.get(tag));
        builder.append(" kill=").append(ItemSlashBlade.KillCount.get(tag));
        builder.append(" proud=").append(ItemSlashBlade.ProudSoul.get(tag));
    }
}
