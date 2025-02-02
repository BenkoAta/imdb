package hu.benkoata.imdb.dtos;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class CredentialsCommand {
    private String username;
    private String password;
    private int totpCode;

    public CredentialsCommand(String username, String password) {
        this(username, password, -1);
    }

    public CredentialsCommand(String username, String password, int totpCode) {
        this.username = username;
        this.password = password;
        this.totpCode = totpCode;
    }
}
