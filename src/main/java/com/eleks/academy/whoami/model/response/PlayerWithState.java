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

	private String guess;

	private PlayerState state;

	private Boolean isLeaving;

	private CompletableFuture<String> questionFuture = new CompletableFuture<String>();

	private CompletableFuture<String> guessFuture = new CompletableFuture<String>();

	private CompletableFuture<String> currentAnswer = new CompletableFuture<>();

	private CompletableFuture<String> currentGuess = new CompletableFuture<>();

	public Future<String> getFirstQuestion() {
		return questionFuture;
	}

	public Future<String> getFirstGuess() {
		return guessFuture;
	}

	public  Future<Object> getQuestionMessage(){
		return CompletableFuture.anyOf(guessFuture, questionFuture);
	}

	public void setFirstQuestion(String question) {
		this.questionFuture.complete(question);
		this.question = question;
	}

	public void setFirstGuess(String guess) {
		this.guessFuture.complete(guess);
		this.guess = guess;
	}

	public void inCompleteFuture() {
		this.question = null;
		this.answer = null;
		this.guess = null;
		questionFuture = new CompletableFuture<>();
		currentAnswer = new CompletableFuture<>();
		currentGuess = new CompletableFuture<>();
		guessFuture = new CompletableFuture<>();
	}

	public Future<String> answerQuestion() {
		return currentAnswer;
	}

	public void setAnswerQuestion(String answer) {
		this.currentAnswer.complete(answer);
		this.answer = QuestionAnswer.valueOf(answer);
	}

	public Future<String> answerGuess() {
		return currentGuess;
	}

	public void setAnswerGuess(String answer) {
		this.currentGuess.complete(answer);
		this.guess = answer;
	}

}
