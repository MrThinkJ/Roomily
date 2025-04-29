package com.c2se.roomily.event.handler;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.event.pojo.ContractGenerationEvent;
import com.c2se.roomily.service.ContractGenerationService;
import com.c2se.roomily.service.RentedRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ContractGenerationEventHandler {
    private final ContractGenerationService contractGenerationService;
    private final RentedRoomService rentedRoomService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleContractGenerationEvent(ContractGenerationEvent event) {
        String rentedRoomId = event.getRentedRoomId();
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(rentedRoomId);
        contractGenerationService.generateRentContract(rentedRoom);
    }
}
