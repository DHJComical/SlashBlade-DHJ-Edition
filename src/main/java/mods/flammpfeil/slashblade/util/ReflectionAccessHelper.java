package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.mixin.ItemStackInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Furia on 2016/02/03.
 */
public class ReflectionAccessHelper {
    @SideOnly(Side.CLIENT)
    public static Timer timer;
    @SideOnly(Side.CLIENT)
    public static float getPartialTicks(){

        if(timer == null)
            timer = Minecraft.getMinecraft().timer;

        return timer.renderPartialTicks;
    }

    public static void setFire(Entity entity, int ticks) {
        entity.fire = ticks;
    }

    public static void setItem(ItemStack stack , Item item){
        stack.item = item;
        ((ItemStackInvoker) (Object) stack).slashblade$forgeInit();
    }

    public static void setVelocity(Entity entity, double x, double y, double z){
        entity.motionX = x;
        entity.motionY = y;
        entity.motionZ = z;
    }
}
