package com.example.crm.Config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.crm.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return userRepository.findByUsername(username)
                .<UserDetails>map(entityUser -> org.springframework.security.core.userdetails.User
                    .withUsername(entityUser.getUsername())
                    .password(entityUser.getPasswordHash())
                    .roles(entityUser.getRole().name())
                    .build()
                )
                .orElseThrow(() ->
                    new UsernameNotFoundException(
                            "User not found: " + username
                    )
                );
    }
}
