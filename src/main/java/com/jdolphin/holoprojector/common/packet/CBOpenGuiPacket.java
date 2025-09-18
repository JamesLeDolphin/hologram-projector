package com.jdolphin.holoprojector.common.packet;

import com.jdolphin.holoprojector.client.screen.HologramScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CBOpenGuiPacket {
    private final BlockPos pos;
    private final boolean slim;
    private final boolean lock;
    private final boolean solid;
    private final String target;

    public CBOpenGuiPacket(BlockPos pos, String target, boolean locked, boolean slim, boolean solid) {
        this.pos = pos;
        this.target = target;
        this.lock = locked;
        this.slim = slim;
        this.solid = solid;
    }

    public CBOpenGuiPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.target = buf.readUtf();
        this.lock = buf.readBoolean();
        this.slim = buf.readBoolean();
        this.solid = buf.readBoolean();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection().equals(NetworkDirection.PLAY_TO_CLIENT)) {
            handleClient();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft.getInstance().setScreen(new HologramScreen(pos, target, lock, slim, solid));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeUtf(target).writeBoolean(lock)
                .writeBoolean(slim).writeBoolean(solid);
    }
}
