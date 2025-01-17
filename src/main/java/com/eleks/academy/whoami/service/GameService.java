package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.TurnDetails;
import com.eleks.academy.whoami.repository.impl.QNAHistoryRepositoryImpl;

import java.util.List;
import java.util.Optional;

public interface GameService {

	List<GameLight> findAvailableGames(String player);

	GameDetails createGame(String player, NewGameRequest gameRequest);

	List<QNAHistoryRepositoryImpl.Question> getQnaHistory(String gameId);

	Optional<SynchronousPlayer> enrollToGame(String id, String player);

	Optional<GameDetails> findByIdAndPlayer(String id, String player);

	void suggestCharacter(String id, String player, CharacterSuggestion suggestion);

	Optional<GameDetails> startGame(String id, String player);

	void askQuestion(String gameId, String player, String message);

	Optional<TurnDetails> findTurnInfo(String id, String player);

	void submitGuess(String id, String player, String message);

	void answerQuestion(String id, String player, String answer);

	void leaveGame(String gameId, String player);

	Optional<GameDetails> findAvailableQuickGame(String player);

	int getAllPlayersCount();

	int getReadyPlayersCount(String id, String player);
}
