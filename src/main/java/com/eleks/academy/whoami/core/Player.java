package com.eleks.academy.whoami.core;

import java.util.concurrent.Future;

public interface Player {

	String getId();

	Future<String> getGuess();

	Future<Boolean> isReadyForGuess();

	Future<String> answerGuess(String guess, String character);

	Future<Boolean> isLeaving();

	void close();

}
