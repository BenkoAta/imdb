package hu.benkoata.imdb.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private long id;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
