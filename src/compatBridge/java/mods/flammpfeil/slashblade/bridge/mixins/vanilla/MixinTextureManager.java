package mods.flammpfeil.slashblade.bridge.mixins.vanilla;

import mods.flammpfeil.slashblade.bridge.compat.LegacyTextureFallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mixin(value = TextureManager.class, remap = false)
public class MixinTextureManager {
    @Unique
    private static final Map<String, ResourceLocation> slashblade$textureFallbackCache = new HashMap<>();
    @Unique
    private static int slashblade$cachedFallbackVersion = -1;

    @ModifyVariable(method = "bindTexture", at = @At("HEAD"), argsOnly = true)
    private ResourceLocation slashblade$bindLegacyAddonTexture(ResourceLocation location) {
        return slashblade$resolveLegacyTexture(location);
    }

    @Unique
    private static ResourceLocation slashblade$resolveLegacyTexture(ResourceLocation location) {
        if (location == null || Minecraft.getMinecraft().getResourceManager() == null) {
            return location;
        }

        int fallbackVersion = LegacyTextureFallbacks.getVersion();
        if (slashblade$cachedFallbackVersion != fallbackVersion) {
            slashblade$textureFallbackCache.clear();
            slashblade$cachedFallbackVersion = fallbackVersion;
        }

        String cacheKey = location.toString();
        if (slashblade$textureFallbackCache.containsKey(cacheKey)) {
            return slashblade$textureFallbackCache.get(cacheKey);
        }

        ResourceLocation resolved = LegacyTextureFallbacks.resolve(location, MixinTextureManager::slashblade$resourceExists);
        slashblade$textureFallbackCache.put(cacheKey, resolved);
        return resolved;
    }

    @Unique
    private static boolean slashblade$resourceExists(ResourceLocation location) {
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            resource.getInputStream().close();
            return true;
        } catch (IOException | RuntimeException ignored) {
            return false;
        }
    }
}
