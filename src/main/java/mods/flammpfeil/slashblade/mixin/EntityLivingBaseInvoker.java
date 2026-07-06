package mods.flammpfeil.slashblade.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityLivingBase.class)
public interface EntityLivingBaseInvoker {
    @Invoker("jump")
    void slashblade$jump();

    @Invoker("getExperiencePoints")
    int slashblade$getExperiencePoints(EntityPlayer attackingPlayer);
}
