package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class WaitingForPlayers extends AbstractGameState {

	private final Map<String, PlayerWithState> players;

	public WaitingForPlayers(int maxPlayers) {
		super(0, maxPlayers);
		this.players = new HashMap<>(maxPlayers);
	}

	private WaitingForPlayers(int maxPlayers, Map<String, PlayerWithState> players) {
		super(players.size(), maxPlayers);
		this.players = players;
	}

	@Override
	public GameState next() {
		var synchronousPlayers = this.players
			.entrySet()
        	.stream()
        	.collect(Collectors.toMap(
        		Map.Entry::getKey, 
        		e -> e.getValue().getPlayer()));
 
		return new SuggestingCharacters (synchronousPlayers);
	}
    public SynchronousPlayer AddPlayer(String playerName){
        var player = new PlayerWithState(new PersistentPlayer(playerName),null,PlayerState.READY);
        this.players.put(playerName, player);
        return player.getPlayer();
    }

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		return Optional.ofNullable(this.players.get(player))
			.map(p -> p.getPlayer());
	}

	@Override
	public int getPlayersInGame() {
		return this.players.size();
	}

	@Override
	public List<PlayerWithState> getPlayers() {
		return players.values().stream().collect(Collectors.toList());
	}

}
