package com.valeria.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ApplicationUserRole
{
    // Роли пользователей с наборами разрешений
    USER(Set.of(Permission.READ)),
    ADMIN(Set.of(Permission.READ, Permission.MODIFICATION));

    private final Set<Permission> permissions;

    // Получить права доступа
    public Set<GrantedAuthority> getGrantedAuthorities()
    {
        Set<GrantedAuthority> authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.getPermission()))
                .collect(Collectors.toSet());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
