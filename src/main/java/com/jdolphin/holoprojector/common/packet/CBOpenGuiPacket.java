package com.jdolphin.holoprojector.common.packet;

import com.jdolphin.holoprojector.client.screen.HologramScreen;
import com.jdolphin.holoprojector.common.HoloProjector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CBOpenGuiPacket(BlockPos pos, String target, boolean locked, boolean slim, boolean solid) implements CustomPacketPayload {
    public static final Type<CBOpenGuiPacket> TYPE = new Type<>(HoloProjector.id("open_gui"));
    public static final StreamCodec<FriendlyByteBuf, CBOpenGuiPacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, CBOpenGuiPacket::pos,
            ByteBufCodecs.STRING_UTF8, CBOpenGuiPacket::target,
            ByteBufCodecs.BOOL, CBOpenGuiPacket::locked,
            ByteBufCodecs.BOOL, CBOpenGuiPacket::slim,
            ByteBufCodecs.BOOL, CBOpenGuiPacket::solid, CBOpenGuiPacket::new);

    public static void handle(CBOpenGuiPacket packet, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new HologramScreen(packet.pos, packet.target, packet.locked, packet.slim, packet.solid));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
