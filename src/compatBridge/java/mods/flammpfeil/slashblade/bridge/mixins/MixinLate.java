package mods.flammpfeil.slashblade.bridge.mixins;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MixinLate implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        List<String> mixins = new ArrayList<>();
        if (Loader.isModLoaded("slashblade_addon")) {
            mixins.add("mixins.flammpfeil.slashblade.bridge.sjap.json");
        }
        return mixins;
    }
}
