package com.jdolphin.holoprojector.common;

import com.jdolphin.holoprojector.client.HoloProjectorRenderer;
import com.jdolphin.holoprojector.common.block.HoloProjectorBlock;
import com.jdolphin.holoprojector.common.block.HoloProjectorBlockEntity;
import com.jdolphin.holoprojector.common.packet.CBOpenGuiPacket;
import com.jdolphin.holoprojector.common.packet.SBUpdateHologramPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(HoloProjector.MODID)
public class HoloProjector {
    public static final String MODID = "holoprojector";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String s) {
        return ResourceLocation.tryBuild(MODID, s);
    }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BE = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<Block> PROJECTOR = BLOCKS.register("projector",
            () -> new HoloProjectorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5F).emissiveRendering((state, getter, pos) -> true)));

    public static final Supplier<Item> PROJECTOR_ITEM = ITEMS.register("projector", () -> new BlockItem(PROJECTOR.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<HoloProjectorBlockEntity>> PROJECTOR_ENTITY = BE.register("projector",
            () -> BlockEntityType.Builder.of(HoloProjectorBlockEntity::new, PROJECTOR.get()).build(null));


    public HoloProjector(IEventBus bus) {
        bus.register(this);

        BLOCKS.register(bus);
        ITEMS.register(bus);
        BE.register(bus);

        bus.addListener(this::addCreative);
    }

    @SubscribeEvent
    private void commonSetup(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.commonToClient(CBOpenGuiPacket.TYPE, CBOpenGuiPacket.CODEC, CBOpenGuiPacket::handle);
        registrar.commonToServer(SBUpdateHologramPacket.TYPE, SBUpdateHologramPacket.CODEC, SBUpdateHologramPacket::handle);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
            event.accept(PROJECTOR_ITEM.get());
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(PROJECTOR_ENTITY.get(), context -> new HoloProjectorRenderer());
        }
    }
}
