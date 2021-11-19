package cofh.redstonearsenal;

import cofh.lib.util.DeferredRegisterCoFH;
import cofh.redstonearsenal.client.renderer.*;
import cofh.redstonearsenal.init.RSABlocks;
import cofh.redstonearsenal.init.RSAConfig;
import cofh.redstonearsenal.init.RSAEntities;
import cofh.redstonearsenal.init.RSAItems;
import cofh.redstonearsenal.network.packet.server.LeftClickPacket;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cofh.core.CoFHCore.PACKET_HANDLER;
import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.lib.util.constants.Constants.PACKET_LEFT_CLICK;
import static cofh.redstonearsenal.init.RSAReferences.*;

@Mod(ID_REDSTONE_ARSENAL)
public class RedstoneArsenal {

    public static final Logger LOG = LogManager.getLogger(ID_REDSTONE_ARSENAL);

    public static final DeferredRegisterCoFH<Block> BLOCKS = DeferredRegisterCoFH.create(ForgeRegistries.BLOCKS, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<Item> ITEMS = DeferredRegisterCoFH.create(ForgeRegistries.ITEMS, ID_REDSTONE_ARSENAL);
    public static final DeferredRegisterCoFH<EntityType<?>> ENTITIES = DeferredRegisterCoFH.create(ForgeRegistries.ENTITIES, ID_REDSTONE_ARSENAL);

    public static ItemGroup itemGroup;

    public RedstoneArsenal() {

        registerPackets();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);

        RSABlocks.register();
        RSAItems.register();
        RSAEntities.register();
    }

    private void registerPackets() {

        PACKET_HANDLER.registerPacket(PACKET_LEFT_CLICK, LeftClickPacket::new);
    }

    // region INITIALIZATION
    private void commonSetup(final FMLCommonSetupEvent event) {

        RSAConfig.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {

        //        if (RSAConfig.enableCreativeTab.get()) {
        //            itemGroup = new ItemGroup(-1, ID_REDSTONE_ARSENAL) {
        //
        //                @Override
        //                @OnlyIn(Dist.CLIENT)
        //                public ItemStack makeIcon() {
        //
        //                    return new ItemStack(ITEMS.get("flux_sword"));
        //                }
        //            };
        //        }

        registerEntityRenderingHandlers();
        registerRenderLayers();
    }
    // endregion

    //region HELPERS
    private void registerEntityRenderingHandlers() {

        RenderingRegistry.registerEntityRenderingHandler(FLUX_SLASH_ENTITY, FluxSlashRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(FLUX_ARROW_ENTITY, FluxArrowRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(FLUX_TRIDENT_ENTITY, FluxTridentRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(FLUX_WRENCH_ENTITY, FluxWrenchRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SHOCKWAVE_ENTITY, ShockwaveRenderer::new);
    }

    private void registerRenderLayers() {

        for (PlayerRenderer renderer : Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values()) {
            renderer.addLayer(new FluxElytraLayer<>(renderer));
        }
    }
    //endregion
}
