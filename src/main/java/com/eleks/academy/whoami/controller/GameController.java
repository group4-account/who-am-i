package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.Message;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.TurnDetails;
import com.eleks.academy.whoami.repository.impl.QNAHistoryRepositoryImpl;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

import static com.eleks.academy.whoami.utils.StringUtils.Headers.PLAYER;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

	private final GameService gameService;

	@GetMapping
	public List<GameLight> findAvailableGames(@RequestHeader(PLAYER) String player) {
		return this.gameService.findAvailableGames(player);
	}
	public GameDetails createGame(@RequestHeader(PLAYER) String player,
								  @Valid @RequestBody NewGameRequest gameRequest) {
		return this.gameService.createGame(player, gameRequest);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Optional<GameDetails> findQuickGame(@RequestHeader(PLAYER) String player) {
		return gameService.findAvailableQuickGame(player);
	}

	@GetMapping("/{id}")
	public ResponseEntity<GameDetails> findById(@PathVariable("id") String id,
												@RequestHeader(PLAYER) String player) {
		return this.gameService.findByIdAndPlayer(id, player)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}


	@PostMapping("/{id}/players")
	public SynchronousPlayer enrollToGame(@PathVariable("id") String id,
										  @RequestHeader(PLAYER) String player) {
		return this.gameService.enrollToGame(id, player).orElseThrow(() -> new GameException("No player"));

	}

	@PostMapping("/{id}/characters")
	@ResponseStatus(HttpStatus.OK)
	public void suggestCharacter(@PathVariable("id") String id,
								 @RequestHeader(PLAYER) String player,
								 @Valid @RequestBody CharacterSuggestion suggestion) {
        Optional.of(player)
                .filter(string -> string.matches(".{2,50}"))
				.orElseThrow(() -> new GameException("Player name must be between 2 and 50 characters"));
		this.gameService.suggestCharacter(id, player, suggestion);
	}

	@GetMapping("/{id}/turn")
	public ResponseEntity<TurnDetails> findTurnInfo(@PathVariable("id") String id,
													@RequestHeader(PLAYER) String player) {
		return this.gameService.findTurnInfo(id, player)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
	@GetMapping("/all-players-count")
	public int getAllPlayersCount() {
		return this.gameService.getAllPlayersCount();
	}

	@GetMapping("/{id}/ready-players-count")
	public int getReadyPlayersCount(@PathVariable("id") String id,
									@RequestHeader(PLAYER) String player) {
		return this.gameService.getReadyPlayersCount(id, player);
	}
	@PostMapping("/{id}")
	public ResponseEntity<GameDetails> startGame(@PathVariable("id") String id,
												 @RequestHeader(PLAYER) String player) {
		return this.gameService.startGame(id, player)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/questions")
	public void askQuestion(@PathVariable("id") String id,
							@RequestHeader(PLAYER) String player,
							@Valid @Size(min = 1, max = 256, message = "message must be less than 256 characters")
								@RequestBody Message message) {
		this.gameService.askQuestion(id, player, message.getMessage());
	}

	@PostMapping("/{id}/guess")
	public void submitGuess(@PathVariable("id") String id,
							@RequestHeader(PLAYER) String player,
							@Valid @Size(min = 2, max = 128, message = "message must be less than 128 characters")
								@RequestBody Message message) {

		this.gameService.submitGuess(id, player, message.getMessage());
	}

	@GetMapping("/{id}/history")
	public List<QNAHistoryRepositoryImpl.Question> getHistory(@PathVariable("id") String id,
															  @RequestHeader(PLAYER) String player) {
		return this.gameService.getQnaHistory(id);
	}

	@PostMapping("/{id}/answer")
	public void answerQuestion(@PathVariable("id") String id,
							   @RequestHeader(PLAYER) String player, @RequestBody Message message) {
		this.gameService.answerQuestion(id, player, message.getMessage());

	}
	@DeleteMapping("/{id}/leave")
	public void leaveGame(@PathVariable("id") String id,
						  @RequestHeader(PLAYER) String player) {
		this.gameService.leaveGame(id, player);
	}
}
