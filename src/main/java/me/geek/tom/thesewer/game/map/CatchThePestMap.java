package me.geek.tom.thesewer.game.map;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class CatchThePestMap {
    private final MapTemplate template;
    private final CatchThePestMapConfig config;
    public BlockBounds hunterSpawn;
    public BlockBounds pestSpawn;

    public CatchThePestMap(MapTemplate template, CatchThePestMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
