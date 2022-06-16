package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.GameCharacter;
import com.eleks.academy.whoami.core.impl.StartGameAnswer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public final class SuggestingCharacters extends AbstractGameState {

    private final Lock lock = new ReentrantLock();

    private final Map<String, PlayerWithState> players;
    private final Map<String, List<GameCharacter>> suggestedCharacters;
    private final Map<String, GameCharacter> authorsCharacters;
    private final Map<String, String> playerCharacterMap;

    public SuggestingCharacters(Map<String, PlayerWithState> players) {
        super(players.size(), players.size());

        this.players = players;
        this.suggestedCharacters = new HashMap<>(this.players.size());
        this.playerCharacterMap = new HashMap<>(this.players.size());
        this.authorsCharacters = new HashMap<>();
    }

    /**
     * Randomly assigns characters to players and returns a next stage
     * or throws {@link GameException} in case {@link this#finished()} returns {@code false}
     *
     * @return next {@link ProcessingQuestion} stage
     */
    @Override
    public GameState next() {
        List<String> playersName = players.keySet().stream().toList();
        return Optional.of(this)
                .filter(SuggestingCharacters::finished)
                .map(SuggestingCharacters::assignCharacters)
                .map(then -> new ProcessingQuestion(playersName.get(0), this.players))
                .orElseThrow(() -> new GameException("Cannot start game"));
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return Optional.ofNullable(this.players.get(player).getPlayer());
    }

    @Override
    public List<PlayerWithState> getPlayersWithState() {
        return players.values().stream().toList();

    }


    // TODO: Consider extracting into {@link GameState}
    private Boolean finished() {
        final var enoughCharacters = Optional.of(this.suggestedCharacters)
                .map(Map::values)
                .stream()
                .mapToLong(Collection::size)
                .sum() >= this.players.size();

        return this.suggestedCharacters.size() > 1
                && enoughCharacters;
    }

    private GameState suggestCharacter(String player, String character) {
        List<GameCharacter> characters = this.suggestedCharacters.get(player);

        if (Objects.isNull(characters)) {
            final var newCharacters = new ArrayList<GameCharacter>();

            this.suggestedCharacters.put(player, newCharacters);
            this.authorsCharacters.put(player, GameCharacter.of(character, player));
            characters = newCharacters;
        }

        characters.add(GameCharacter.of(character, player));

        return this;
    }

    @Override
    public GameState makeTurn(Answer answer) {
        this.lock.lock();
        try {

            players.values().stream()
                    .filter(playerWithState -> Objects.equals(playerWithState.getPlayer().getName(),
                            players.get(answer.getPlayer()).getPlayer().getName()))
                    .findFirst()
                    .ifPresent(a -> a.setState(PlayerState.READY));
            return Optional.of(answer)
                    .filter(StartGameAnswer.class::isInstance)
                    .map(StartGameAnswer.class::cast)
                    .map(then -> this.next())
                    .orElseGet(() -> this.suggestCharacter(answer.getPlayer(), answer.getMessage()));
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * The term author is referred to a player who suggested at least one character
     * <p>
     * Basic algorithm description:
     * 1) Collect all randomly-ordered authors into a form of cyclic oriented graph.
     * 2) Assign to each author a random character suggested by the next author (next graph node)
     * 3) Randomly assign all the suggested characters that are left disregarding the author to
     * all the non-author players
     */
    private GameState assignCharacters() {
        Function<String, Integer> randomAuthorOrderComparator = value ->
                Double.valueOf(Math.random() * 1000).intValue();

        final var authors =
                this.suggestedCharacters.keySet()
                        .stream()
                        .sorted(Comparator.comparing(randomAuthorOrderComparator))
                        .collect(Collectors.toList());

        authors.forEach(author -> {
            final var character = this.getRandomCharacter()
                    .apply(this.suggestedCharacters.get(this.<String>cyclicNext().apply(authors, author)));

            character.markTaken();

            this.playerCharacterMap.put(author, character.getCharacter());
        });

        final var authorsSet = new HashSet<>(authors);
        this.players.keySet()
                .stream()
                .filter(authorsSet::contains)
                .forEach(player -> {
                    var character = this.getRandomCharacter().apply(this.authorsCharacters.values()
                            .stream().toList());
                    while(player.equals(character.getAuthor())) {
                        character = this.getRandomCharacter().apply(this.authorsCharacters.values()
                                .stream().toList());
                    }
                    character.markTaken();

                    this.playerCharacterMap.put(player, character.getCharacter());
                    this.players.get(player).getPlayer().setCharacter(character.getCharacter());
                    authorsCharacters.remove(character.getAuthor());
                });
        return this;
    }

    private Function<List<GameCharacter>, GameCharacter> getRandomCharacter() {
        return gameCharacters -> {
            int randomPos = (int) (Math.random() * gameCharacters.size());
            return gameCharacters.get(randomPos);
        };
    }

    private <T> BiFunction<List<T>, T, T> cyclicNext() {
        return (list, item) -> {
            final var index = list.indexOf(item);

            return Optional.of(index)
                    .filter(i -> i + 1 < list.size())
                    .map(i -> list.get(i + 1))
                    .orElseGet(() -> list.get(0));
        };
    }
}
