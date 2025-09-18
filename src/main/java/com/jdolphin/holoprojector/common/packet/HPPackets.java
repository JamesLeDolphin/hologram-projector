package com.jdolphin.holoprojector.common.packet;

import com.jdolphin.holoprojector.common.HoloProjector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class HPPackets {
    static int index = 0;

    private static final String PROTOCOL_VERSION = "1";

    public static void sendTo(ServerPlayer player, Object packet) {
        INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HoloProjector.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void init() {
        INSTANCE.messageBuilder(SBUpdateHologramPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SBUpdateHologramPacket::encode)
                .decoder(SBUpdateHologramPacket::new)
                .consumerMainThread(SBUpdateHologramPacket::handle)
                .add();

        INSTANCE.messageBuilder(CBOpenGuiPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CBOpenGuiPacket::encode)
                .decoder(CBOpenGuiPacket::new)
                .consumerMainThread(CBOpenGuiPacket::handle)
                .add();
    }
}
