package com.bank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Le nom d'utilisateur ou l'email est obligatoire")
    private String usernameOrEmail;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
