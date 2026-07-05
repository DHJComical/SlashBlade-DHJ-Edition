package mods.flammpfeil.slashblade.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by Furia on 14/06/09.
 */
public class NetworkManager {
    //public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(SlashBlade.modid);
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("flammpfeil.sb");


    public static void init() {
        INSTANCE.registerMessage(MessageRangeAttackHandler.class, MessageRangeAttack.class, 0, Side.SERVER);
        INSTANCE.registerMessage(MessageSpecialActionHandler.class, MessageSpecialAction.class, 1, Side.SERVER);
        INSTANCE.registerMessage(MessageMoveCommandStateHandler.class, MessageMoveCommandState.class, 2, Side.SERVER);
        INSTANCE.registerMessage(MessageRankpointSynchronizeHandler.class, MessageRankpointSynchronize.class, 3, Side.CLIENT);
    }
}
