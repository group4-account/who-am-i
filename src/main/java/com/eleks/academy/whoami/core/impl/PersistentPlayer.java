package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.Player;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import lombok.Data;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
@Data
public class PersistentPlayer implements Player, SynchronousPlayer {

	private final String id;
	private String character;
	private String name;

	private Queue<String> questionQueue;
	private volatile Future<String> question;
	private volatile CompletableFuture<String> currentAnswer;
	private volatile CompletableFuture<Boolean> readyForAnswerFuture;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public PersistentPlayer(String id) {
		this.id = Objects.requireNonNull(id);
	}

	@Override
	public Future<String> getQuestion() {
		return question;
	}
	private void askQuestion(String question) {

	}
	private void setQuestion(String question) {

	}
	public Future<String> answerQuestion(String question, String character) {
		return null;
	}

	@Override
	public Future<String> answerQuestion() {
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
