package hu.benkoata.imdb.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class JwtTokenDto {
    private String token;
    private LocalDateTime expiration;
}
