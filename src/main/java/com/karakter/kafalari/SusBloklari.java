package com.karakter.kafalari;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Harf, emoji ve bayrak blokları. Listeler derleme sırasında build.gradle tarafından üretilir. */
public final class SusBloklari {
    private SusBloklari() {}

    public static final List<DeferredBlock<Block>> HARFLER = kaydet("harfler", false);
    public static final List<DeferredBlock<Block>> EMOJILER = kaydet("emojiler", true);
    public static final List<DeferredBlock<Block>> BAYRAKLAR = kaydet("bayraklar", false);

    public static final Supplier<CreativeModeTab> HARF_SEKME = sekme("harfler", "Harfler", HARFLER);
    public static final Supplier<CreativeModeTab> EMOJI_SEKME = sekme("emojiler", "Emojiler", EMOJILER);
    public static final Supplier<CreativeModeTab> BAYRAK_SEKME = sekme("bayraklar", "Bayraklar", BAYRAKLAR);

    /** Ana sınıf bu metodu çağırınca yukarıdaki alanlar kaydedilir. */
    public static void yukle() {}

    /** Ad gösteren blok eşyası (dil dosyasına gerek kalmadan). */
    public static class SusItem extends BlockItem {
        private final String ad;

        public SusItem(Block block, Properties properties, String ad) {
            super(block, properties);
            this.ad = ad;
        }

        @Override
        public Component getName(ItemStack stack) {
            return Component.literal(ad);
        }
    }

    private static List<DeferredBlock<Block>> kaydet(String grup, boolean seffaf) {
        List<DeferredBlock<Block>> liste = new ArrayList<>();
        for (String satir : oku(grup)) {
            String[] p = satir.split("\\|", 2);
            String id = p[0].trim();
            String ad = p.length > 1 ? p[1].trim() : id;
            DeferredBlock<Block> blok = KarakterKafalari.BLOCKS.register(id, () -> {
                BlockBehaviour.Properties ozellik = BlockBehaviour.Properties.of().strength(0.8f).sound(SoundType.WOOL);
                if (seffaf) {
                    ozellik = ozellik.noOcclusion().isViewBlocking((s, l, pos) -> false).isSuffocating((s, l, pos) -> false);
                }
                return new Block(ozellik);
            });
            KarakterKafalari.ITEMS.register(id, () -> new SusItem(blok.get(), new Item.Properties(), ad));
            liste.add(blok);
        }
        return liste;
    }

    private static List<String> oku(String grup) {
        List<String> satirlar = new ArrayList<>();
        try (InputStream in = SusBloklari.class.getResourceAsStream("/karakterkafalari_listeler/" + grup + ".txt")) {
            if (in == null) {
                return satirlar;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String s;
            while ((s = r.readLine()) != null) {
                if (!s.isBlank()) {
                    satirlar.add(s);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Liste okunamadı: " + grup, e);
        }
        return satirlar;
    }

    private static Supplier<CreativeModeTab> sekme(String id, String baslik, List<DeferredBlock<Block>> bloklar) {
        return KarakterKafalari.TABS.register(id, () -> CreativeModeTab.builder()
                .title(Component.literal(baslik))
                .icon(() -> bloklar.isEmpty() ? ItemStack.EMPTY : bloklar.get(0).get().asItem().getDefaultInstance())
                .displayItems((params, output) -> bloklar.forEach(b -> output.accept(b.get())))
                .build());
    }
}
