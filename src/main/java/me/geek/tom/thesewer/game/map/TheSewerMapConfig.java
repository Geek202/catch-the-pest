package me.geek.tom.thesewer.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class TheSewerMapConfig {
    public static final Codec<TheSewerMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock)
    ).apply(instance, TheSewerMapConfig::new));

    public final BlockState spawnBlock;

    public TheSewerMapConfig(BlockState spawnBlock) {
        this.spawnBlock = spawnBlock;
    }
}
