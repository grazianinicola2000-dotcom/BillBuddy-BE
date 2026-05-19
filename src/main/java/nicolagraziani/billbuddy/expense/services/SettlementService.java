package nicolagraziani.billbuddy.expense.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.expense.entities.ExpenseSplit;
import nicolagraziani.billbuddy.expense.entities.Settlement;
import nicolagraziani.billbuddy.expense.enums.ExpenseType;
import nicolagraziani.billbuddy.expense.payloads.settlement.CreateSettlementDTO;
import nicolagraziani.billbuddy.expense.payloads.settlement.SettlementFilterDTO;
import nicolagraziani.billbuddy.expense.payloads.settlement.SettlementResponseDTO;
import nicolagraziani.billbuddy.expense.repositories.SettlementRepository;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.services.GroupMemberService;
import nicolagraziani.billbuddy.group.services.GroupService;
import nicolagraziani.billbuddy.user.Role;
import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final ExpenseSplitService expenseSplitService;

    public SettlementService(SettlementRepository settlementRepository, UserService userService, GroupService groupService, GroupMemberService groupMemberService, ExpenseSplitService expenseSplitService) {
        this.settlementRepository = settlementRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.groupMemberService = groupMemberService;
        this.expenseSplitService = expenseSplitService;
    }

    public Settlement findSettlementById(UUID settlementId) {
        return this.settlementRepository.findById(settlementId).orElseThrow(() -> new NotFoundException(settlementId));
    }

    public SettlementResponseDTO getSettlementResponseById(UUID settlementId, User currenUser) {
        Settlement settlement = this.findSettlementById(settlementId);
        User foundUser = this.userService.findActiveUserById(currenUser.getUserId());
        validateSettlementAccess(settlement, foundUser);
        return mapToResponse(settlement);
    }

    @Transactional
    public SettlementResponseDTO createSettlement(CreateSettlementDTO body, User currentUser) {
        User debtor = this.userService.findActiveUserById(currentUser.getUserId());
        ExpenseSplit split = this.expenseSplitService.findExpenseSplitById(body.expenseSplitId());
        if (split.getExpense().getExpenseType() == ExpenseType.PERSONAL) {
            throw new BadRequestException("Personal expenses cannot have settlements");
        }
        User creditor = split.getExpense().getPaidBy();
        Group group = split.getExpense().getGroup();

        this.groupMemberService.validateMembership(group, debtor);
        this.groupMemberService.validateMembership(group, creditor);
        if (!split.getUser().getUserId().equals(debtor.getUserId())) {
            throw new AuthorizationDeniedException("You cannot pay someone else's debt");
        }

        BigDecimal remainingDebt = split.getAmountOwed().subtract(split.getAmountPaid());
        if (body.amount().compareTo(remainingDebt) > 0) {
            throw new BadRequestException("Settlement amount exceeds remaining debt");
        }
        if (body.currencyCode() != split.getExpense().getCurrencyCode()) {
            throw new BadRequestException("Settlement currency must match expense currency");
        }

        split.setAmountPaid(split.getAmountPaid().add(body.amount()));

        Settlement settlement = new Settlement(debtor, creditor, body.amount(), body.currencyCode(), group, body.note(), split);
        Settlement savedSettlement = this.settlementRepository.save(settlement);

        log.info("Settlement created: {} paid {} {}", debtor.getUsername(), creditor.getUsername(), body.amount());
        return mapToResponse(savedSettlement);
    }

    //    PAID BY USER
    public Page<SettlementResponseDTO> findPaidByUser(int page, int size, String sortBy, User currentUser, SettlementFilterDTO filters) {
        if (size > 100 || size < 1) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());
        Page<Settlement> settlements;

        if (filters != null && filters.currencyCode() != null) {
            settlements = this.settlementRepository.findByDebtorAndCurrencyCode(foundUser, filters.currencyCode(), pageable);
        } else {
            settlements = this.settlementRepository.findByDebtor(foundUser, pageable);
        }

        return settlements.map(this::mapToResponse);
    }

    public Page<SettlementResponseDTO> findGroupSettlements(int page, int size, String sortBy, User currentUser, SettlementFilterDTO filters, UUID groupId) {
        if (size > 100 || size < 1) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        Group group = this.groupService.findGroupById(groupId);
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());

        this.groupMemberService.validateMembership(group, foundUser);

        Page<Settlement> settlements;
        if (filters != null && filters.currencyCode() != null) {
            settlements = this.settlementRepository.findByGroupAndCurrencyCode(group, filters.currencyCode(), pageable);
        } else {
            settlements = this.settlementRepository.findByGroup(group, pageable);
        }
        return settlements.map(this::mapToResponse);
    }

    @Transactional
    public void deleteSettlement(UUID settlementId, User currentUser) {
        Settlement settlement = this.findSettlementById(settlementId);
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());

        ExpenseSplit split = settlement.getExpenseSplit();
        split.setAmountPaid(split.getAmountPaid().subtract(settlement.getAmount()));

        boolean isPayer = settlement.getDebtor().getUserId().equals(foundUser.getUserId());
        boolean isSystemAdmin = foundUser.getRole() == Role.ADMIN;
        if (!isPayer && !isSystemAdmin) {
            throw new AuthorizationDeniedException("You are not allowed to delete this settlement");
        }

        this.settlementRepository.delete(settlement);
        log.info("Settlement {} deleted by {}", settlement.getSettlementId(), foundUser.getUsername());
    }

    private void validateSettlementAccess(Settlement settlement, User user) {
        boolean isPayer = settlement.getCreditor().getUserId().equals(user.getUserId());
        boolean isReceiver = settlement.getDebtor().getUserId().equals(user.getUserId());
        boolean isSystemAdmin = user.getRole() == Role.ADMIN;

        if (!isPayer && !isReceiver && !isSystemAdmin) {
            throw new AuthorizationDeniedException("You are not allowed to access this settlement");
        }
    }

    private SettlementResponseDTO mapToResponse(Settlement settlement) {
        return new SettlementResponseDTO(
                settlement.getSettlementId(),
                settlement.getDebtor().getUserId(),
                settlement.getDebtor().getUsername(),
                settlement.getCreditor().getUserId(),
                settlement.getCreditor().getUsername(),
                settlement.getGroup() != null ? settlement.getGroup().getGroupId() : null,
                settlement.getGroup() != null ? settlement.getGroup().getName() : null,
                settlement.getAmount(),
                settlement.getCurrencyCode(),
                settlement.getNote(), settlement.getCreatedAt()
        );
    }
}
