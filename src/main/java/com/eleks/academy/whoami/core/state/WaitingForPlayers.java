package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;
import java.util.stream.Collectors;

public final class WaitingForPlayers extends AbstractGameState {

	private final Map<String, SynchronousPlayer> players;
	private int maxPlayers;
	public WaitingForPlayers(int maxPlayers) {
		super(0, maxPlayers);
		this.maxPlayers = maxPlayers;
		this.players = new HashMap<>(maxPlayers);
	}

	private WaitingForPlayers(int maxPlayers, Map<String, SynchronousPlayer> players) {
		super(players.size(), maxPlayers);
		this.players = players;
	}

	@Override
	public GameState next() {

		return new SuggestingCharacters (players);
	}
    public SynchronousPlayer addPlayer(SynchronousPlayer player){
		players.put(player.getName(), player);
		return player;
    }

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		return Optional.ofNullable(this.players.get(player));
	}

	@Override
	public int getPlayersInGame() {
		return this.players.size();
	}

	@Override
	public GameState makeTurn(Answer answer) {
		Map<String, SynchronousPlayer> nextPlayers = new HashMap<>(this.players);
		if (nextPlayers.containsKey(answer.getPlayer()) || maxPlayers == this.getPlayersInGame()) {
//			new GameException("Cannot enroll to the game");
		} else {
			nextPlayers.put(answer.getPlayer(), new PersistentPlayer(answer.getPlayer()));
		}
		if (nextPlayers.size() == getMaxPlayers()) {
			return new SuggestingCharacters(nextPlayers);
		} else {
			return new WaitingForPlayers(getMaxPlayers(), nextPlayers);
		}
	}

	@Override
	public List<PlayerWithState> getPlayersWithState() {
		List<PlayerWithState> playerWithStateList = new ArrayList<>();
		this.players.values().forEach(player -> playerWithStateList.add(PlayerWithState.builder()
						.state(PlayerState.NOT_READY)
						.player(player)
						.build()));
		return playerWithStateList;
	}

	@Override
	public List<PlayerWithState> getPlayers() {
		return null;
	}

}
