package com.eleks.academy.whoami.repository;

import com.eleks.academy.whoami.model.response.PlayerWithState;
import com.eleks.academy.whoami.repository.impl.QNAHistoryRepositoryImpl;

import java.util.List;

public interface QNAHistoryRepository {
    void AddQuestionRequest(AddQuestionRequest addQuestionRequest, List<PlayerWithState> players);
    void AddAnswerRequest(AddAnswerRequest addAnswerRequest);

    List<QNAHistoryRepositoryImpl.Question> GetGameHistory(String gameId);
}
