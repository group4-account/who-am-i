package com.eleks.academy.whoami.database.repository;

import com.eleks.academy.whoami.database.entity.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PlayerRepository extends CrudRepository<Player, Integer> {

	Optional<Player> findByEmail(String email);
}
