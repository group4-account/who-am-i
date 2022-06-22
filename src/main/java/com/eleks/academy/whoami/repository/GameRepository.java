package com.eleks.academy.whoami.repository;

import com.eleks.academy.whoami.core.SynchronousGame;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface GameRepository {

	Stream<SynchronousGame> findAllAvailable(String player);

	Map<String, SynchronousGame> findAvailableQuickGames();

	Map<String, SynchronousGame> findNotAvailableQuickGames();

	SynchronousGame save(SynchronousGame game);

	Optional<SynchronousGame> findById(String id);

    int getAllPlayersCount();
}
