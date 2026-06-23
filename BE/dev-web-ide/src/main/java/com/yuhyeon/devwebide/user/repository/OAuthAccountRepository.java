package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.OAuthAccount;
import com.yuhyeon.devwebide.user.domain.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderUserId(
            OAuthProvider provider,
            String providerUserId
    );
}
