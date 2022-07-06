package com.eleks.academy.whoami.core.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistentPlayerTest {

	@Test
	void allowToSuggestCharacterOnlyOnce() {
		PersistentPlayer player = new PersistentPlayer("PLayerName");
		player.setCharacter("character");
		assertThrows(IllegalStateException.class, () -> player.setCharacter("character"));
	}
}
