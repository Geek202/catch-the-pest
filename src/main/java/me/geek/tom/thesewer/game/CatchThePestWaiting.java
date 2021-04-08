package me.geek.tom.thesewer.game;

import net.minecraft.util.ActionResult;
import net.minecraft.world.GameRules;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import me.geek.tom.thesewer.game.map.CatchThePestMap;
import me.geek.tom.thesewer.game.map.CatchThePestMapGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

public class CatchThePestWaiting {
    private final GameSpace gameSpace;
    private final CatchThePestMap map;
    private final CatchThePestConfig config;
    private final CatchThePestSpawnLogic spawnLogic;

    private CatchThePestWaiting(GameSpace gameSpace, CatchThePestMap map, CatchThePestConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new CatchThePestSpawnLogic(gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<CatchThePestConfig> context) {
        CatchThePestConfig config = context.getConfig();
        CatchThePestMapGenerator generator = new CatchThePestMapGenerator(config.mapConfig);
        CatchThePestMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setGameRule(GameRules.NATURAL_REGENERATION, false)
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            CatchThePestWaiting waiting = new CatchThePestWaiting(game.getSpace(), map, context.getConfig());

            GameWaitingLobby.applyTo(game, config.playerConfig);

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
        });
    }

    private StartResult requestStart() {
        CatchThePestActive.open(this.gameSpace, this.map, this.config);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player, null);
    }
}
