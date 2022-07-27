package com.eleks.academy.whoami.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuessMessage {
    @Size(min = 2, max = 128, message = "message must be between 2 and 128 characters")
    private String message;

}

