package mods.flammpfeil.slashblade.bridge.mixins.sjap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "cn.mmf.slashblade_addon.ClientProxy", remap = false)
public class MixinClientProxy {
    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(stringValue = "flammpfeil.slashblade:model/named/blade.obj"),
            remap = false
    )
    private static String slashblade$redirectBladeModelDomain(String value) {
        return "slashblade:model/named/blade.obj";
    }

    @ModifyConstant(
            method = "preInit",
            constant = @Constant(stringValue = "flammpfeil.slashblade:sphere.obj"),
            remap = false
    )
    private String slashblade$redirectSphereModelDomain(String value) {
        return "slashblade:sphere.obj";
    }
}
