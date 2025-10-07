package sptech.school.Lodgfy.business.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponseDTO {
    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponseDTO(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }


}
