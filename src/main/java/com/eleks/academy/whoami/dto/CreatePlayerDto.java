package com.eleks.academy.whoami.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlayerDto {

	@NotBlank
	@Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()+~+]*$")
	String username;

	@Email
	@NotBlank
	String email;

	@NotBlank
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@#$!%*?&]{8,127}$")
	String password;
}