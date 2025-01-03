package hu.benkoata.imdb.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class CreateUserCommand {
    @NotNull
    @NotEmpty
    private String fullName;
    @NotNull
    @NotEmpty
    private String email;
    @NotNull
    @NotEmpty
    private String password;
    @NotNull
    @NotEmpty
    @Length(min=2, max=2)
    private String preferredLanguageId = "hu";
    @NotNull
    @NotEmpty
    @Length(min=2, max=2)
    private String countryId = "HU";

    public CreateUserCommand(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }
}
