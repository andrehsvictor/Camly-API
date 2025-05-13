package andrehsvictor.camly.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import andrehsvictor.camly.account.dto.AccountDto;
import andrehsvictor.camly.account.dto.CreateAccountDto;
import andrehsvictor.camly.account.dto.PasswordResetDto;
import andrehsvictor.camly.account.dto.SendActionEmailDto;
import andrehsvictor.camly.account.dto.UpdateAccountDto;
import andrehsvictor.camly.account.dto.UpdatePasswordDto;
import andrehsvictor.camly.account.dto.VerifyEmailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account management operations")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create a new account", description = "Creates a new user account with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid account data", content = @Content)
    })
    @PostMapping("/api/v1/account")
    public ResponseEntity<AccountDto> create(@Valid @RequestBody CreateAccountDto accountDto) {
        AccountDto createdAccount = accountService.create(accountDto);
        return ResponseEntity.status(201).body(createdAccount);
    }

    @Operation(summary = "Send action email", description = "Sends an email for account verification or password reset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Email not found", content = @Content)
    })
    @PostMapping("/api/v1/account/send-action-email")
    public ResponseEntity<Void> sendActionEmail(@Valid @RequestBody SendActionEmailDto sendActionEmailDto) {
        accountService.sendActionEmail(sendActionEmailDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Verify email address", description = "Validates email verification token and marks the account as verified")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid verification token", content = @Content)
    })
    @PostMapping("/api/v1/account/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailDto verifyEmailDto) {
        accountService.verifyEmail(verifyEmailDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset password", description = "Updates account password using a valid reset token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reset token or password", content = @Content)
    })
    @PostMapping("/api/v1/account/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        accountService.resetPassword(passwordResetDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update password", description = "Changes the authenticated user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @PutMapping("/api/v1/account/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordDto) {
        accountService.updatePassword(updatePasswordDto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update account", description = "Updates the authenticated user's account information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid account data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @PutMapping("/api/v1/account")
    public ResponseEntity<AccountDto> update(@Valid @RequestBody UpdateAccountDto updateAccountDto) {
        AccountDto updatedAccount = accountService.update(updateAccountDto);
        return ResponseEntity.ok(updatedAccount);
    }

    @Operation(summary = "Delete account", description = "Deletes the authenticated user's account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @DeleteMapping("/api/v1/account")
    public ResponseEntity<Void> delete() {
        accountService.delete();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get account information", description = "Retrieves the authenticated user's account details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token is required", content = @Content)
    })
    @GetMapping("/api/v1/account")
    public ResponseEntity<AccountDto> get() {
        AccountDto accountDto = accountService.get();
        return ResponseEntity.ok(accountDto);
    }
}