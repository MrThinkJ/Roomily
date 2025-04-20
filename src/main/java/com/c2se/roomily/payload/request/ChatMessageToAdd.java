package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class ChatMessageToAdd {
    private String content;
    private String senderId;
    private String chatRoomId;
    private MultipartFile image;
    private Boolean isAdConversion;
    private String adClickId;
}
