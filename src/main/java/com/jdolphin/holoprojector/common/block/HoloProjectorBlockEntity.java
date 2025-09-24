package com.jdolphin.holoprojector.common.block;

import com.jdolphin.holoprojector.common.HoloProjector;
import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.awt.Color;
import java.util.UUID;

public class HoloProjectorBlockEntity extends BlockEntity {
    private boolean lock = false, solid = false, slim = false;
    private GameProfile targetPlayer = null;
    private int color = new Color(0.4f, 0.4f, 1, 0.6f).getRGB();
    private UUID owner = Util.NIL_UUID;

    public HoloProjectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    public boolean isSlim() {
        return slim;
    }

    public boolean isSolid() {
        return solid;
    }

    public GameProfile getTargetPlayer() {
        return targetPlayer;
    }

    public boolean isLocked() {
        return lock;
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    public void setChanged() {
        if (level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        super.setChanged();
    }

    public void updateProjector(GameProfile profile, boolean lock, boolean slim, boolean solid) {
        this.targetPlayer = profile;
        this.lock = lock;
        this.slim = slim;
        this.solid = solid;
        this.setChanged();
    }

    public HoloProjectorBlockEntity(BlockPos pos, BlockState state) {
        this(HoloProjector.PROJECTOR_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Projected")) this.targetPlayer = NbtUtils.readGameProfile(tag.getCompound("Projected"));
        this.lock = tag.getBoolean("Locked");
        this.slim = tag.getBoolean("Slim");
        this.solid = tag.getBoolean("Solid");
        this.owner = tag.getUUID("Owner");
        if (tag.contains("Color")) {
            this.color = tag.getInt("Color");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (targetPlayer != null) {
            CompoundTag compoundtag = new CompoundTag();
            tag.put("Projected", NbtUtils.writeGameProfile(compoundtag, this.targetPlayer));
        }
        tag.putBoolean("Locked", lock);
        tag.putBoolean("Slim", slim);
        tag.putBoolean("Solid", solid);
        tag.putUUID("Owner", owner);
        tag.putInt("Color", this.color);
    }
}
