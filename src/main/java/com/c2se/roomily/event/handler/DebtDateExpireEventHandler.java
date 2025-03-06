package com.c2se.roomily.event.handler;

import com.c2se.roomily.event.DebtDateExpireEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DebtDateExpireEventHandler {
    @EventListener
    @Async
    public void handleDebtDateExpireEvent(DebtDateExpireEvent debtDateExpireEvent) {
        log.info("Handling debt date expire event");
        // TODO: Implement debt date expire event handling
    }
}
