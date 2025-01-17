package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.List;
import java.util.Optional;

public sealed interface GameState permits AbstractGameState {

	GameState next();

	Optional<PersistentPlayer> findPlayer(String player);

	/**
	 * Used for presentation purposes only
	 *
	 * @return a player, whose turn is now
	 * or {@code null} if state does not take turns (e.g. {@link SuggestingCharacters})
	 */
	String getCurrentTurn();

	/**
	 * Used for presentation purposes only
	 *
	 * @return the status of the current state to show to players
	 */
	String getStatus();

	/**
	 * Used for presentation purposes only
	 *
	 * @return the count of the players
	 */
	int getPlayersInGame();

	List<PlayerWithState> getPlayersWithState();
	/**
	 * Used for presentation purposes only
	 *
	 * @return the maximum allowed count of the players
	 */
	int getMaxPlayers();

    default SynchronousPlayer enrollToGame(String player) {
    	throw new GameException("Cannot enroll to game");
	}

    GameState makeTurn(Answer player);

	GameState leaveGame(String player);

	long getTimer();

	Optional<PlayerWithState> findPlayerWithState(String player);
}
