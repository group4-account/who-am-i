package com.eleks.academy.whoami.repository;

import com.eleks.academy.whoami.model.request.QuestionAnswer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class AddAnswerRequest {
    public Boolean IsGuess;
    public String gameId;
    public String playerName;
    public QuestionAnswer answer;
}
