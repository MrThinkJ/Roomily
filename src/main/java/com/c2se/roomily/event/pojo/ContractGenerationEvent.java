package com.c2se.roomily.event.pojo;

import lombok.Builder;
import org.springframework.context.ApplicationEvent;

public class ContractGenerationEvent extends ApplicationEvent {
    private final String rentedRoomId;

    @Builder
    public ContractGenerationEvent(Object source, String rentedRoomId) {
        super(source);
        this.rentedRoomId = rentedRoomId;
    }

    public static ContractGenerationEventBuilder builder(Object source) {
        return new ContractGenerationEventBuilder().source(source);
    }

    public String getRentedRoomId() {
        return rentedRoomId;
    }
}
