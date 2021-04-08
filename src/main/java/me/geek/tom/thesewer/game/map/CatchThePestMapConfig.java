package me.geek.tom.thesewer.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public class CatchThePestMapConfig {
    public static final Codec<CatchThePestMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("map_template").forGetter(config -> config.mapTemplate)
    ).apply(instance, CatchThePestMapConfig::new));

    public final Identifier mapTemplate;

    public CatchThePestMapConfig(Identifier mapTemplate) {
        this.mapTemplate = mapTemplate;
    }
}
