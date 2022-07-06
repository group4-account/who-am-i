package com.eleks.academy.whoami.core;

import com.eleks.academy.whoami.core.impl.Answer;

// TODO: Change default methods to abstract, drop the old version ones
public interface Game {

	void makeTurn(Answer player);

	boolean isFinished();

	void changeTurn();

	void initGame();

	void play();

}
