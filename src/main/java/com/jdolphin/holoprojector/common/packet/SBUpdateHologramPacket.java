package com.jdolphin.holoprojector.common.packet;

import com.jdolphin.holoprojector.common.HoloProjector;
import com.jdolphin.holoprojector.common.block.HoloProjectorBlockEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record SBUpdateHologramPacket(BlockPos pos, String target, boolean lock, boolean slim, boolean solid) implements CustomPacketPayload {
    public static final Type<SBUpdateHologramPacket> TYPE = new Type<>(HoloProjector.id("update"));

    public static final StreamCodec<FriendlyByteBuf, SBUpdateHologramPacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, SBUpdateHologramPacket::pos,
            ByteBufCodecs.STRING_UTF8, SBUpdateHologramPacket::target,
            ByteBufCodecs.BOOL, SBUpdateHologramPacket::lock,
            ByteBufCodecs.BOOL, SBUpdateHologramPacket::slim,
            ByteBufCodecs.BOOL, SBUpdateHologramPacket::solid, SBUpdateHologramPacket::new);

    public static void handle(SBUpdateHologramPacket packet, IPayloadContext context) {
        Player player = context.player();
        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            BlockEntity be = level.getBlockEntity(packet.pos);
            if (be instanceof HoloProjectorBlockEntity projector) {
                CompletableFuture<Optional<GameProfile>> completable = SkullBlockEntity.fetchGameProfile(packet.target);
                context.enqueueWork(() -> {
                    completable.thenAccept(optional -> {
                        if (optional.isPresent()) {
                            GameProfile profile = optional.get();
                            ResolvableProfile resolvableProfile = new ResolvableProfile(profile);
                            resolvableProfile.resolve().thenAccept(resolvableProfile1 ->
                                    projector.updateProjector(resolvableProfile1, packet.lock, packet.slim, packet.solid));
                        }
                    });
                });
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
