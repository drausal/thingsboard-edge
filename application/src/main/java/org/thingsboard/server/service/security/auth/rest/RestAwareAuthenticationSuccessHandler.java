/**
 * Copyright © 2016-2022 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.service.security.auth.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.service.security.auth.MfaAuthenticationToken;
import org.thingsboard.server.service.security.auth.jwt.RefreshTokenRepository;
import org.thingsboard.server.service.security.auth.mfa.config.TwoFaConfigManager;
import org.thingsboard.server.service.security.model.JwtTokenPair;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.model.token.JwtTokenFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component(value = "defaultAuthenticationSuccessHandler")
@RequiredArgsConstructor
public class RestAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper mapper;
    private final JwtTokenFactory tokenFactory;
    private final TwoFaConfigManager twoFaConfigManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        JwtTokenPair tokenPair = new JwtTokenPair();

        if (authentication instanceof MfaAuthenticationToken) {
            int preVerificationTokenLifetime = twoFaConfigManager.getPlatformTwoFaSettings(securityUser.getTenantId(), true)
                    .flatMap(settings -> Optional.ofNullable(settings.getTotalAllowedTimeForVerification())
                            .filter(time -> time > 0))
                    .orElse((int) TimeUnit.MINUTES.toSeconds(30));
            tokenPair.setToken(tokenFactory.createPreVerificationToken(securityUser, preVerificationTokenLifetime).getToken());
            tokenPair.setRefreshToken(null);
            tokenPair.setScope(Authority.PRE_VERIFICATION_TOKEN);
        } else {
            tokenPair.setToken(tokenFactory.createAccessJwtToken(securityUser).getToken());
            tokenPair.setRefreshToken(refreshTokenRepository.requestRefreshToken(securityUser).getToken());
        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), tokenPair);

        clearAuthenticationAttributes(request);
    }

    /**
     * Removes temporary authentication-related data which may have been stored
     * in the session during the authentication process..
     *
     */
    protected final void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
