package nicolagraziani.billbuddy.exceptions;

import java.time.LocalDateTime;

public record ErrorsDTO(String message, LocalDateTime timestamp) {
}
