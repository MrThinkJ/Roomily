package com.c2se.roomily.event.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdClickEvent {
    private String promotedRoomId;
    private LocalDateTime timestamp;
}
