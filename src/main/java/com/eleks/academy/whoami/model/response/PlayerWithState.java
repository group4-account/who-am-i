package com.eleks.academy.whoami.model.response;

import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static com.eleks.academy.whoami.model.request.QuestionAnswer.*;

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

	public Future<String> getFutureQuestion() {
		return questionFuture;
	}

	public void setFutureQuestion(String question) {
		this.question = question;
		this.questionFuture.complete(question);
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
		this.answer = valueOf(answer);
		this.currentAnswer.complete(answer);
	}

	public void setAnswer(String answer) {
		this.answer = valueOf(answer);
	}
}
