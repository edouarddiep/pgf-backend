package com.pgf.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Le site est prérendu statiquement : le HTML servi aux visiteurs et aux moteurs
 * de recherche reste celui du dernier déploiement tant qu'aucun build n'a lieu.
 * Toute écriture de contenu planifie donc un rebuild Vercel.
 */
@Service
@Slf4j
public class DeployHookService {

    private final RestTemplate restTemplate;
    private final String hookUrl;
    private final Duration debounce;

    // Une publication enchaîne plusieurs écritures (exposition, fichiers, ordre) :
    // un seul build est déclenché pour la rafale, une fois celle-ci terminée.
    private final AtomicBoolean scheduled = new AtomicBoolean();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "deploy-hook");
        thread.setDaemon(true);
        return thread;
    });

    public DeployHookService(RestTemplate restTemplate,
                             @Value("${app.deploy.hook-url:}") String hookUrl,
                             @Value("${app.deploy.debounce:2m}") Duration debounce) {
        this.restTemplate = restTemplate;
        this.hookUrl = hookUrl;
        this.debounce = debounce;
    }

    public void scheduleRebuild() {
        if (!StringUtils.hasText(hookUrl)) {
            return;
        }
        if (scheduled.compareAndSet(false, true)) {
            scheduler.schedule(this::triggerRebuild, debounce.toSeconds(), TimeUnit.SECONDS);
            log.info("Rebuild du site planifié dans {}s", debounce.toSeconds());
        }
    }

    private void triggerRebuild() {
        // Libéré avant l'appel : une écriture survenue pendant celui-ci doit
        // pouvoir planifier le build suivant.
        scheduled.set(false);
        try {
            restTemplate.postForEntity(hookUrl, null, Void.class);
            log.info("Deploy hook Vercel déclenché");
        } catch (Exception e) {
            log.error("Deploy hook Vercel en échec", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
