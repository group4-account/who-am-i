package com.eleks.academy.whoami.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlayerDto {

	@NotBlank
	@Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()+~+]*$", message = "Symbol _ is forbidden")
	@Length(min = 2, max = 50)
	String username;

	@Email
	@NotBlank
	@Length(min = 3)
	String email;

	@NotBlank
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()+~+])[A-Za-z\\d!@#$%^&*()+~+]{8,127}$",
			message = "The password shall contain at least 8 symbols (max 127):" +
					" min 1 upper-case, 1 lower-case letter, special symbol and number")
	String password;
}
