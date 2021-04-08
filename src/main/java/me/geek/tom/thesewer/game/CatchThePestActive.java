package me.geek.tom.thesewer.game;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.geek.tom.thesewer.game.map.CatchThePestMap;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CatchThePestActive {
    private final CatchThePestConfig config;

    public final GameSpace gameSpace;
    private final CatchThePestMap gameMap;

    // TODO replace with ServerPlayerEntity if players are removed upon leaving
    private final Object2ObjectMap<ServerPlayerEntity, CatchThePestPlayer> participants;
    private final CatchThePestSpawnLogic spawnLogic;
    private final CatchThePestStageManager stageManager;
    private final boolean ignoreWinState;
    private final CatchThePestTimerBar timerBar;

    private CatchThePestActive(GameSpace gameSpace, CatchThePestMap map, GlobalWidgets widgets, CatchThePestConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.spawnLogic = new CatchThePestSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>();

        for (PlayerRef player : participants) {
            this.participants.put(player.getEntity(gameSpace.getWorld()), new CatchThePestPlayer());
        }

        PlayerRef pest = Util.getRandom(participants.toArray(new PlayerRef[0]), new Random());
        ServerPlayerEntity pestPlayer = pest.getEntity(gameSpace.getWorld());
        assert pestPlayer != null;
        this.participants.get(pestPlayer).isPest = true;
        for (ServerPlayerEntity player : gameSpace.getPlayers()) {
            if (player == pestPlayer) {
                player.sendMessage(new TranslatableText("game.catch-the-pest.you-are-the-pest"), false);
            } else {
                player.sendMessage(new TranslatableText("game.catch-the-pest.you-are-a-hunter", pestPlayer.getDisplayName()), false);
            }
        }

        this.stageManager = new CatchThePestStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.timerBar = new CatchThePestTimerBar(widgets);
    }

    public static void open(GameSpace gameSpace, CatchThePestMap map, CatchThePestConfig config) {
        gameSpace.openGame(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());
            GlobalWidgets widgets = new GlobalWidgets(game);
            CatchThePestActive active = new CatchThePestActive(gameSpace, map, widgets, config, participants);

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.INTERACTION, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.DENY);
            game.setRule(GameRule.MODIFY_INVENTORY, RuleResult.DENY);
            game.setRule(GameRule.MODIFY_ARMOR, RuleResult.DENY);

            game.on(GameOpenListener.EVENT, active::onOpen);
            game.on(GameCloseListener.EVENT, active::onClose);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        });
    }

    private void onOpen() {
        ServerWorld world = this.gameSpace.getWorld();
        for (ServerPlayerEntity player : this.participants.keySet()) {
            this.spawnParticipant(player);
        }
        this.stageManager.onOpen(world.getTime(), this.config);
    }

    private void onClose() {

    }

    private void addPlayer(ServerPlayerEntity player) {
        if (!this.participants.containsKey(player)) {
            this.spawnSpectator(player);
        }
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(player);
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        if (source.getSource() instanceof ServerPlayerEntity) {
            if (this.participants.get(player).isPest) {
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnParticipant(player);
        if (source.getSource() instanceof ServerPlayerEntity) {
            if (this.participants.get(player).isPest) {
                this.stageManager.finished(player.getServerWorld().getTime(), ((ServerPlayerEntity) source.getSource()));
            }
        }
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        CatchThePestPlayer participant = this.participants.get(player);
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player, participant);
        if (!participant.isPest) {
            player.inventory.setStack(0, ItemStackBuilder.of(new ItemStack(Items.NETHERITE_SWORD))
                    .addEnchantment(Enchantments.SHARPNESS, 1)
                    .build());
        } else {
            player.inventory.setStack(38, ItemStackBuilder.of(new ItemStack(Items.LEATHER_CHESTPLATE))
                    .setColor(0xFF0000)
                    .build());
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    32767, // according to vanilla code, this is permanent.
                    2,
                    true,
                    false
            ));
        }
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player, null);
    }

    private void tick() {
        ServerWorld world = this.gameSpace.getWorld();
        long time = world.getTime();

        CatchThePestStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
                return;
        }

        this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs * 20L);
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = winningPlayer.getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
        } else {
            message = new LiteralText("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerPlayerEntity winner = this.stageManager.getWinner();
        if (winner != null) {
            return WinResult.win(winner);
        }

        Optional<ServerPlayerEntity> winningPlayer = this.participants
                .object2ObjectEntrySet().stream().filter(e -> e.getValue().isPest).map(Map.Entry::getKey).findFirst();
        return winningPlayer
                .map(WinResult::win)
                .orElseGet(WinResult::no);
    }

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
