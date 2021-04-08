package me.geek.tom.thesewer.game;

import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameSpace;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import me.geek.tom.thesewer.CatchThePest;
import me.geek.tom.thesewer.game.map.CatchThePestMap;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class CatchThePestSpawnLogic {
    private final GameSpace gameSpace;
    private final CatchThePestMap map;

    public CatchThePestSpawnLogic(GameSpace gameSpace, CatchThePestMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.setGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
    }

    public void spawnPlayer(ServerPlayerEntity player, CatchThePestPlayer participant) {
        ServerWorld world = this.gameSpace.getWorld();

        BlockBounds bounds = (participant == null || participant.isPest) ? this.map.pestSpawn : this.map.hunterSpawn;
        if (bounds == null) {
            CatchThePest.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }
        BlockPos pos = bounds.getMin();

        BlockPos size = bounds.getSize();
        float x = pos.getX() + MathHelper.nextFloat(player.getRandom(), 0, size.getX() - .5f); // subtract .5 to prevent clipping in a wall
        float z = pos.getZ() + MathHelper.nextFloat(player.getRandom(), 0, size.getZ() - .5f);

        player.teleport(world, x, pos.getY(), z, 0.0F, 0.0F);
    }
}
