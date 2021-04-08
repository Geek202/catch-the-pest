package me.geek.tom.thesewer.game.map;

import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;

import java.io.IOException;

public class CatchThePestMapGenerator {

    private final CatchThePestMapConfig config;

    public CatchThePestMapGenerator(CatchThePestMapConfig config) {
        this.config = config;
    }

    public CatchThePestMap build() {
        try {
            MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.mapTemplate);
            CatchThePestMap map = new CatchThePestMap(template, this.config);
            TemplateRegion pestSpawnRegion = template.getMetadata().getFirstRegion("pest_spawn");
            TemplateRegion hunterSpawnRegion = template.getMetadata().getFirstRegion("hunter_spawn");
            if (pestSpawnRegion != null) {
                map.pestSpawn = pestSpawnRegion.getBounds();
            } else {
                throw new GameOpenException(new LiteralText("No pest spawn is defined in the map!"));
            }
            if (hunterSpawnRegion != null) {
                map.hunterSpawn = hunterSpawnRegion.getBounds();
            } else {
                throw new GameOpenException(new LiteralText("No hunter spawn is defined in the map!"));
            }
            return map;
        } catch (IOException e) {
            throw new GameOpenException(new LiteralText("Failed to load map!"), e);
        }
    }
}
