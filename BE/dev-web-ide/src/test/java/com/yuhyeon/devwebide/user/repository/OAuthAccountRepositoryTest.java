package com.yuhyeon.devwebide.user.repository;

import com.yuhyeon.devwebide.user.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class OAuthAccountRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Test
    @DisplayName("OAuth 계정 저장")
    void saveOAuthAccount() {
        User user = User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        OAuthAccount oauthAccount = OAuthAccount.builder()
                .user(savedUser)
                .provider(OAuthProvider.GITHUB)
                .providerUserId("github-12345")
                .providerEmail("test@test.com")
                .build();

        OAuthAccount savedOAuthAccount = oauthAccountRepository.save(oauthAccount);

        assertThat(savedOAuthAccount.getId()).isNotNull();
        assertThat(savedOAuthAccount.getProvider())
                .isEqualTo(OAuthProvider.GITHUB);
        assertThat(savedOAuthAccount.getProviderUserId())
                .isEqualTo("github-12345");
    }

    @Test
    @DisplayName("provider와 providerUserId로 OAuth 계정 조회")
    void findByProviderAndProviderUserId() {
        User user = User.builder()
                .email("github@test.com")
                .nickname("깃허브유저")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        OAuthAccount oauthAccount = OAuthAccount.builder()
                .user(savedUser)
                .provider(OAuthProvider.GITHUB)
                .providerUserId("github-99999")
                .providerEmail("github@test.com")
                .build();

        oauthAccountRepository.save(oauthAccount);

        Optional<OAuthAccount> result =
                oauthAccountRepository.findByProviderAndProviderUserId(
                        OAuthProvider.GITHUB,
                        "github-99999"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getProvider())
                .isEqualTo(OAuthProvider.GITHUB);
        assertThat(result.get().getProviderUserId())
                .isEqualTo("github-99999");
        assertThat(result.get().getUser().getEmail())
                .isEqualTo("github@test.com");
    }
}
