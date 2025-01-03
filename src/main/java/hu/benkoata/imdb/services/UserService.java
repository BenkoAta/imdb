package hu.benkoata.imdb.services;

import hu.benkoata.imdb.dtos.UserDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ModelMapper modelMapper;
    public UserDto getUserDetails(UserDetails userDetails) {
        return modelMapper.map(userDetails, UserDto.class);
    }
}
