package me.geek.tom.thesewer;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.geek.tom.thesewer.game.CatchThePestConfig;
import me.geek.tom.thesewer.game.CatchThePestWaiting;

public class CatchThePest implements ModInitializer {

    public static final String ID = "catch-the-pest";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final GameType<CatchThePestConfig> TYPE = GameType.register(
            new Identifier(ID, "the-sewer"),
            CatchThePestWaiting::open,
            CatchThePestConfig.CODEC
    );

    @Override
    public void onInitialize() {}
}
