package com.jdolphin.holoprojector.client.screen;

import com.jdolphin.holoprojector.common.packet.HPPackets;
import com.jdolphin.holoprojector.common.packet.SBUpdateHologramPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class HologramScreen extends Screen {
    public static ResourceLocation BG_LOCATION = new ResourceLocation("holoprojector","textures/gui/hologram_bg.png");
    private final BlockPos pos;
    private final String name;
    private final boolean bSlim;
    private final boolean bLock;
    private final boolean bSolid;

    private EditBox nameInput;
    private Checkbox lock, slim, solid;
    private Button done, cancel;

    public HologramScreen(BlockPos pos, String name, boolean lock, boolean slim, boolean solid) {
        super(Component.translatable("screen.holoprojector.hologram"));
        this.pos = pos;
        this.name = name;
        this.bLock = lock;
        this.bSlim = slim;
        this.bSolid = solid;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void init() {
        super.init();

        this.nameInput = this.addWidget(new EditBox(this.font, this.width / 2 - 20, this.height / 2 - 64, 128, 20,
                Component.translatable("chat.editBox")));
        nameInput.setValue(name);
        this.lock = this.addWidget(new Checkbox(this.width / 2 - 20, this.height / 2 - 12, 20, 20, Component.empty(), bLock, false));
        this.slim = this.addWidget(new Checkbox(this.width / 2 + 128, this.height / 2 - 12, 20, 20, Component.empty(), bSlim, false));

        //this.player = this.addWidget(new Checkbox(this.width / 2 - 20, this.height / 2 + 12, 20, 20, Component.empty(), bPlayer, false));
        this.solid = this.addWidget(new Checkbox(this.width / 2 - 20, this.height / 2 + 12, 20, 20, Component.empty(), bSolid, false));


        done = this.addRenderableWidget(new Button.Builder(Component.literal("Done"), button -> {
            SBUpdateHologramPacket packet = new SBUpdateHologramPacket(pos, nameInput.getValue(), lock.selected(), slim.selected(), solid.selected());
            HPPackets.INSTANCE.sendToServer(packet);
            this.onClose();
        }).bounds(this.width / 2 - 128, this.height / 2 + 68, 128, 20).build());

        cancel = this.addRenderableWidget(new Button.Builder(Component.literal("Cancel"), button -> this.onClose())
                .bounds(this.width / 2 + 8, this.height / 2 + 68, 128, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.fill(this.width / 2 - 154, this.height / 2 - 110, this.width / 2 + 165, this.height / 2 + 100, Color.BLUE.darker().getRGB());
        nameInput.render(graphics, mouseX, mouseY, delta);
        this.lock.render(graphics, mouseX, mouseY, delta);
        this.slim.render(graphics, mouseX, mouseY, delta);
        this.solid.render(graphics, mouseX, mouseY, delta);

        graphics.renderOutline(done.getX(), done.getY(), done.getWidth(), done.getHeight(), Color.BLUE.brighter().getRGB());
        graphics.renderOutline(cancel.getX(), cancel.getY(), cancel.getWidth(), cancel.getHeight(), Color.BLUE.brighter().getRGB());

        graphics.drawString(this.font, "Player name: ", this.nameInput.getX() - 100, this.nameInput.getY() + 5, Color.WHITE.getRGB());

        graphics.drawCenteredString(this.font, "Lock projector: ", this.lock.getX() - 50, this.lock.getY() + 5, Color.WHITE.getRGB());
        graphics.drawCenteredString(this.font, "Use slim player model: ", this.slim.getX() - 55, this.slim.getY() + 5, Color.WHITE.getRGB());
        graphics.drawCenteredString(this.font, "Render solid: ", this.solid.getX() - 50, this.solid.getY() + 5, Color.WHITE.getRGB());

        RenderSystem.enableBlend();
        graphics.blit(BG_LOCATION, this.width / 2 - 158, this.height / 2 - 115, 0, 0, 330, 224, 330, 224);
        RenderSystem.disableBlend();
    }

}
