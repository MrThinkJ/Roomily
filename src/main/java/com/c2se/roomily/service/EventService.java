package com.c2se.roomily.service;

import com.c2se.roomily.event.pojo.AppEvent;

public interface EventService {
    void publishEvent(AppEvent event);
}
