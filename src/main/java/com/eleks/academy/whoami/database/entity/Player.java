package com.eleks.academy.whoami.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.*;

@Entity
@Table(name = "players", schema = "for_heroku", catalog = "dac7okrn1q4c25")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Integer id;

	private String username;

	private String password;

	private String email;
}
