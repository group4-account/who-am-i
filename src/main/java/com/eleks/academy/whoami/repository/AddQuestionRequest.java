package com.eleks.academy.whoami.repository;

import com.eleks.academy.whoami.model.request.QuestionAnswer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class AddQuestionRequest {
    public Boolean IsGuess;
    public String gameId;
    public String playerName;
    public String question;

}
