package com.valeria.service;

import com.valeria.entity.AppUser;
import com.valeria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService
{
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrName)
            throws UsernameNotFoundException {

        // ищем сначала по email
        AppUser user = userRepository.findByEmail(emailOrName)
                .orElseGet(() -> userRepository.findByName(emailOrName)
                        .orElseThrow(() ->
                                new UsernameNotFoundException("Пользователь не найден")));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole())
                .accountExpired(user.getIsAccountExpired())
                .accountLocked(user.getIsAccountLocked())
                .credentialsExpired(user.getIsCredentialsExpired())
                .disabled(user.getIsDisabled())
                .build();
    }
}
