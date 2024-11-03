package com.webtech.rail.rail.userRepository;

import com.webtech.rail.rail.model.ResetToken;
import com.webtech.rail.rail.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    void deleteByToken(String token);
    Optional<ResetToken> findByToken(String token);
   Optional<ResetToken> findByUser(Optional<User> user);
    void deleteByUser(User user);  // To clean up old tokens
    Optional<ResetToken> findByUserEmail(String email);  // To find tokens by email
    boolean existsByTokenAndExpiryDateAfter(String token, Date date);  // To check token validity
    void deleteByTokenAndExpiryDateBefore(String token, Date date);
    Optional<ResetToken> findByTokenAndExpiryDateAfter(String token, Date date);

    @Modifying
    @Query("DELETE FROM ResetToken r WHERE r.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") Date now);

}