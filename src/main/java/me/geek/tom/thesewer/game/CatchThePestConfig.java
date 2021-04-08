package me.geek.tom.thesewer.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import me.geek.tom.thesewer.game.map.CatchThePestMapConfig;

public class CatchThePestConfig {
    public static final Codec<CatchThePestConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            CatchThePestMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs)
    ).apply(instance, CatchThePestConfig::new));

    public final PlayerConfig playerConfig;
    public final CatchThePestMapConfig mapConfig;
    public final int timeLimitSecs;

    public CatchThePestConfig(PlayerConfig players, CatchThePestMapConfig mapConfig, int timeLimitSecs) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimitSecs = timeLimitSecs;
    }
}
