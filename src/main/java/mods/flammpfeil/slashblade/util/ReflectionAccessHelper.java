package mods.flammpfeil.slashblade.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Furia on 2016/02/03.
 */
public class ReflectionAccessHelper {
    private static final Method FORGE_INIT_METHOD = findForgeInitMethod();

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
        if(FORGE_INIT_METHOD != null) try {
            FORGE_INIT_METHOD.invoke(stack);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static Method findForgeInitMethod() {
        try {
            Method method = ItemStack.class.getDeclaredMethod("forgeInit");
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static void setVelocity(Entity entity, double x, double y, double z){
        entity.motionX = x;
        entity.motionY = y;
        entity.motionZ = z;
    }
}
