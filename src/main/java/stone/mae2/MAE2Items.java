package stone.mae2;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.parts.PartModels;
import appeng.items.materials.StorageComponentItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.items.storage.BasicStorageCell;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import stone.mae2.parts.PatternP2PTunnelPart;

public abstract class MAE2Items {

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister
        .create(Registries.CREATIVE_MODE_TAB, MAE2.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister
        .create(ForgeRegistries.ITEMS, MAE2.MODID);

    public static RegistryObject<PartItem<PatternP2PTunnelPart>> PATTERN_P2P_TUNNEL;
    public static RegistryObject<StorageComponentItem>[] STORAGE_COMPONENTS;
    public static RegistryObject<BasicStorageCell>[] STORAGE_CELLS;

    public static void init(IEventBus bus) {
        register();
        ITEMS.register(bus);
        TABS.register(bus);

        bus.addListener((FMLCommonSetupEvent event) ->
        {

            if (MAE2Config.isInterfaceP2PEnabled)
            {
                P2PTunnelAttunement
                    .registerAttunementTag(PATTERN_P2P_TUNNEL.get());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static void register() {
        // always registers pattern p2p for the creative tab's icon
        // TODO figure something out for that
        PATTERN_P2P_TUNNEL = Util.make(() ->
        {
            PartModels.registerModels(
                PartModelsHelper.createModels(PatternP2PTunnelPart.class));
            return ITEMS.register("pattern_p2p_tunnel",
                () -> new PartItem<>(new Item.Properties(),
                    PatternP2PTunnelPart.class, PatternP2PTunnelPart::new));
        });
        
        if (MAE2Config.areExtraTiersEnabled)
        {
            STORAGE_COMPONENTS = new RegistryObject[MAE2Config.extraStorageTiers];
            STORAGE_CELLS = new RegistryObject[MAE2Config.extraStorageTiers];
            MAE2.LOGGER.debug("Registering {} exra storage tiers!",
                MAE2Config.extraStorageTiers);
            for (int i = 0; i < MAE2Config.extraStorageTiers; i++)
            {
                final int tier = i;
                STORAGE_COMPONENTS[i] = ITEMS.register("storage_component_" + i,
                    () -> new StorageComponentItem(new Item.Properties(),
                        256 * (tier + 1) * 4));
            }
        }

    }

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = TABS
        .register(
        "main",
        () -> CreativeModeTab.builder()
            .title(
                Component.translatable("gui." + MAE2.MODID + ".creative_tab"))
            .icon(() -> new ItemStack(PATTERN_P2P_TUNNEL.get()))
            .displayItems((params, output) ->
            {
                for (var entry : ITEMS.getEntries())
                {
                    output.accept(entry.get());
                }
            }).build());
}
