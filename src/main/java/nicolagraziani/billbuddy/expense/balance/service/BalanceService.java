package nicolagraziani.billbuddy.expense.balance.service;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.expense.balance.payloads.*;
import nicolagraziani.billbuddy.expense.entities.Expense;
import nicolagraziani.billbuddy.expense.entities.ExpenseSplit;
import nicolagraziani.billbuddy.expense.entities.Settlement;
import nicolagraziani.billbuddy.expense.enums.CurrencyCode;
import nicolagraziani.billbuddy.expense.services.ExpenseService;
import nicolagraziani.billbuddy.expense.services.ExpenseSplitService;
import nicolagraziani.billbuddy.expense.services.SettlementService;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.services.GroupMemberService;
import nicolagraziani.billbuddy.group.services.GroupService;
import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class BalanceService {

    private final ExpenseService expenseService;
    private final ExpenseSplitService expenseSplitService;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final UserService userService;
    private final SettlementService settlementService;

    public BalanceService(ExpenseService expenseService, ExpenseSplitService expenseSplitService, GroupService groupService, GroupMemberService groupMemberService, UserService userService, SettlementService settlementService) {
        this.expenseService = expenseService;
        this.expenseSplitService = expenseSplitService;
        this.groupService = groupService;
        this.groupMemberService = groupMemberService;
        this.userService = userService;
        this.settlementService = settlementService;
    }

    public List<SplitBalanceDTO> calculateGroupSplitBalances(UUID groupId, User currentUser) {
        Group foundGroup = this.groupService.findGroupById(groupId);
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());
        this.groupMemberService.validateMembership(foundGroup, foundUser);

        List<ExpenseSplit> splits = this.expenseSplitService.findExpenseSplitByGroup(foundGroup);

        return splits.stream().map(this::mapToSplitBalance)
                .filter(splitBalanceDTO -> splitBalanceDTO.remainingDebt().compareTo(BigDecimal.ZERO) > 0 && !splitBalanceDTO.debtorId().equals(splitBalanceDTO.creditorId())).toList();

    }

    //    CALCULATE DEBT FOR EACH GROUP MEMBER
    public List<UserBalanceDTO> calculateGroupUserBalance(UUID groupId, User currentUser) {
        List<SplitBalanceDTO> splitBalances = this.calculateGroupSplitBalances(groupId, currentUser);

        Map<String, BigDecimal> balances = new HashMap<>();
        for (SplitBalanceDTO split : splitBalances) {
            String key = split.debtorId() + "::"
                    + split.creditorId() + "::"
                    + split.currencyCode();

            balances.merge(key, split.remainingDebt(), BigDecimal::add);
        }

        List<UserBalanceDTO> result = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            String[] parts = entry.getKey().split("::");
            UUID debtorId = UUID.fromString(parts[0]);
            UUID creditorId = UUID.fromString(parts[1]);
            CurrencyCode currencyCode = CurrencyCode.valueOf(parts[2]);

            User debtor = this.userService.findActiveUserById(debtorId);
            User creditor = this.userService.findActiveUserById(creditorId);

            result.add(new UserBalanceDTO(
                    debtor.getUserId(),
                    debtor.getUsername(),
                    creditor.getUserId(),
                    creditor.getUsername(),
                    entry.getValue(),
                    currencyCode
            ));
        }
        return result;
    }

    //    CALCULATE NET BALANCE FOR EACH GROUP MEMBER
    public List<NetBalanceDTO> calculateGroupNetBalances(UUID groupId, User currentUser) {
        List<UserBalanceDTO> balances = this.calculateGroupUserBalance(groupId, currentUser);

        Map<String, BigDecimal> netBalances = new HashMap<>();
        for (UserBalanceDTO balance : balances) {
            String debtorKey = balance.debtorId() + "::" + balance.currencyCode();
            String creditorKey = balance.creditorId() + "::" + balance.currencyCode();
            netBalances.merge(debtorKey, balance.amount().negate(), BigDecimal::add);
            netBalances.merge(creditorKey, balance.amount(), BigDecimal::add);
        }

        List<NetBalanceDTO> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : netBalances.entrySet()) {
            String[] parts = entry.getKey().split("::");
            UUID userId = UUID.fromString(parts[0]);
            CurrencyCode currencyCode = CurrencyCode.valueOf(parts[1]);
            User user = this.userService.findActiveUserById(userId);

            if (entry.getValue().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            result.add(new NetBalanceDTO(user.getUserId(), user.getUsername(), entry.getValue(), currencyCode));
        }

        return result;
    }

    //    OPTIMIZED PAYMENT
    public List<OptimizedPaymentDTO> calculateOptimizedPayments(UUID groupId, User currentUser) {
        List<NetBalanceDTO> netBalances = this.calculateGroupNetBalances(groupId, currentUser);
        List<NetBalanceDTO> debtors = new ArrayList<>();
        List<NetBalanceDTO> creditors = new ArrayList<>();

        for (NetBalanceDTO balanceDTO : netBalances) {
            if (balanceDTO.netBalance().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(balanceDTO);
            } else if (balanceDTO.netBalance().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(balanceDTO);
            }
        }

        List<OptimizedPaymentDTO> optimized = new ArrayList<>();
        int debtorIndex = 0;
        int creditorIndex = 0;
        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            NetBalanceDTO debtor = debtors.get(debtorIndex);
            NetBalanceDTO creditor = creditors.get(creditorIndex);

            if (debtor.currencyCode() != creditor.currencyCode()) {
                throw new IllegalStateException("Currency mismatch during optimization");
            }

            BigDecimal amount = debtor.netBalance().abs().min(creditor.netBalance());
            optimized.add(new OptimizedPaymentDTO(
                    debtor.userId(),
                    debtor.username(),
                    creditor.userId(),
                    creditor.username(),
                    amount,
                    debtor.currencyCode()
            ));

            BigDecimal updatedDebtorBalance = debtor.netBalance().add(amount);
            BigDecimal updatedCreditorBalance = creditor.netBalance().subtract(amount);

            debtors.set(debtorIndex, new NetBalanceDTO(debtor.userId(), debtor.username(), updatedDebtorBalance, debtor.currencyCode()));
            creditors.set(creditorIndex, new NetBalanceDTO(creditor.userId(), creditor.username(), updatedCreditorBalance, creditor.currencyCode()));

            if (updatedDebtorBalance.compareTo(BigDecimal.ZERO) == 0) debtorIndex++;
            if (updatedCreditorBalance.compareTo(BigDecimal.ZERO) == 0) creditorIndex++;
        }

        return optimized;
    }

    //    CALCULATE GROUP SUMMARY
    public List<GroupBalanceSummaryDTO> calculateGroupSummary(UUID groupId, User currentUser) {
        Group group = this.groupService.findGroupById(groupId);
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());
        this.groupMemberService.validateMembership(group, foundUser);

        List<Expense> expenses = this.expenseService.findExpensesByGroupRaw(group);
        List<Settlement> settlements = this.settlementService.findSettlementByGroup(group);
        List<SplitBalanceDTO> splitBalances = this.calculateGroupSplitBalances(groupId, foundUser);
        List<NetBalanceDTO> netBalances = this.calculateGroupNetBalances(groupId, foundUser);
        List<OptimizedPaymentDTO> optimizedPayments = this.calculateOptimizedPayments(groupId, foundUser);

        Map<CurrencyCode, BigDecimal> totalExpenseMap = new HashMap<>();
        Map<CurrencyCode, BigDecimal> totalSettledMap = new HashMap<>();
        Map<CurrencyCode, BigDecimal> outstandingMap = new HashMap<>();

        for (Expense expense : expenses) {
            totalExpenseMap.merge(expense.getCurrencyCode(), expense.getTotalAmount(), BigDecimal::add);
        }
        for (Settlement settlement : settlements) {
            totalSettledMap.merge(settlement.getCurrencyCode(), settlement.getAmount(), BigDecimal::add);
        }
        for (SplitBalanceDTO split : splitBalances) {
            outstandingMap.merge(split.currencyCode(), split.remainingDebt(), BigDecimal::add);
        }

        Set<CurrencyCode> currencies = new HashSet<>();
        currencies.addAll(totalExpenseMap.keySet());
        currencies.addAll(totalSettledMap.keySet());
        currencies.addAll(outstandingMap.keySet());

        List<GroupBalanceSummaryDTO> result = new ArrayList<>();

        for (CurrencyCode currency : currencies) {
            BigDecimal totalExpenses = totalExpenseMap.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal totalSettled = totalSettledMap.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal totalOutstanding = outstandingMap.getOrDefault(currency, BigDecimal.ZERO);

            List<NetBalanceDTO> currencyNetBalances = netBalances.stream().filter(balance -> balance.currencyCode() == currency).toList();
            List<OptimizedPaymentDTO> currencyOptimizedPayments = optimizedPayments.stream().filter(payment -> payment.currencyCode() == currency).toList();

            result.add(new GroupBalanceSummaryDTO(
                    group.getGroupId(),
                    group.getName(),
                    currency,
                    totalExpenses,
                    totalSettled,
                    totalOutstanding,
                    currencyNetBalances,
                    currencyOptimizedPayments
            ));
        }
        return result;
    }

    //    CALCULATE GLOBAL SUMMARY
    public List<GlobalBalanceSummaryDTO> calculateGlobalSummary(User currentUser) {
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());

        List<Expense> paidExpenses = this.expenseService.findExpensesPaidByUserRaw(foundUser);
        List<Settlement> receivedSettlements = this.settlementService.findSettlementsByCreditor(foundUser);
        List<ExpenseSplit> userSplits = this.expenseSplitService.findExpenseSplitByUser(foundUser);
        List<ExpenseSplit> creditorSplits = this.expenseSplitService.findExpenseSplitByCreditor(foundUser);

        Map<CurrencyCode, BigDecimal> totalSpentMap = new HashMap<>();
        Map<CurrencyCode, BigDecimal> totalReceivedMap = new HashMap<>();
        Map<CurrencyCode, BigDecimal> totalOwedMap = new HashMap<>();
        Map<CurrencyCode, BigDecimal> totalToReceiveMap = new HashMap<>();

        for (Expense expense : paidExpenses) {
            totalSpentMap.merge(expense.getCurrencyCode(), expense.getTotalAmount(), BigDecimal::add);
        }
        for (Settlement settlement : receivedSettlements) {
            totalReceivedMap.merge(settlement.getCurrencyCode(), settlement.getAmount(), BigDecimal::add);
        }
        for (ExpenseSplit split : userSplits) {
            if (split.getExpense().getPaidBy().getUserId().equals(foundUser.getUserId())) {
                continue;
            }
            BigDecimal remaining = split.getAmountOwed().subtract(split.getAmountPaid());
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            totalOwedMap.merge(split.getExpense().getCurrencyCode(), remaining, BigDecimal::add);
        }
        for (ExpenseSplit split : creditorSplits) {
            if (split.getUser().getUserId().equals(foundUser.getUserId())) {
                continue;
            }
            BigDecimal remaining = split.getAmountOwed().subtract(split.getAmountPaid());
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            totalToReceiveMap.merge(split.getExpense().getCurrencyCode(), remaining, BigDecimal::add);
        }

        Set<CurrencyCode> currencies = new HashSet<>();
        currencies.addAll(totalSpentMap.keySet());
        currencies.addAll(totalReceivedMap.keySet());
        currencies.addAll(totalOwedMap.keySet());
        currencies.addAll(totalToReceiveMap.keySet());

        List<GlobalBalanceSummaryDTO> result = new ArrayList<>();
        for (CurrencyCode currency : currencies) {
            BigDecimal totalSpent = totalSpentMap.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal totalReceived = totalReceivedMap.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal totalOwed = totalOwedMap.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal totalToReceive = totalToReceiveMap.getOrDefault(currency, BigDecimal.ZERO);
            BigDecimal currentExposure = totalSpent.subtract(totalReceived);
            BigDecimal netBalance = totalToReceive.subtract(totalOwed);

            result.add(new GlobalBalanceSummaryDTO(
                    foundUser.getUserId(),
                    foundUser.getUsername(),
                    currency,
                    totalSpent,
                    totalReceived,
                    currentExposure,
                    totalOwed,
                    totalToReceive,
                    netBalance
            ));
        }

        return result;
    }

    //    MAP TO DTO
    private SplitBalanceDTO mapToSplitBalance(ExpenseSplit split) {
        BigDecimal remainingDebt = split.getAmountOwed().subtract(split.getAmountPaid());
        boolean settled = remainingDebt.compareTo(BigDecimal.ZERO) <= 0;

        return new SplitBalanceDTO(
                split.getExpense().getExpenseId(),
                split.getExpense().getTitle(),
                split.getExpenseSplitId(),
                split.getUser().getUserId(),
                split.getUser().getUsername(),
                split.getExpense().getPaidBy().getUserId(),
                split.getExpense().getPaidBy().getUsername(),
                split.getAmountOwed(),
                split.getAmountPaid(),
                remainingDebt,
                split.getExpense().getCurrencyCode(),
                settled
        );
    }
}
