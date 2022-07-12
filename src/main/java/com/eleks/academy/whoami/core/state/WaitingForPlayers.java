package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.eleks.academy.whoami.model.response.PlayerState.NOT_READY;

public final class WaitingForPlayers extends AbstractGameState {

    private final Map<String, PlayerWithState> players;
    private int maxPlayers;

    public WaitingForPlayers(int maxPlayers) {
        super(0, maxPlayers);
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>(maxPlayers);
    }


    private WaitingForPlayers(int maxPlayers, Map<String, PlayerWithState> players) {
        super(players.size(), maxPlayers);
        this.players = players;
    }

    @Override
    public GameState next() {
        return new SuggestingCharacters(this.players);
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return Optional.ofNullable(this.players.get(player))
                .map(PlayerWithState::getPlayer);
    }

    @Override
    public int getPlayersInGame() {
        return this.players.size();
    }

    @Override
    public SynchronousPlayer enrollToGame(String player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameState makeTurn(Answer answer) {
        Map<String, PlayerWithState> nextPlayers = new HashMap<>(this.players);
        if (nextPlayers.containsKey(answer.getPlayer()) || maxPlayers == this.getPlayersInGame()) {
            throw new GameException("Cannot enroll to the game");
        } else {
            PersistentPlayer persistentPlayer = new PersistentPlayer(answer.getPlayer());
            nextPlayers.put(answer.getPlayer(),
                    new PlayerWithState(persistentPlayer, null, null, NOT_READY));
        }
        if (nextPlayers.size() == getMaxPlayers()) {
            return new SuggestingCharacters(nextPlayers);
        } else {
            return new WaitingForPlayers(getMaxPlayers(), nextPlayers);
        }
    }

    @Override
    public GameState leaveGame(String player) {
        Map<String, PlayerWithState> players = new HashMap<>(this.players);
        players.remove(player);
        return new WaitingForPlayers(getMaxPlayers(), players);
    }

    @Override
    public List<PlayerWithState> getPlayersWithState() {
        return new ArrayList<>(players.values());
    }

}
