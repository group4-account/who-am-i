package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.Player;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import lombok.Data;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;
@Data
public class PersistentPlayer implements Player, SynchronousPlayer {

	private final String id;
	private String character;
	private String name;

	private Queue<String> questionQueue;
	private volatile String question;
	private volatile String currentAnswer;
	private volatile Boolean readyForAnswerFuture = true;

	public PersistentPlayer(String id) {
		this.id = Objects.requireNonNull(id);
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public Future<String> answerQuestion(String question, String character) {
		return null;
	}

	@Override
	public Future<String> getGuess() {
		return null;
	}

	@Override
	public Future<Boolean> isReadyForGuess() {
		return null;
	}

	@Override
	public Future<String> answerGuess(String guess, String character) {
		return null;
	}

	@Override
	public void close() {

	}


	public void setId(String player) {

	}
}
