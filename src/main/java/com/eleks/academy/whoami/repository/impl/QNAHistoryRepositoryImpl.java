package com.eleks.academy.whoami.repository.impl;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import com.eleks.academy.whoami.repository.AddAnswerRequest;
import com.eleks.academy.whoami.repository.AddQuestionRequest;
import com.eleks.academy.whoami.repository.QNAHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Component
public class QNAHistoryRepositoryImpl implements QNAHistoryRepository{

    @AllArgsConstructor
    public class Answer{
        public String PlayerId;
        public Date AnsweredOn;
        public Boolean WasSetAutomatically;
        public QuestionAnswer Answer;
        public Answer(){
            this.AnsweredOn = new Date();
        }
    }

    @AllArgsConstructor
    public class Question{
        public String GameId;
        public Boolean isActiveQuestion;
        public Boolean IsGuess;
        public String Question;
        public Date AskedOn;
        public String PlayerId;
        public List<Answer> Answers;
        public List<String> Players;
        private List<com.eleks.academy.whoami.model.response.PlayerWithState> participatingPlayers;

        public Question(){
            this.AskedOn = new Date();
            this.Answers = new ArrayList<>();
        }
    }

    private final ConcurrentLinkedQueue<Question>  questionsList = new ConcurrentLinkedQueue<>();

    @Override
    public void AddQuestionRequest(AddQuestionRequest addQuestionRequest, List<com.eleks.academy.whoami.model.response.PlayerWithState> players) {
        var question = new Question();
        question.participatingPlayers = players;
        question.Players = players.stream().map(x -> x.getPlayer().getId()).collect(Collectors.toList());
        question.GameId = addQuestionRequest.gameId;
        question.PlayerId = addQuestionRequest.playerName;
        question.IsGuess = addQuestionRequest.IsGuess;
        question.Question = addQuestionRequest.question;
        question.isActiveQuestion = true;
        questionsList.stream().filter(q -> q.GameId.equalsIgnoreCase(question.GameId)).forEach(x -> x.isActiveQuestion = false);
        questionsList.add(question);

    }

    @Override
    public void AddAnswerRequest(AddAnswerRequest addAnswerRequest) {
        var question = this.questionsList.stream()
                .filter(q -> q.GameId.equalsIgnoreCase(addAnswerRequest.gameId))
                .reduce((first, second) -> second);
        var answer = new Answer();
        answer.PlayerId = addAnswerRequest.playerName;
        answer.Answer = addAnswerRequest.answer;
        answer.WasSetAutomatically = false;

        question.ifPresent(q -> q.Answers.add(answer));

    }

    @Override
    public List<Question> GetGameHistory(String gameId){
        return questionsList.stream().filter(x -> x.GameId.equalsIgnoreCase(gameId))
                .map(x -> {
                    var answers = GetAnswers(x);
                   return new Question(x.GameId, x.isActiveQuestion,x.IsGuess, x.Question, x.AskedOn, x.PlayerId, answers, x.Players, x.participatingPlayers);
                }).collect(Collectors.toList());
    }

    private List<Answer> GetAnswers(Question question) {
        if (!question.isActiveQuestion){
            return question.Answers;
        }

        var currentQuestion = question.participatingPlayers.stream()
                .filter(player -> player.getState() == PlayerState.ASKING  || player.getState() == PlayerState.ASKED)
                .findFirst()
                .map(PlayerWithState::getQuestion);

        var answersList = new ArrayList<Answer>();
        answersList.addAll(question.Answers);
        question.participatingPlayers.forEach(player ->{
            if (answersList.stream().noneMatch(a -> a.PlayerId.equalsIgnoreCase(player.getPlayer().getId()))){

                if ((currentQuestion.isPresent() ? currentQuestion.get() : "") == "" || !(currentQuestion.isPresent() ? currentQuestion.get() : "").equalsIgnoreCase(question.Question)) {
                    question.isActiveQuestion = false;
                    answersList.add(new Answer(player.getPlayer().getId(), new Date(), true, QuestionAnswer.NOT_SURE));
                }
            }
        });

        answersList.removeIf(x -> x.PlayerId.equalsIgnoreCase(question.PlayerId));
        question.Answers = answersList;
        return answersList;
    }
}
