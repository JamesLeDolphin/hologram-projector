package com.jdolphin.holoprojector.common.packet;

import com.jdolphin.holoprojector.common.block.HoloProjectorBlockEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class SBUpdateHologramPacket {
    private final BlockPos pos;
    private final boolean slim;
    private final boolean lock;
    private final boolean solid;
    private final String target;

    public SBUpdateHologramPacket(BlockPos pos, String target, boolean lock, boolean slim, boolean solid) {
        this.pos = pos;
        this.target = target;
        this.lock = lock;
        this.slim = slim;
        this.solid = solid;
    }

    public SBUpdateHologramPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.target = buf.readUtf();
        this.lock = buf.readBoolean();
        this.slim = buf.readBoolean();
        this.solid = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeUtf(target).writeBoolean(lock)
                .writeBoolean(slim).writeBoolean(solid);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ServerLevel level = player.serverLevel();
            BlockEntity be = level.getBlockEntity(this.pos);
            if (be instanceof HoloProjectorBlockEntity projector) {
                if (level.getServer().getProfileCache() != null) {
                    Optional<GameProfile> gameProfile = level.getServer().getProfileCache().get(target);
                    if (gameProfile.isPresent()) {
                        GameProfile profile = gameProfile.get();
                        SkullBlockEntity.updateGameprofile(profile, profile1 -> projector.updateProjector(profile1, lock, slim, solid));
                    }
                }
            }
        }
    }
}
