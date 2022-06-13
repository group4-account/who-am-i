package com.eleks.academy.whoami.model.response;

import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerWithState {

	private PersistentPlayer player;

	private QuestionAnswer answer;

	private PlayerState state;
	
}
