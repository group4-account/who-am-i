package com.eleks.academy.whoami.core;

import java.util.concurrent.Future;

public interface SynchronousPlayer {

	String getId();

	String getCharacter();

	void setCharacter(String character);

	Future<String> inCompleteFuture();

	Future<String> setAnswerQuestion(String answer);

	Future<String> setQuestion(String question);
}
