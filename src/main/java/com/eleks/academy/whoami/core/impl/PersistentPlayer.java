package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.Player;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import lombok.Data;

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;

@Data
public class PersistentPlayer implements Player, SynchronousPlayer {

	private final String id;
	private String character;
	private String name;

	private Queue<String> questionQueue;
	private volatile CompletableFuture<String> question = new CompletableFuture<>();
	private volatile CompletableFuture<String> currentAnswer;
	private volatile CompletableFuture<Boolean> readyForAnswerFuture;
//	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public PersistentPlayer(String id) {
		this.id = Objects.requireNonNull(id);
	}

	@Override
	public Future<String> getFirstQuestion() {
		return question;
	}

	@Override
	public void setQuestion(String question) {
		this.question = CompletableFuture.completedFuture(question)
				.thenApply(playerQuestion -> playerQuestion + "?");
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
