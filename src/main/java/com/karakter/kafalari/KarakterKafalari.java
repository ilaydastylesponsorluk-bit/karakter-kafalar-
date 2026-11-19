package com.karakter.kafalari;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(KarakterKafalari.MODID)
public class KarakterKafalari {
    public static final String MODID = "karakterkafalari";

    // Yeni kafa eklemek için: isim buraya + textures/block/<isim>.png
    public static final String[] KAFALAR = { "koylu_kafa", "mavi_kiz_kafa", "beyaz_gozlu_kafa", "salak_surat_kafa", "iskelet_surat_kafa", "maske_kafa" };

    // Moblar: isim, yumurta rengi 1, yumurta rengi 2 (skin: textures/entity/<isim>.png)
    private static final Object[][] MOB_LISTESI = {
            { "koylu", 0x6E4B32, 0x1BAA1B },
            { "mavi_kiz", 0x28AAC8, 0x111111 },
            { "beyaz_gozlu", 0x00AAAA, 0xFFFFFF },
            { "salak_surat", 0x3CA03C, 0xE01010 },
            { "iskelet_surat", 0xEEEEEE, 0x111111 },
            { "maske", 0x111111, 0xFFFFFF },
    };

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final List<DeferredBlock<? extends KafaBlock>> KAFA_BLOKLARI = new ArrayList<>();
    public static final List<DeferredHolder<EntityType<?>, EntityType<KarakterMob>>> MOBLAR = new ArrayList<>();
    public static final List<DeferredItem<? extends Item>> YUMURTALAR = new ArrayList<>();

    static {
        for (String isim : KAFALAR) {
            boolean parlak = isim.equals("beyaz_gozlu_kafa");
            DeferredBlock<KafaBlock> blok = BLOCKS.register(isim, () -> new KafaBlock(
                    BlockBehaviour.Properties.of().strength(1.0f).sound(SoundType.WOOL)
                            .lightLevel(s -> parlak ? 15 : 0)));
            ITEMS.register(isim, () -> new KafaItem(blok.get(), new Item.Properties()));
            KAFA_BLOKLARI.add(blok);
        }

        // Korku tuzağı: Beyaz Gözlü kafanın aynısı gibi görünür
        DeferredBlock<KorkuBlock> korku = BLOCKS.register("korku_kafa", () -> new KorkuBlock(
                BlockBehaviour.Properties.of().strength(1.0f).sound(SoundType.WOOL).lightLevel(s -> 15)));
        ITEMS.register("korku_kafa", () -> new KafaItem(korku.get(), new Item.Properties()));
        KAFA_BLOKLARI.add(korku);

        for (Object[] m : MOB_LISTESI) {
            String isim = (String) m[0];
            DeferredHolder<EntityType<?>, EntityType<KarakterMob>> tip = ENTITIES.register(isim,
                    () -> EntityType.Builder.of(KarakterMob::new, MobCategory.CREATURE).sized(0.6f, 1.8f).build(isim));
            MOBLAR.add(tip);
            YUMURTALAR.add(ITEMS.register(isim + "_yumurta",
                    () -> new DeferredSpawnEggItem(tip, (Integer) m[1], (Integer) m[2], new Item.Properties())));
        }
    }

    public static final Supplier<CreativeModeTab> SEKME = TABS.register("kafalar", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.karakterkafalari"))
            .icon(() -> KAFA_BLOKLARI.get(0).get().asItem().getDefaultInstance())
            .displayItems((params, output) -> {
                KAFA_BLOKLARI.forEach(b -> output.accept(b.get()));
                YUMURTALAR.forEach(y -> output.accept(y.get()));
            })
            .build());

    public KarakterKafalari(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        TABS.register(modBus);

        modBus.addListener(KarakterKafalari::ozellikler);
        modBus.addListener(KarakterKafalari::dogmaKurallari);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(KarakterRenderer::kaydet);
        }
    }

    private static void ozellikler(EntityAttributeCreationEvent event) {
        for (var tip : MOBLAR) {
            event.put(tip.get(), KarakterMob.ozellikler().build());
        }
    }

    private static void dogmaKurallari(RegisterSpawnPlacementsEvent event) {
        for (var tip : MOBLAR) {
            event.register(tip.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }
}
