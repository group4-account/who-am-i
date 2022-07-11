package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import lombok.SneakyThrows;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.eleks.academy.whoami.model.request.QuestionAnswer.NO;
import static com.eleks.academy.whoami.model.request.QuestionAnswer.NOT_SURE;
import static com.eleks.academy.whoami.model.response.PlayerState.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.partitioningBy;

public final class ProcessingQuestion extends AbstractGameState {

	private Map<String, PlayerWithState> players;

	public ProcessingQuestion(String currentPlayer1, Map<String, PlayerWithState> players) {
		super(players.size(), players.size());
		this.players = players;

		new ArrayList<>(this.players.values())
				.stream()
				.filter(playerWithState -> playerWithState.getPlayer().getBeingInActiveCount() == 3)
				.forEach(player -> this.leaveGame(player, currentPlayer1));

		final String currentPlayer = currentPlayer1;

		this.players.get(currentPlayer).setState(ASKING);
		this.players.values().stream()
				.filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), currentPlayer))
				.forEach(player -> player.setState(READY));

		supplyAsync(() -> this.makeTurn(new Answer(null)));
	}

	@Override
	public GameState next() {
		throw new GameException("Not implemented");
	}

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		return ofNullable(this.players.get(player))
				.map(PlayerWithState::getPlayer);
	}

	@Override
	public String getCurrentTurn() {
		return this.players.values().stream()
				.filter(player -> Objects.equals(player.getState(), ASKING))
				.findFirst()
				.map(playerWithState -> playerWithState.getPlayer().getId())
				.orElse("No one asking at this time");
	}

	@Override
	public List<PlayerWithState> getPlayersWithState() {
		return this.players.values().stream().toList();
	}

	@SneakyThrows
	@Override
	public GameState makeTurn(Answer answerQuestion) {
		PlayerWithState currentPlayer = players.get(getCurrentTurn());
		resetToDefault();
		try {
			try {
				currentPlayer.setQuestion(currentPlayer.getPlayer().getFirstQuestion().get(60, SECONDS));
			} catch (TimeoutException e) {
				Map<String, PlayerWithState> newPlayersMap = this.players;
				newPlayersMap.remove(currentPlayer.getPlayer().getId());
				List<String> playersList = new ArrayList<>(newPlayersMap.keySet());
				return new ProcessingQuestion(playersList.get(findCurrentPlayerIndex(playersList, currentPlayer)), newPlayersMap);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		this.players.values().stream()
				.filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(),
						currentPlayer.getPlayer().getId()))
				.forEach(player -> player.setState(ANSWERING));
		this.players.values()
				.parallelStream()
				.filter(playerWithState -> Objects.equals(playerWithState.getState(), ANSWERING))
				.forEach(player1 -> {
					try {
						try {
							player1.setAnswer(QuestionAnswer.valueOf(
									player1.getPlayer().answerQuestion().get(20, SECONDS)));
							player1.getPlayer().zeroTimePlayersBeingInactive();
						} catch (TimeoutException e) {
							player1.getPlayer().incrementBeingInactiveCount();
						} finally {
							this.players.values()
									.stream().filter(playerWithState -> playerWithState.getAnswer() == null
											&& playerWithState.getState().equals(ANSWERING))
									.forEachOrdered(playerWithState -> playerWithState.setAnswer(NOT_SURE));
						}
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				});
		Map<Boolean, List<PlayerWithState>> booleanPlayersAnswerMap = this.players.values().stream()
				.filter(player -> Objects.equals(player.getState(), ANSWERING))
				.collect(partitioningBy(playerWithState -> Objects.equals(playerWithState.getAnswer(), NO)));
		if (booleanPlayersAnswerMap.get(FALSE).size() < booleanPlayersAnswerMap.get(TRUE).size()) {
			List<String> collect = new ArrayList<>(this.players.keySet());
			return new ProcessingQuestion(collect.get(findCurrentPlayerIndex(collect, currentPlayer)), players);
		} else {
			return new ProcessingQuestion(currentPlayer.getPlayer().getId(), players);
		}
	}


	@Override
	public GameState leaveGame(String player) {
		List<String> playersList = new ArrayList<>(this.players.keySet());
		if (isAskingPlayer(player)) {
			return new ProcessingQuestion(playersList.get(findCurrentPlayerIndex(playersList,
					this.players.get(getCurrentTurn()))), players);
		} else {
			//TODO: add remove player from list
			return this;
		}
	}

	private void leaveGame(PlayerWithState playerWithState, String currentPlayer) {
		Map<String, PlayerWithState> newPlayersMap = this.players;
		if (isAskingPlayer(playerWithState.getPlayer().getId())) {
			newPlayersMap.remove(currentPlayer);
		} else {
			newPlayersMap.remove(playerWithState.getPlayer().getId());
		}
		this.players = newPlayersMap;
	}

	private boolean isAskingPlayer(String answer) {
		return Objects.equals(answer,
				this.players.values()
						.stream()
						.filter(player -> Objects.equals(player.getState(), ASKING))
						.findFirst()
						.map(playerWithState -> playerWithState.getPlayer().getId())
						.orElse("NOT_ASKING_PLAYER"));
	}

	private int findCurrentPlayerIndex(List<String> playersList, PlayerWithState currentPlayer) {
		return Stream.of(playersList.indexOf(currentPlayer.getPlayer().getId()))
				.map(playerIndex -> playerIndex + 1 >= this.players.size() ? 0 : playerIndex + 1)
				.findFirst()
				.orElseThrow();
	}

	private void resetToDefault() {
		this.players.values().forEach(playerWithState -> {
			playerWithState.setQuestion(null);
			playerWithState.getPlayer().setQuestion(null);
			ofNullable(playerWithState.getPlayer())
					.map(PersistentPlayer::inCompleteFuture);
			playerWithState.setAnswer(null);
		});
	}

}
