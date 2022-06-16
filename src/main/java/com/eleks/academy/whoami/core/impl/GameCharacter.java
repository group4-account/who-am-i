package com.eleks.academy.whoami.core.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class GameCharacter {

	private final String character;

	private final String author;

	private boolean taken;

	public void markTaken() {
		this.taken = Boolean.TRUE;
	}

}
