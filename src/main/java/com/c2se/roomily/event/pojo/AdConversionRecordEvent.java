package com.c2se.roomily.event.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdConversionRecordEvent {
    private String adClickId;
}
