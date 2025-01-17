package com.eleks.academy.whoami.repository.impl;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.repository.GameRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Repository
public class GameInMemoryRepository implements GameRepository {

	private final Map<String, SynchronousGame> games = new ConcurrentHashMap<>();

	@Override
	public Stream<SynchronousGame> findAllAvailable(String player) {
		Predicate<SynchronousGame> freeToJoin = SynchronousGame::isAvailable;

		Predicate<SynchronousGame> playersGame = game ->
				ofNullable(game.findPlayer(player))
						.map(Optional::isPresent)
						.orElse(false);

		return this.games.values()
				.stream()
				.filter(freeToJoin.or(playersGame));
	}

	@Override
	public Map<String, SynchronousGame> findAvailableQuickGames() {
		return filterByValue(games, SynchronousGame::isAvailable);
	}

	private static <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate) {
		return map.entrySet()
				.stream()
				.filter(entry -> predicate.test(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public SynchronousGame save(SynchronousGame game) {
		this.games.put(game.getId(), game);

		return game;
	}

	@Override
	public Optional<SynchronousGame> findById(String id) {
		return ofNullable(this.games.get(id));
	}

	@Override
	public int getAllPlayersCount() {
		return games.values()
				.stream()
				.map(game -> ofNullable(game.getPlayersInGame())
						.map(List::size).orElse(0))
				.mapToInt(Integer::intValue).sum();
	}

	@Override
	public void remove(SynchronousGame game) {
		this.games.remove(game.getId(), game);
	}
}
