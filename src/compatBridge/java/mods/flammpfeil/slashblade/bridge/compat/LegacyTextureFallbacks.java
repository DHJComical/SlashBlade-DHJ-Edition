package mods.flammpfeil.slashblade.bridge.compat;

import mods.flammpfeil.slashblade.util.ResourceLocationRaw;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LegacyTextureFallbacks {
    public interface ResourceExists {
        boolean test(ResourceLocation location);
    }

    private static final List<DomainFallback> FALLBACKS = new ArrayList<>();
    private static int version;

    private LegacyTextureFallbacks() {
    }

    public static synchronized void registerDomainFallback(String sourceDomain, String fallbackDomain, String pathPrefix, String pathSuffix) {
        DomainFallback fallback = new DomainFallback(sourceDomain, fallbackDomain, pathPrefix, pathSuffix);
        if (FALLBACKS.contains(fallback)) {
            return;
        }

        FALLBACKS.add(fallback);
        version++;
    }

    public static synchronized int getVersion() {
        return version;
    }

    public static ResourceLocation resolve(ResourceLocation location, ResourceExists resourceExists) {
        if (location == null || resourceExists.test(location)) {
            return location;
        }

        for (DomainFallback fallback : getFallbacks()) {
            ResourceLocation resolved = fallback.resolve(location, resourceExists);
            if (resolved != location) {
                return resolved;
            }
        }

        return location;
    }

    private static synchronized List<DomainFallback> getFallbacks() {
        return Collections.unmodifiableList(new ArrayList<>(FALLBACKS));
    }

    private static final class DomainFallback {
        private final String sourceDomain;
        private final String fallbackDomain;
        private final String pathPrefix;
        private final String pathSuffix;

        private DomainFallback(String sourceDomain, String fallbackDomain, String pathPrefix, String pathSuffix) {
            this.sourceDomain = sourceDomain;
            this.fallbackDomain = fallbackDomain;
            this.pathPrefix = pathPrefix;
            this.pathSuffix = pathSuffix;
        }

        private ResourceLocation resolve(ResourceLocation location, ResourceExists resourceExists) {
            if (!sourceDomain.equals(location.getNamespace())
                    || !location.getPath().startsWith(pathPrefix)
                    || !location.getPath().endsWith(pathSuffix)) {
                return location;
            }

            ResourceLocation fallbackLocation = new ResourceLocationRaw(fallbackDomain, location.getPath());
            return resourceExists.test(fallbackLocation) ? fallbackLocation : location;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DomainFallback)) {
                return false;
            }

            DomainFallback that = (DomainFallback) obj;
            return sourceDomain.equals(that.sourceDomain)
                    && fallbackDomain.equals(that.fallbackDomain)
                    && pathPrefix.equals(that.pathPrefix)
                    && pathSuffix.equals(that.pathSuffix);
        }

        @Override
        public int hashCode() {
            int result = sourceDomain.hashCode();
            result = 31 * result + fallbackDomain.hashCode();
            result = 31 * result + pathPrefix.hashCode();
            result = 31 * result + pathSuffix.hashCode();
            return result;
        }
    }
}
