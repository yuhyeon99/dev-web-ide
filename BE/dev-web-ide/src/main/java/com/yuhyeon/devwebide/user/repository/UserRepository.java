package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatusAndIdNotAndNicknameContainingIgnoreCaseOrStatusAndIdNotAndEmailContainingIgnoreCase(
            UserStatus nicknameStatus,
            Long nicknameExcludedUserId,
            String nicknameQuery,
            UserStatus emailStatus,
            Long emailExcludedUserId,
            String emailQuery,
            Pageable pageable
    );
}
