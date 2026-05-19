package nicolagraziani.billbuddy.expense.controllers;

import nicolagraziani.billbuddy.expense.payloads.settlement.SettlementFilterDTO;
import nicolagraziani.billbuddy.expense.payloads.settlement.SettlementResponseDTO;
import nicolagraziani.billbuddy.expense.services.SettlementService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupSettlementController {

    private final SettlementService settlementService;

    public GroupSettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/{groupId}/settlements")
    public Page<SettlementResponseDTO> findGroupSettlements(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            SettlementFilterDTO filters,
            @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.settlementService.findGroupSettlements(page, size, sortBy, currentAuthenticatedUser, filters, groupId);
    }
}
