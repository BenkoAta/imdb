package hu.benkoata.imdb.dtos;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class CredentialsCommand {
    private String username;
    private String password;
    private int totpCode;
}
