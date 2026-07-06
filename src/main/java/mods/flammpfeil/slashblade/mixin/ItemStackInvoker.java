package mods.flammpfeil.slashblade.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStack.class)
public interface ItemStackInvoker {
    @Invoker(value = "forgeInit", remap = false)
    void slashblade$forgeInit();
}
