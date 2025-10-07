package com.bank.dto;

import com.bank.model.AccountStatus;
import com.bank.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private Long id;

    private String accountNumber;

    private AccountType accountType;

    private BigDecimal balance;

    private String currency;

    private AccountStatus status;

    private UserInfo owner;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime closedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private String email;
    }

    public static AccountResponse fromAccount(com.bank.model.Account account) {
        AccountResponseBuilder builder = AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .closedAt(account.getClosedAt());

        if (account.getUser() != null) {
            builder.owner(UserInfo.builder()
                    .id(account.getUser().getId())
                    .username(account.getUser().getUsername())
                    .fullName(account.getUser().getFullName())
                    .email(account.getUser().getEmail())
                    .build());
        }

        return builder.build();
    }
}
