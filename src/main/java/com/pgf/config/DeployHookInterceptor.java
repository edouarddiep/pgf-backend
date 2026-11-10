package com.pgf.config;

import com.pgf.service.DeployHookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Déclenché après exécution du handler, donc après le commit de la transaction :
 * une écriture annulée ne provoque jamais de build.
 */
@Component
@RequiredArgsConstructor
public class DeployHookInterceptor implements HandlerInterceptor {

    private final DeployHookService deployHookService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex != null || HttpMethod.GET.matches(request.getMethod())) {
            return;
        }
        if (HttpStatus.Series.resolve(response.getStatus()) == HttpStatus.Series.SUCCESSFUL) {
            deployHookService.scheduleRebuild();
        }
    }
}
