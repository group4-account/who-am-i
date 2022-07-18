package com.eleks.academy.whoami.model.response;

import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerWithState {

	private PersistentPlayer player;

	private QuestionAnswer answer;

	private String question;

	private PlayerState state;

	private Boolean isLeaving;

	private CompletableFuture<String> questionFuture = new CompletableFuture<>();

	private CompletableFuture<String> currentAnswer = new CompletableFuture<>();

	public Future<String> getFirstQuestion() {
		return questionFuture;
	}

	public void setFirstQuestion(String question) {
		this.questionFuture.complete(question);
		this.question = question;
	}

	public void inCompleteFuture() {
		this.question = null;
		this.answer = null;
		questionFuture = new CompletableFuture<>();
		currentAnswer = new CompletableFuture<>();
	}

	public Future<String> answerQuestion() {
		return currentAnswer;
	}
	public void setAnswerQuestion(String answer) {
		this.currentAnswer.complete(answer);
		this.answer = QuestionAnswer.valueOf(answer);
	}

}
