package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.eleks.academy.whoami.model.request.QuestionAnswer.NO;
import static com.eleks.academy.whoami.model.request.QuestionAnswer.NOT_SURE;
import static com.eleks.academy.whoami.model.response.PlayerState.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.partitioningBy;

public final class ProcessingQuestion extends AbstractGameState {

	private Map<String, PlayerWithState> players;
	private volatile long timer;
	private volatile long timerToLeave;
	private final int maxTimeForQuestion = 60;
	private final int maxTimeForAnswer = 20;

	public ProcessingQuestion(String currentPlayer1, Map<String, PlayerWithState> players) {
		super(players.size(), players.size());
		this.players = players;
		final String currentPlayer = currentPlayer1;
		this.players.get(currentPlayer).setState(ASKING);
		this.players.values().stream()
				.filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), currentPlayer))
				.forEach(player -> player.setState(READY));
		this.players.values()
				.stream()
				.filter(playerWithState -> playerWithState.getPlayer().getBeingInActiveCount() == 3)
				.forEach(this::leaveGame);
		runAsync(() -> this.makeTurn(new Answer(null)));
		runAsync(this::startTimer);
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
				.filter(player -> Objects.equals(player.getState(), ASKING) ||
						Objects.equals(player.getState(), ASKED))
				.findFirst()
				.map(playerWithState -> playerWithState.getPlayer().getId())
				.orElse("No one asking at this time");
	}

	@Override
	public List<PlayerWithState> getPlayersWithState() {
		return ofNullable(this.players)
				.map(Map::values)
				.map(Collection::stream)
				.map(Stream::toList)
				.orElse(new ArrayList<>());
	}

	@Override
	public Optional<PlayerWithState> findPlayerWithState(String player) {
		return ofNullable(this.players.get(player));
	}

	@Override
	public GameState makeTurn(Answer answerQuestion) {
		resetToDefault();
		PlayerWithState currentPlayer = players.get(getCurrentTurn());
		try {
			try {
				currentPlayer.getFirstQuestion().get(maxTimeForQuestion, SECONDS);
				currentPlayer.setState(ASKED);
			} catch (TimeoutException e) {
				Map<String, PlayerWithState> newPlayersMap = this.players;
				setTimerToLeave(currentPlayer, newPlayersMap);
				List<String> playersList = new ArrayList<>(this.players.keySet());
				return new ProcessingQuestion(playersList
						.get(findCurrentPlayerIndex(playersList, currentPlayer)), newPlayersMap);
			} finally {
				if (currentPlayer.getQuestion() != null)
					this.players.values().stream()
							.filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(),
									currentPlayer.getPlayer().getId()))
							.forEach(player -> player.setState(ANSWERING));
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		this.players.values()
				.parallelStream()
				.filter(playerWithState -> playerWithState.getState() == ANSWERING)
				.forEach(player1 -> {
					try {
						try {
							player1.answerQuestion().get(maxTimeForAnswer, SECONDS);
							player1.getPlayer().zeroTimePlayersBeingInactive();
							player1.setState(ANSWERED);
						} catch (TimeoutException e) {
							player1.getPlayer().incrementBeingInactiveCount();
						} finally {
							this.players.values()
									.stream().filter(playerWithState -> playerWithState.getState() == ANSWERING)
									.forEachOrdered(playerWithState -> playerWithState.setAnswer(NOT_SURE));
						}
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				});
		Map<Boolean, List<PlayerWithState>> booleanPlayersAnswerMap = this.players.values().stream()
				.filter(player -> player.getState() == ANSWERING || player.getState() == ANSWERED)
				.collect(partitioningBy(playerWithState -> playerWithState.getAnswer() == NO));

		if (booleanPlayersAnswerMap.get(FALSE).size() < booleanPlayersAnswerMap.get(TRUE).size()) {
			List<String> collect = new ArrayList<>(this.players.keySet());
			return new ProcessingQuestion(collect.get(findCurrentPlayerIndex(collect, currentPlayer)), players);
		} else {
			return new ProcessingQuestion(currentPlayer.getPlayer().getId(), players);
		}
	}

	@Override
	public GameState leaveGame(String player) {
		Map<String, PlayerWithState> newPlayersMap = this.players;
		PlayerWithState removingPlayer = players.get(player);
		setTimerToLeave(removingPlayer, newPlayersMap);
		List<String> playersList = new ArrayList<>(this.players.keySet());
		if (isAskingPlayer(player)) {
			PlayerWithState currentPlayer = players.get(getCurrentTurn());
			return new ProcessingQuestion(playersList
					.get(findCurrentPlayerIndex(playersList, currentPlayer)), newPlayersMap);
		} else if (this.players.size() == 1) {
			return new GameFinished(this.players);
		} else {
			return this;
		}
	}

	@Override
	public long getTimer() {
		return timer;
	}

	private void setTimerToLeave(PlayerWithState removingPlayer, Map<String, PlayerWithState> newPlayersMap) {
		int limit = 3;
		removingPlayer.setIsLeaving(true);
		removingPlayer.setState(LEAVING);
		runAsync(() -> {
			long start = System.currentTimeMillis();
			timerToLeave = 1;
			while (timerToLeave > 0) {
				long now = System.currentTimeMillis();
				timerToLeave = limit - TimeUnit.MILLISECONDS.toSeconds(now - start);
			}
			newPlayersMap.remove(removingPlayer.getPlayer().getId());
			this.players = newPlayersMap;
		});
	}

	private void leaveGame(PlayerWithState player) {
		Map<String, PlayerWithState> newPlayersMap = this.players;
		setTimerToLeave(player, newPlayersMap);
	}

	private boolean isAskingPlayer(String answer) {
		return Objects.equals(answer,
				this.players.values()
						.stream()
						.filter(player -> player.getState() == ASKING || player.getState() == ASKED)
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
		this.players.values().forEach(PlayerWithState::inCompleteFuture);
	}

	private void startTimer() {
		while (true) {
			long start = currentTimeMillis();
			boolean isQuestion = true;
			while (this.players.values().stream().anyMatch(player -> player.getState() == READY)) {
				long now = currentTimeMillis();
				timer = maxTimeForQuestion - MILLISECONDS.toSeconds(now - start);
				if (timer <= 0) {
					isQuestion = false;
					break;
				}
			}
			start = currentTimeMillis();
			while (this.players.values().stream().allMatch(player -> player.getState() != ASKING) && isQuestion) {
				long now = currentTimeMillis();
				timer = maxTimeForAnswer - MILLISECONDS.toSeconds(now - start);
				if (timer < 0) {
					break;
				}
			}
		}


	}
}
