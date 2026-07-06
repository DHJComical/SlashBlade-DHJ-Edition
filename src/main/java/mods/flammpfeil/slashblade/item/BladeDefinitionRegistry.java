package mods.flammpfeil.slashblade.item;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class BladeDefinitionRegistry {
    private static final Map<String, BladeDefinition> definitions = Maps.newLinkedHashMap();
    private static final Map<String, String> aliases = Maps.newLinkedHashMap();
    private static final Map<String, ItemStack> prototypes = Maps.newLinkedHashMap();

    private BladeDefinitionRegistry() {
    }

    public static BladeDefinition registerDefinition(String bladeId, ItemStack prototype, boolean fixedBlade, boolean legacyBridge) {
        String canonicalBladeId = normalize(resolveAlias(StringUtils.defaultIfBlank(bladeId, BladeIdentity.getRawBladeId(prototype))));
        if (canonicalBladeId.isEmpty()) {
            throw new IllegalArgumentException("bladeId must not be empty");
        }

        BladeStateCodec.ensureBladeState(prototype, canonicalBladeId);

        Set<String> definitionAliases = new LinkedHashSet<String>();
        BladeDefinition existing = definitions.get(canonicalBladeId);
        if (existing != null) {
            definitionAliases.addAll(existing.getAliases());
        }
        definitionAliases.add(canonicalBladeId);

        BladeDefinition definition = BladeDefinition.fromPrototype(canonicalBladeId, prototype, fixedBlade, legacyBridge, definitionAliases);
        definitions.put(canonicalBladeId, definition);
        registerPrototype(canonicalBladeId, prototype);
        registerAlias(canonicalBladeId, canonicalBladeId);

        if (fixedBlade) {
            ResourceLocation registryName = prototype.getItem().getRegistryName();
            if (registryName != null) {
                registerAlias(registryName.toString(), canonicalBladeId);
            }
        }

        String legacyName = definition.getLegacyName();
        if (!legacyName.isEmpty()) {
            registerAlias(legacyName, canonicalBladeId);
        }

        return definitions.get(canonicalBladeId);
    }

    public static void registerPrototype(String key, ItemStack prototype) {
        String normalizedKey = normalize(key);
        if (normalizedKey.isEmpty() || prototype.isEmpty()) {
            return;
        }

        ItemStack copy = prototype.copy();
        if (copy.getItem() instanceof ItemSlashBlade) {
            BladeStateCodec.ensureBladeState(copy);
        }
        prototypes.put(normalizedKey, copy);
    }

    public static void registerAlias(String alias, String bladeId) {
        String normalizedAlias = normalize(alias);
        String normalizedBladeId = normalize(bladeId);
        if (normalizedAlias.isEmpty() || normalizedBladeId.isEmpty()) {
            return;
        }

        String canonicalBladeId = normalize(resolveAlias(normalizedBladeId));
        if (canonicalBladeId.isEmpty()) {
            canonicalBladeId = normalizedBladeId;
        }

        aliases.put(normalizedAlias, canonicalBladeId);

        BladeDefinition definition = definitions.get(canonicalBladeId);
        if (definition == null) {
            return;
        }

        if (!definition.getAliases().contains(normalizedAlias)) {
            Set<String> newAliases = new LinkedHashSet<String>(definition.getAliases());
            newAliases.add(normalizedAlias);
            definitions.put(canonicalBladeId, definition.withAliases(newAliases));
        }
    }

    public static String resolveAlias(String key) {
        String normalizedKey = normalize(key);
        if (normalizedKey.isEmpty()) {
            return "";
        }

        if (aliases.containsKey(normalizedKey)) {
            return aliases.get(normalizedKey);
        }

        if (definitions.containsKey(normalizedKey)) {
            return normalizedKey;
        }

        return normalizedKey;
    }

    @Nullable
    public static BladeDefinition get(String key) {
        String normalizedKey = normalize(resolveAlias(key));
        return definitions.get(normalizedKey);
    }

    public static boolean hasDefinition(String key) {
        return get(key) != null;
    }

    public static Collection<BladeDefinition> getDefinitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public static Set<String> getAliases(String key) {
        BladeDefinition definition = get(key);
        return definition == null ? Collections.<String>emptySet() : definition.getAliases();
    }

    public static ItemStack createBladeStack(String key) {
        String normalizedKey = normalize(key);
        if (normalizedKey.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (prototypes.containsKey(normalizedKey)) {
            return preparePrototypeCopy(prototypes.get(normalizedKey));
        }

        String canonicalBladeId = normalize(resolveAlias(normalizedKey));
        if (prototypes.containsKey(canonicalBladeId)) {
            return preparePrototypeCopy(prototypes.get(canonicalBladeId));
        }

        BladeDefinition definition = definitions.get(canonicalBladeId);
        if (definition == null) {
            return ItemStack.EMPTY;
        }

        return preparePrototypeCopy(definition.getCanonicalStack());
    }

    private static ItemStack preparePrototypeCopy(ItemStack prototype) {
        ItemStack copy = prototype.copy();
        if (copy.getItem() instanceof ItemSlashBlade) {
            BladeStateCodec.ensureBladeState(copy);
        }
        return copy;
    }

    private static String normalize(String key) {
        return StringUtils.trimToEmpty(key);
    }
}
