package hu.benkoata.imdb.dtos;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateUserDto {
    private final Long id;
    private final String qrUrl;
}
