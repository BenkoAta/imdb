package hu.benkoata.imdb.dtos;

import lombok.Data;

@Data
public class CredentialsCommand {
    private String username;
    private String password;
    private int totpCode;
}
