package me.geek.tom.thesewer.game.map;

import xyz.nucleoid.plasmid.map.template.MapTemplate;
import net.minecraft.util.math.BlockPos;

public class TheSewerMapGenerator {

    private final TheSewerMapConfig config;

    public TheSewerMapGenerator(TheSewerMapConfig config) {
        this.config = config;
    }

    public TheSewerMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        TheSewerMap map = new TheSewerMap(template, this.config);

        this.buildSpawn(template);
        map.spawn = new BlockPos(0,65,0);

        return map;
    }

    private void buildSpawn(MapTemplate builder) {
        BlockPos min = new BlockPos(-5, 64, -5);
        BlockPos max = new BlockPos(5, 64, 5);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            builder.setBlockState(pos, this.config.spawnBlock);
        }
    }
}
