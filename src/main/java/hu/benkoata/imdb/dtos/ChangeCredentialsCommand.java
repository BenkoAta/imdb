package hu.benkoata.imdb.dtos;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = {"password", "newPassword"})
public class ChangeCredentialsCommand {
    private String username;
    private String password;
    private int totpCode;
    private String newPassword;
}
