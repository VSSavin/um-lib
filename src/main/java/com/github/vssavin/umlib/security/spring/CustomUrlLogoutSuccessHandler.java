package com.github.vssavin.umlib.security.spring;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * An {@link org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler} implementation
 * that generates a url to redirect after the user logs out.
 *
 * @author vssavin on 15.08.2022.
 */
@Component
class CustomUrlLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler
        implements LogoutSuccessHandler {

    private final CustomRedirectStrategy customRedirectStrategy = new CustomRedirectStrategy();

    CustomUrlLogoutSuccessHandler() {
        super.setRedirectStrategy(customRedirectStrategy);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        customRedirectStrategy.setParameterMap(request.getParameterMap());
        super.handle(request, response, authentication);
    }

    private static class CustomRedirectStrategy extends DefaultRedirectStrategy {
        private Map<String, String[]> parameterMap = Collections.emptyMap();

        @Override
        protected String calculateRedirectUrl(String contextPath, String url) {
            String redirectUrl = super.calculateRedirectUrl(contextPath, url);
            UriBuilder uriBuilder = UriComponentsBuilder.newInstance();
            uriBuilder.path(redirectUrl);
            uriBuilder.queryParams(toMultiValueMapWithoutCsrf(parameterMap));
            return uriBuilder.build().toASCIIString();
        }

        private MultiValueMap<String, String> toMultiValueMapWithoutCsrf(Map<String, String[]> parameterMap) {
            MultiValueMap<String, String> result = new LinkedMultiValueMap<>(parameterMap.size());
            parameterMap.forEach((key, values) -> {
                for (String value : values) {
                    if (!key.equals("_csrf")) {
                        result.add(key, value);
                    }
                }
            });

            return result;
        }

        private void setParameterMap(Map<String, String[]> parameterMap) {
            this.parameterMap = parameterMap;
        }
    }

}

