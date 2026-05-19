package nicolagraziani.billbuddy.expense.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.expense.entities.Expense;
import nicolagraziani.billbuddy.expense.entities.ExpenseCategory;
import nicolagraziani.billbuddy.expense.entities.ExpenseSplit;
import nicolagraziani.billbuddy.expense.enums.ExpenseType;
import nicolagraziani.billbuddy.expense.payloads.expense.CreateExpenseDTO;
import nicolagraziani.billbuddy.expense.payloads.expense.ExpenseResponseDTO;
import nicolagraziani.billbuddy.expense.payloads.expense.ExpenseSplitResponseDTO;
import nicolagraziani.billbuddy.expense.payloads.expense.GetExpensesFilterDTO;
import nicolagraziani.billbuddy.expense.repositories.ExpenseRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final ExpenseCategoryService expenseCategoryService;

    public ExpenseService(ExpenseRepository expenseRepository, UserService userService, GroupService groupService, GroupMemberService groupMemberService, ExpenseCategoryService expenseCategoryService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.groupMemberService = groupMemberService;
        this.expenseCategoryService = expenseCategoryService;
    }

    public Expense findExpenseById(UUID expenseId) {
        return this.expenseRepository.findById(expenseId).orElseThrow(() -> new NotFoundException(expenseId));
    }

    public ExpenseResponseDTO getExpenseById(UUID expenseId, User currentUser) {
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());
        Expense expense = this.findExpenseById(expenseId);
        validateExpenseAccess(expense, foundUser);
        return mapToResponse(expense);
    }

    @Transactional
    public ExpenseResponseDTO createExpense(CreateExpenseDTO body, User currentUser) {
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());
        List<UUID> participantIds = getParticipants(body, foundUser);
        validateUniqueParticipants(participantIds);
        validatePersonalExpense(body, foundUser);
        validateAmountPrecision(body.totalAmount());
        validateExpenseDate(body.expenseDate());

        Group group = null;
        if (body.groupId() != null) {
            group = this.groupService.findGroupById(body.groupId());
            validateGroupMembership(group, foundUser, participantIds);
        }

        ExpenseCategory category = null;
        if (body.categoryId() != null) {
            category = this.expenseCategoryService.findExpenseCategoryById(body.categoryId());
        }

        ExpenseType expenseType = body.groupId() != null ? ExpenseType.GROUP : ExpenseType.PERSONAL;
        Expense expense = new Expense(body.title(), body.description(), body.totalAmount(), expenseType, group, foundUser, category, body.expenseDate(), body.currencyCode());

        List<User> participants = getParticipantUsers(participantIds);
        List<ExpenseSplit> splits = createEqualSplits(expense, participants, body.totalAmount());

        splits.forEach(expense::addSplit);

        Expense savedExpense = this.expenseRepository.save(expense);
        log.info("Expense '{}' created by user {}", savedExpense.getTitle(), foundUser.getUsername());

        return mapToResponse(savedExpense);
    }

    private List<UUID> getParticipants(CreateExpenseDTO body, User user) {
        if (body.groupId() == null) {
            if (body.participantIds() == null || body.participantIds().isEmpty()) {
                return List.of(user.getUserId());
            }
        }
        if (body.participantIds() == null || body.participantIds().isEmpty()) {
            throw new BadRequestException("At least one participant is required");
        }
        return body.participantIds();
    }

    //    VALIDATE PERSONAL EXPENSES
    private void validatePersonalExpense(CreateExpenseDTO body, User user) {
        List<UUID> participantsIds = getParticipants(body, user);
        if (body.groupId() != null) {
            return;
        }
        if (participantsIds.size() != 1) {
            throw new BadRequestException("Personal expenses can only have one participant");
        }
        UUID participantId = participantsIds.getFirst();
        if (!participantId.equals(user.getUserId())) {
            throw new BadRequestException("Personal expense participant must match the payer");
        }
    }

    //    VALIDATE EXPENSE DATE
    private void validateExpenseDate(LocalDate expenseDate) {
        if (expenseDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Expense date must be in the past");
        }
    }

    //    VALIDATE AMOUNT PRECISION
    private void validateAmountPrecision(BigDecimal amount) {
        if (amount.scale() > 2) {
            throw new BadRequestException("Amount cannot have more than 2 decimal");
        }
    }


    //    VALIDATE UNIQUE PARTICIPANTS
    private void validateUniqueParticipants(List<UUID> participantIds) {
        Set<UUID> uniqueParticipants = new HashSet<>(participantIds);
        if (uniqueParticipants.size() != participantIds.size()) {
            throw new BadRequestException("Participant list contains duplicates");
        }
    }


    //    VALIDATE GROUP MEMBERS
    private List<User> getParticipantUsers(List<UUID> participantIds) {
        return participantIds.stream().map(this.userService::findActiveUserById).toList();
    }

    private void validateGroupMembership(Group group, User paidBy, List<UUID> participantIds) {
        if (!this.groupMemberService.existsByGroupAndUser(group, paidBy)) {
            throw new AuthorizationDeniedException("You are not a member of this group");
        }
        List<User> participants = getParticipantUsers(participantIds);
        for (User participant : participants) {
            if (!this.groupMemberService.existsByGroupAndUser(group, participant)) {
                throw new BadRequestException("All participants must belong to the group");
            }
        }
    }

    //    CREATE EQUAL SPLITS (last takes the difference)
    private List<ExpenseSplit> createEqualSplits(Expense expense, List<User> participants, BigDecimal totalAmount) {
        if (participants.isEmpty()) {
            throw new BadRequestException("Participants list cannot be empty");
        }
        List<ExpenseSplit> splits = new ArrayList<>();

        BigDecimal participantCount = BigDecimal.valueOf(participants.size());
        BigDecimal splitAmount = totalAmount.divide(participantCount, 2, RoundingMode.HALF_UP);
        BigDecimal accumulated = BigDecimal.ZERO;

        for (int i = 0; i < participants.size(); i++) {
            BigDecimal finalAmount = splitAmount;
            if (i == participants.size() - 1) {
                finalAmount = totalAmount.subtract(accumulated);
            }
            accumulated = accumulated.add(finalAmount);

            ExpenseSplit split = new ExpenseSplit(expense, participants.get(i), finalAmount);
            splits.add(split);
        }
        return splits;
    }

    //  CREATE RESPONSE
    private ExpenseResponseDTO mapToResponse(Expense expense) {

        List<ExpenseSplitResponseDTO> splits = expense.getSplits().stream().map(split -> new ExpenseSplitResponseDTO(
                        split.getExpenseSplitId(),
                        split.getUser().getUserId(),
                        split.getUser().getUsername(),
                        split.getUser().getAvatarURL(),
                        split.getAmountOwed(),
                        split.getAmountPaid()))
                .toList();

        return new ExpenseResponseDTO(
                expense.getExpenseId(),
                expense.getTitle(),
                expense.getDescription(),
                expense.getTotalAmount(),
                expense.getExpenseType(),
                expense.getCurrencyCode(),
                expense.getExpenseDate(),
                expense.getPaidBy().getUserId(),
                expense.getPaidBy().getUsername(),
                expense.getGroup() != null ? expense.getGroup().getGroupId() : null,
                expense.getGroup() != null ? expense.getGroup().getName() : null,
                expense.getExpenseCategory() != null ? expense.getExpenseCategory().getExpenseCategoryId() : null,
                expense.getExpenseCategory() != null ? expense.getExpenseCategory().getName() : null,
                splits,
                expense.getCreatedAt()
        );
    }

    //    VALIDATE EXPENSE ACCESS
    private void validateExpenseAccess(Expense expense, User user) {
        boolean isPayer = expense.getPaidBy().getUserId().equals(user.getUserId());
        boolean isParticipant = expense.getSplits().stream().anyMatch(expenseSplit -> expenseSplit.getUser().getUserId().equals(user.getUserId()));
        boolean isSystemAdmin = user.getRole() == Role.ADMIN;

        if (!isPayer && !isParticipant && !isSystemAdmin) {
            throw new AuthorizationDeniedException("You are not allowed to access this expense");
        }
    }

    //    FIND MY EXPENSES
    public Page<ExpenseResponseDTO> findExpensesPaidByUser(int page, int size, String sortBy, User currentUser, GetExpensesFilterDTO filters) {
        if (size > 100 || size < 1) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());

        Page<Expense> expenses;
        if (filters != null && filters.expenseType() != null) {
            expenses = this.expenseRepository.findByPaidByAndExpenseType(foundUser, filters.expenseType(), pageable);
        } else {
            expenses = this.expenseRepository.findByPaidBy(foundUser, pageable);
        }
        return expenses.map(this::mapToResponse);
    }

    //    FIND GROUP EXPENSES
    public Page<ExpenseResponseDTO> findGroupExpenses(int page, int size, String sortBy, User currentUser, GetExpensesFilterDTO filters, UUID groupId) {
        if (size > 100 || size < 1) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Group foundGroup = this.groupService.findGroupById(groupId);
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());
        this.groupMemberService.validateMembership(foundGroup, foundUser);

        Page<Expense> expenses;
        if (filters != null && filters.expenseType() != null) {
            expenses = this.expenseRepository.findByGroupAndExpenseType(foundGroup, filters.expenseType(), pageable);
        } else {
            expenses = this.expenseRepository.findByGroup(foundGroup, pageable);
        }
        return expenses.map(this::mapToResponse);
    }

    //    DELETE EXPENSE
    @Transactional
    public void deleteExpense(UUID expenseId, User currentUser) {
        Expense foundExpense = this.findExpenseById(expenseId);
        User foundUser = this.userService.findActiveUserById(currentUser.getUserId());

        boolean isPayer = foundExpense.getPaidBy().getUserId().equals(foundUser.getUserId());
        boolean isSystemAdmin = foundUser.getRole() == Role.ADMIN;

        boolean isGroupOwner = false;
        if (foundExpense.getGroup() != null) {
            isGroupOwner = this.groupMemberService.isOwnerOrSystemAdmin(foundExpense.getGroup(), foundUser);
        }

        if (!isPayer && !isGroupOwner && !isSystemAdmin) {
            throw new AuthorizationDeniedException("You are not allowed to delete this expense");
        }

        this.expenseRepository.delete(foundExpense);
        log.info("Expense '{}' deleted by user {}", foundExpense.getTitle(), foundUser.getUsername());
    }
}
