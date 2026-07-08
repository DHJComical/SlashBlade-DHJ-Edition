package mods.flammpfeil.slashblade.bridge.core;

import mods.flammpfeil.slashblade.bridge.compat.LegacyTextureFallbacks;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("SlashBlade Legacy Compatibility Remapper")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
public class LegacySlashBladeCorePlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        LegacyTextureFallbacks.registerDomainFallback("slashblade", "flammpfeil.slashblade", "model/", ".png");
        LegacyTextureFallbacks.registerDomainFallback("slashblade", "flammpfeil.slashblade", "model/", ".obj");
        return Collections.singletonList("mixins.flammpfeil.slashblade.bridge.vanilla.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { LegacySlashBladeClassTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
