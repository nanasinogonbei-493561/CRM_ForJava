package com.example.crm.Config;

import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventListener.class);

    @EventListener
    public void handleFailure(AbstractAuthenticationFailureEvent event) {
        logger.warn("認証失敗イベント発生：{}", event.getAuthentication().getName());
    }

    @EventListener
    public void handleSuccess(AuthenticationSuccessEvent event) {
        logger.info("認証成功：{}", event.getAuthentication().getName());
    }
}
