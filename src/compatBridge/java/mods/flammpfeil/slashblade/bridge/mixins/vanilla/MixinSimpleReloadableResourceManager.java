package mods.flammpfeil.slashblade.bridge.mixins.vanilla;

import mods.flammpfeil.slashblade.bridge.compat.LegacyTextureFallbacks;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mixin(value = SimpleReloadableResourceManager.class, remap = false)
public class MixinSimpleReloadableResourceManager {
    @Shadow
    private Map<String, FallbackResourceManager> domainResourceManagers;

    @Unique
    private static final Map<String, ResourceLocation> slashblade$resourceFallbackCache = new HashMap<>();
    @Unique
    private static int slashblade$cachedFallbackVersion = -1;

    @ModifyVariable(method = "getResource", at = @At("HEAD"), argsOnly = true)
    private ResourceLocation slashblade$getLegacyAddonResource(ResourceLocation location) {
        if (location == null || domainResourceManagers == null) {
            return location;
        }

        int fallbackVersion = LegacyTextureFallbacks.getVersion();
        if (slashblade$cachedFallbackVersion != fallbackVersion) {
            slashblade$resourceFallbackCache.clear();
            slashblade$cachedFallbackVersion = fallbackVersion;
        }

        String cacheKey = location.toString();
        if (slashblade$resourceFallbackCache.containsKey(cacheKey)) {
            return slashblade$resourceFallbackCache.get(cacheKey);
        }

        ResourceLocation resolved = LegacyTextureFallbacks.resolve(location, this::slashblade$resourceExists);
        slashblade$resourceFallbackCache.put(cacheKey, resolved);
        return resolved;
    }

    @Unique
    private boolean slashblade$resourceExists(ResourceLocation location) {
        FallbackResourceManager resourceManager = domainResourceManagers.get(location.getNamespace());
        if (resourceManager == null) {
            return false;
        }

        try {
            IResource resource = resourceManager.getResource(location);
            resource.getInputStream().close();
            return true;
        } catch (IOException | RuntimeException ignored) {
            return false;
        }
    }
}
