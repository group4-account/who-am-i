package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.AnswerQuestion;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.eleks.academy.whoami.model.request.QuestionAnswer.*;
import static com.eleks.academy.whoami.model.response.PlayerState.ANSWERING;
import static com.eleks.academy.whoami.model.response.PlayerState.ASKING;
import static java.util.stream.Collectors.toList;

// TODO: Implement makeTurn(...) and next() methods, pass a turn to next player
public final class ProcessingQuestion extends AbstractGameState {

    private final Map<String, PlayerWithState> players;

	public ProcessingQuestion(String currentPlayer1, Map<String, PlayerWithState> players) {
        super(players.size(), players.size());
		final String currentPlayer = currentPlayer1;
		this.players = players;


        players.get(currentPlayer).setState(ASKING);
        players.values().stream()
                .filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), currentPlayer))
                .forEach(player -> player.setState(ANSWERING));
        players.values().stream()
                .filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), currentPlayer))
                .forEach(player -> player.getPlayer().setReadyForAnswerFuture(CompletableFuture.completedFuture(false)));
        this.makeTurn(new Answer(null));
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
		return this.players.values().stream().filter(player -> player.getState().equals(ASKING))
				.findFirst()
				.map(a -> a.getPlayer().getId())
				.orElseThrow();
    }

    @Override
    public List<PlayerWithState> getPlayersWithState() {
        return players.values().stream().toList();
    }


	@Override
	public GameState makeTurn(Answer answer) {
		PlayerWithState currentPlayer = players.get(getCurrentTurn());
		try {
			try {
				players.get(currentPlayer.getPlayer().getId())
						.getPlayer().getFirstQuestion().get(20, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				int currentPlayerIndex = 0;
				List<String> collect = new ArrayList<>(this.players.keySet());
				currentPlayerIndex = collect.indexOf(currentPlayer.getPlayer().getName());
				currentPlayerIndex = currentPlayerIndex + 1 >= this.players.size() ? 0 : currentPlayerIndex + 1;
				return new ProcessingQuestion(collect.get(currentPlayerIndex), players);
			}
		} catch (InterruptedException | ExecutionException  e) {
			e.printStackTrace();
		}

		List<String> answers = this.players.values().stream().parallel().map(player1 -> {
			try {
				try {
					return player1.getPlayer().answerQuestion().get(20, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					 player1.setAnswer(NOT_SURE);
					return String.valueOf(NOT_SURE);
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}).collect(toList());
		long yesAnswers = answers.stream().filter(YES.name()::equals).count();
		long noAnswers = answers.stream().filter(NO.name()::equals).count();
		if (yesAnswers < noAnswers) {
			int currentPlayerIndex;
			List<String> collect = new ArrayList<>(this.players.keySet());
			currentPlayerIndex = collect.indexOf(currentPlayer.getPlayer().getName());
			currentPlayerIndex = currentPlayerIndex + 1 >= this.players.size() ? 0 : currentPlayerIndex + 1;
			return new ProcessingQuestion(collect.get(currentPlayerIndex), players);
		} else {
			return new ProcessingQuestion(currentPlayer.getPlayer().getName(), players);
		}
	}


    @Override
    public GameState makeLeave(Answer answer) {
		PlayerWithState currentPlayer = players.get(getCurrentTurn());
		int currentPlayerIndex;
		List<String> collect = new ArrayList<>(this.players.keySet());
		currentPlayerIndex = collect.indexOf(currentPlayer.getPlayer().getName());
		currentPlayerIndex = currentPlayerIndex + 1 >= this.players.size() ? 0 : currentPlayerIndex + 1;
	if (Objects.equals(answer.getPlayer(), this.players.values().stream().filter(player -> player.getState().equals(ASKING))
			.findFirst().map(a -> a.getPlayer().getId()).orElse("NOT_ASKING_PLAYER"))) {
		return new ProcessingQuestion(collect.get(currentPlayerIndex), players);
	} else {
		//TODO: add remove player from list
		return this;
	}
}

}
