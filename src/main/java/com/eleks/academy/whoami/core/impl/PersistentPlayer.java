package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.Player;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import lombok.Data;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;

@Data
public class PersistentPlayer implements Player, SynchronousPlayer {

	private final String id;
	private String character;
	private String name;

	private Queue<String> questionQueue;
	private volatile CompletableFuture<String> question = new CompletableFuture<>();
	private volatile CompletableFuture<String> currentAnswer = new CompletableFuture<>();
	private volatile CompletableFuture<Boolean> readyForAnswerFuture;

	public PersistentPlayer(String id) {
		this.id = Objects.requireNonNull(id);
	}

	@Override
	public Future<String> getFirstQuestion() {
		return question;
	}

	@Override
	public Future<String> setQuestion(String question) {
		this.question.complete(question);
		return this.question;
	}
	@Override
	public Future<String> inCompleteFuture() {
		question = this.question.newIncompleteFuture();
		currentAnswer = this.currentAnswer.newIncompleteFuture();
		return this.currentAnswer;
	}
	@Override
	public Future<String> answerQuestion() {
		return currentAnswer;
	}
	@Override
	public Future<String> setAnswerQuestion(String answer) {
		this.currentAnswer.complete(answer);
		return this.currentAnswer;
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


}
