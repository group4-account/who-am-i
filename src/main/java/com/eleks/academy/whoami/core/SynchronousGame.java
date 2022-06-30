package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.List;
import java.util.Optional;

public interface SynchronousGame {

	Optional<SynchronousPlayer> findPlayer(String player);

	String getId();

	SynchronousPlayer enrollToGame(String player);

	List<PlayerWithState> getPlayersInGame();

	String getStatus();

	boolean isAvailable();

	String getTurn();

	void makeTurn(Answer player);

	void askQuestion(String player, String message);

	void answerQuestion(String player, Answer answer);

	SynchronousGame start();

	Optional<GameState> getCurrentTurnInfo();

	void removeFromGame(String gameId, String player);
}
