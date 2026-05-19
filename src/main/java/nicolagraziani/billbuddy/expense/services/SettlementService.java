package nicolagraziani.billbuddy.expense.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.expense.entities.Settlement;
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

import java.util.UUID;

@Service
@Slf4j
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    public SettlementService(SettlementRepository settlementRepository, UserService userService, GroupService groupService, GroupMemberService groupMemberService) {
        this.settlementRepository = settlementRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.groupMemberService = groupMemberService;
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
        User payer = this.userService.findActiveUserById(currentUser.getUserId());
        User receiver = this.userService.findActiveUserById(body.receiverId());
        Group group = this.groupService.findGroupById(body.groupId());
        validateSettlementUser(payer, receiver);
        validateSettlementMembership(group, payer, receiver);

        Settlement settlement = new Settlement(payer, receiver, body.amount(), body.currencyCode(), group, body.note());
        Settlement savedSettlement = this.settlementRepository.save(settlement);

        log.info("Settlement created: {} paid {} {}", payer.getUsername(), receiver.getUsername(), body.amount());
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
            settlements = this.settlementRepository.findByPayerAndCurrencyCode(foundUser, filters.currencyCode(), pageable);
        } else {
            settlements = this.settlementRepository.findByPayer(foundUser, pageable);
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

        boolean isPayer = settlement.getPayer().getUserId().equals(foundUser.getUserId());
        boolean isSystemAdmin = foundUser.getRole() == Role.ADMIN;
        if (!isPayer && !isSystemAdmin) {
            throw new AuthorizationDeniedException("You are not allowed to delete this settlement");
        }

        this.settlementRepository.delete(settlement);
        log.info("Settlement {} deleted by {}", settlement.getSettlementId(), foundUser.getUsername());
    }

    private void validateSettlementUser(User payer, User receiver) {
        if (payer.getUserId().equals(receiver.getUserId())) {
            throw new BadRequestException("You cannot create a settlement with yourself");
        }
    }

    private void validateSettlementMembership(Group group, User payer, User receiver) {
        this.groupMemberService.validateMembership(group, payer);
        this.groupMemberService.validateMembership(group, receiver);
    }

    private void validateSettlementAccess(Settlement settlement, User user) {
        boolean isPayer = settlement.getPayer().getUserId().equals(user.getUserId());
        boolean isReceiver = settlement.getReceiver().getUserId().equals(user.getUserId());
        boolean isSystemAdmin = user.getRole() == Role.ADMIN;

        if (!isPayer && !isReceiver && !isSystemAdmin) {
            throw new AuthorizationDeniedException("You are not allowed to access this settlement");
        }
    }

    private SettlementResponseDTO mapToResponse(Settlement settlement) {
        return new SettlementResponseDTO(
                settlement.getSettlementId(),
                settlement.getPayer().getUserId(),
                settlement.getPayer().getUsername(),
                settlement.getReceiver().getUserId(),
                settlement.getReceiver().getUsername(),
                settlement.getGroup() != null ? settlement.getGroup().getGroupId() : null,
                settlement.getGroup() != null ? settlement.getGroup().getName() : null,
                settlement.getAmount(),
                settlement.getCurrencyCode(),
                settlement.getNote(), settlement.getCreatedAt()
        );
    }
}
