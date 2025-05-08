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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/api/v1/account")
    public ResponseEntity<AccountDto> create(@Valid @RequestBody CreateAccountDto accountDto) {
        AccountDto createdAccount = accountService.create(accountDto);
        return ResponseEntity.ok(createdAccount);
    }

    @PostMapping("/api/v1/account/send-action-email")
    public ResponseEntity<Void> sendActionEmail(@Valid @RequestBody SendActionEmailDto sendActionEmailDto) {
        accountService.sendActionEmail(sendActionEmailDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/account/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailDto verifyEmailDto) {
        accountService.verifyEmail(verifyEmailDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/account/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        accountService.resetPassword(passwordResetDto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/account/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordDto) {
        accountService.updatePassword(updatePasswordDto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/account")
    public ResponseEntity<AccountDto> update(@Valid @RequestBody UpdateAccountDto updateAccountDto) {
        AccountDto updatedAccount = accountService.update(updateAccountDto);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/api/v1/account")
    public ResponseEntity<Void> delete() {
        accountService.delete();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/account")
    public ResponseEntity<AccountDto> get() {
        AccountDto accountDto = accountService.get();
        return ResponseEntity.ok(accountDto);
    }

}
