package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.AnswerQuestion;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;

import static com.eleks.academy.whoami.model.response.PlayerState.*;
import static java.util.stream.Collectors.*;

// TODO: Implement makeTurn(...) and next() methods, pass a turn to next player
public final class ProcessingQuestion extends AbstractGameState {

    private final String currentPlayer;
    private final Map<String, PlayerWithState> players;

    public ProcessingQuestion(String currentPlayer, Map<String, PlayerWithState> players) {
        super(players.size(), players.size());

        this.players = players;

        this.currentPlayer = currentPlayer;

        players.get(this.currentPlayer).setState(ASKING);
        players.values().stream()
                .filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), this.currentPlayer))
                .forEach(player -> player.setState(ANSWERING));
        players.values().stream()
                .filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), this.currentPlayer))
                .forEach(player -> player.getPlayer().setReadyForAnswerFuture(false));
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
        return this.currentPlayer;
    }

    @Override
    public List<PlayerWithState> getPlayersWithState() {
        return players.values().stream().toList();
    }


    @Override
    public GameState makeTurn(Answer answer) {
        if (!this.players.get(this.currentPlayer).getPlayer().getReadyForAnswerFuture() ||
        this.players.get(this.currentPlayer).getPlayer().getQuestion() == null) {
            if (this.currentPlayer.equals(answer.getPlayer())) {
                return askQuestion(answer);
            } else {
                throw new GameException("Not your turn!");
            }
        }
        if (this.players.values().stream().anyMatch(players -> players.getAnswer() == null)) {
           return makeAnswer(answer);
        }
        List<String> answers = this.players.values().stream()
                .map(player1 -> player1.getPlayer().getCurrentAnswer()).collect(toList());
        long yesAnswers = answers.stream().filter("YES"::equals).count();
        long noAnswers = answers.stream().filter("NO"::equals).count();
        if (yesAnswers < noAnswers) {
            int currentPlayerIndex;
            List<String> collect = new ArrayList<>(this.players.keySet());
            currentPlayerIndex = collect.indexOf(currentPlayer);
            currentPlayerIndex = currentPlayerIndex + 1 >= this.players.size() ? 0 : currentPlayerIndex + 1;
            return new ProcessingQuestion(collect.get(currentPlayerIndex), players);
        } else {
            return new ProcessingQuestion(currentPlayer, players);
        }
    }

    private GameState makeAnswer(Answer answer) {
        AnswerQuestion answerQuestion = (AnswerQuestion) answer;
        if (Objects.equals(this.currentPlayer, answer.getPlayer()))
            throw new GameException("You don`t answer now !");
        this.players.get(answer.getPlayer()).setAnswer(QuestionAnswer.valueOf(answerQuestion.getMessage()));
        return new ProcessingQuestion(currentPlayer, players);
    }

    private GameState askQuestion(Answer answer) {
        this.players.get(answer.getPlayer()).getPlayer().setQuestion(answer.getMessage());
        this.players.get(answer.getPlayer()).getPlayer().setReadyForAnswerFuture(true);
        return new ProcessingQuestion(this.currentPlayer, players);

    }

    @Override
    public GameState makeLeave(Answer answer) {
        return null;
    }
}
