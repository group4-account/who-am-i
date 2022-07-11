package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GameFinished extends AbstractGameState {

	public GameFinished(Map<String, PlayerWithState> players) {
		super(players.size(), players.size());
	}

	@Override
	public GameState next() {
		return null;
	}

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		return Optional.empty();
	}


	@Override
	public List<PlayerWithState> getPlayersWithState() {
		return new ArrayList<>();
	}

	@Override
	public SynchronousPlayer enrollToGame(String player) {
		return super.enrollToGame(player);
	}

	@Override
	public GameState makeTurn(Answer player) {
		return null;
	}

	@Override
	public GameState leaveGame(String player) {
		return null;
	}
}
