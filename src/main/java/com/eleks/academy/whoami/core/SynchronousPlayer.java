package com.eleks.academy.whoami.core;

public interface SynchronousPlayer {

	String getId();

	String getCharacter();

	void setCharacter(String character);

	void setId(String player);
	void setQuestion(String question);
}
