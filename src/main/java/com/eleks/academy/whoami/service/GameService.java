package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.TurnDetails;

import java.util.List;
import java.util.Optional;

public interface GameService {

	List<GameLight> findAvailableGames(String player);

	GameDetails createGame(String player, NewGameRequest gameRequest);

	Optional<SynchronousPlayer> enrollToGame(String id, String player);

	Optional<GameDetails> findByIdAndPlayer(String id, String player);

	void suggestCharacter(String id, String player, CharacterSuggestion suggestion);

	Optional<GameDetails> startGame(String id, String player);

	void askQuestion(String gameId, String player, CharacterSuggestion message);

	Optional<TurnDetails> findTurnInfo(String id, String player);

	void submitGuess(String id, String player, QuestionAnswer guess);

	void answerQuestion(String id, String player, QuestionAnswer answer);

	void leaveGame(String id, String player);

	Optional<GameDetails> findAvailableQuickGame(String player);
}
