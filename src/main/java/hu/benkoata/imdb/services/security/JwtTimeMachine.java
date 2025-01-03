package hu.benkoata.imdb.services.security;

import io.jsonwebtoken.Clock;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@RequiredArgsConstructor
public class JwtTimeMachine implements Clock {
    private final Date date;
    @Override
    public Date now() {
        return date;
    }
}
