package nicolagraziani.billbuddy.expense.balance;

import nicolagraziani.billbuddy.expense.balance.payloads.GlobalBalanceSummaryDTO;
import nicolagraziani.billbuddy.expense.balance.payloads.GroupBalanceSummaryDTO;
import nicolagraziani.billbuddy.expense.balance.payloads.OptimizedPaymentDTO;
import nicolagraziani.billbuddy.expense.balance.payloads.SplitBalanceDTO;
import nicolagraziani.billbuddy.expense.balance.service.BalanceService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/balances")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/groups/{groupId}/details")
    public List<SplitBalanceDTO> getGroupSplitBalances(@PathVariable UUID groupId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.balanceService.calculateGroupSplitBalances(groupId, currentAuthenticatedUser);
    }

    @GetMapping("/groups/{groupId}/optimized")
    public List<OptimizedPaymentDTO> getOptimizedPayments(@PathVariable UUID groupId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.balanceService.calculateOptimizedPayments(groupId, currentAuthenticatedUser);
    }

    @GetMapping("/groups/{groupId}/summary")
    public List<GroupBalanceSummaryDTO> getGroupSummary(@PathVariable UUID groupId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.balanceService.calculateGroupSummary(groupId, currentAuthenticatedUser);
    }

    @GetMapping("/users/summary")
    public List<GlobalBalanceSummaryDTO> getGlobalSummary(@AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.balanceService.calculateGlobalSummary(currentAuthenticatedUser);
    }
}
