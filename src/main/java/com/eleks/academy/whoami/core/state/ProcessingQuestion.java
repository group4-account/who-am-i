package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

// TODO: Implement makeTurn(...) and next() methods, pass a turn to next player
public final class ProcessingQuestion extends AbstractGameState {

    private final String currentPlayer;
    private final Map<String, PlayerWithState> players;
    private int currentPlayerIndex;

    public ProcessingQuestion(String currentPlayer, Map<String, PlayerWithState> players) {
        super(players.size(), players.size());

        this.players = players;

        this.currentPlayer = currentPlayer;
    }

    @Override
    public GameState next() {
        throw new GameException("Not implemented");
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return Optional.ofNullable(this.players.get(player).getPlayer());
    }

    @Override
    public String getCurrentTurn() {
        return null;
    }

    @Override
    public List<PlayerWithState> getPlayersWithState() {
        return players.values().stream().toList();
    }


    @Override
    public GameState makeTurn(Answer answer) {
        PersistentPlayer currentPlayer = players.get(this.currentPlayer).getPlayer();
        String question = null;
        try {
            question = players.get(this.currentPlayer).getPlayer().getQuestion().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        String finalQuestion = question;
        List<String> answers = this.players.values().stream().map(player1 -> {
            try {
                return player1.getPlayer().answerQuestion(finalQuestion, currentPlayer.getCharacter()).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).collect(toList());
        long yesAnswers = answers.stream().filter("YES"::equals).count();
        long noAnswers = answers.stream().filter("NO"::equals).count();
        if (yesAnswers < noAnswers) {
            currentPlayerIndex = 0;
            List<String> collect = new ArrayList<>(this.players.keySet());
            currentPlayerIndex = collect.indexOf(currentPlayer.getName());
            this.currentPlayerIndex = this.currentPlayerIndex + 1 >= this.players.size() ? 0 : this.currentPlayerIndex + 1;
            return new ProcessingQuestion(collect.get(currentPlayerIndex), players);
        } else {
            return new ProcessingQuestion(currentPlayer.getName(), players);
        }
    }
}
