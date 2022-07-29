package com.eleks.academy.whoami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class AuthenticationDTO  {

		private String email;

	private String password;


}
