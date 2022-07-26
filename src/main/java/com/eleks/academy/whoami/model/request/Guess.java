package com.eleks.academy.whoami.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Guess {
    @Size(min =2, max = 128, message = "message must be more than 2 and less than 128 characters")
    @NotBlank
    private String message;
}
