package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.model.request.QuestionAnswer;

public final class AnswerQuestion extends Answer {
	private final QuestionAnswer questionAnswer;
	public AnswerQuestion(String player, QuestionAnswer questionAnswer) {
		super(player);
		this.questionAnswer = questionAnswer;
	}

	public static StartGameAnswer of(String player) {
		return new StartGameAnswer(player);
	}

}
