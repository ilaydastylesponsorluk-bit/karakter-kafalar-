package com.karakter.kafalari;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(KarakterKafalari.MODID)
public class KarakterKafalari {
    public static final String MODID = "karakterkafalari";

    // Yeni kafa eklemek için buraya ismini yaz (dokular: <isim>_front/_side/_top.png)
    public static final String[] KAFALAR = { "koylu_kafa", "mavi_kiz_kafa" };

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final List<DeferredBlock<KafaBlock>> KAFA_BLOKLARI = new ArrayList<>();

    static {
        for (String isim : KAFALAR) {
            DeferredBlock<KafaBlock> blok = BLOCKS.register(isim, () -> new KafaBlock(
                    BlockBehaviour.Properties.of().strength(1.0f).sound(SoundType.WOOL)));
            ITEMS.registerSimpleBlockItem(isim, blok);
            KAFA_BLOKLARI.add(blok);
        }
    }

    public static final java.util.function.Supplier<CreativeModeTab> SEKME = TABS.register("kafalar", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.karakterkafalari"))
            .icon(() -> KAFA_BLOKLARI.get(0).get().asItem().getDefaultInstance())
            .displayItems((params, output) -> KAFA_BLOKLARI.forEach(b -> output.accept(b.get())))
            .build());

    public KarakterKafalari(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
