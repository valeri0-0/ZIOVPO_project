package com.valeria.repository;

import com.valeria.entity.UserSession;
import com.valeria.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID>
{
    Optional<UserSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);

    @EntityGraph(attributePaths = {"user"})
    Optional<UserSession> findByRefreshToken(String refreshToken);
}
