package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// TODO: Implement makeTurn(...) and next() methods, pass a turn to next player
public final class ProcessingQuestion extends AbstractGameState {

    private final String currentPlayer;
    private final Map<String, PlayerWithState> players;

    public ProcessingQuestion(String currentPlayer, Map<String, PlayerWithState> players) {
        super(players.size(), players.size());

        this.players = players;

        this.currentPlayer = currentPlayer;
    }

    @Override
    public GameState next() {
        throw new GameException("Not implemented");
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return Optional.ofNullable(this.players.get(player))
                .map(PlayerWithState::getPlayer);
    }

    @Override
    public String getCurrentTurn() {
        return this.currentPlayer;
    }

//    @Override
    public SynchronousPlayer add(SynchronousPlayer player) {
        return player;
    }

    @Override
    public List<PlayerWithState> getPlayersWithState() {
        return players.values().stream().toList();
    }

    @Override
    public List<PlayerWithState> getPlayers() {
        return null;
    }

}