package nicolagraziani.billbuddy.expense.controllers;

import nicolagraziani.billbuddy.exceptions.ValidationException;
import nicolagraziani.billbuddy.expense.payloads.settlement.CreateSettlementDTO;
import nicolagraziani.billbuddy.expense.payloads.settlement.SettlementFilterDTO;
import nicolagraziani.billbuddy.expense.payloads.settlement.SettlementResponseDTO;
import nicolagraziani.billbuddy.expense.services.SettlementService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/settlements")
public class SettlementController {
    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/{settlementId}")
    public SettlementResponseDTO getSettlementById(@PathVariable UUID settlementId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.settlementService.getSettlementResponseById(settlementId, currentAuthenticatedUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SettlementResponseDTO createSettlement(@AuthenticationPrincipal User currentAuthenticatedUser, @Validated @RequestBody CreateSettlementDTO body, BindingResult validation) {
        if (validation.hasErrors()) {
            List<String> errors = validation.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            throw new ValidationException(errors);
        }
        return this.settlementService.createSettlement(body, currentAuthenticatedUser);
    }

    @GetMapping("/me/paid")
    public Page<SettlementResponseDTO> findPaidByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            SettlementFilterDTO filters,
            @AuthenticationPrincipal User currentAuthenticatedUser
    ) {
        return this.settlementService.findPaidByUser(page, size, sortBy, currentAuthenticatedUser, filters);
    }

    @DeleteMapping("/{settlementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSettlement(@PathVariable UUID settlementId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        this.settlementService.deleteSettlement(settlementId, currentAuthenticatedUser);
    }
}
