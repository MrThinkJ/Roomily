package com.c2se.roomily.payload.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UpdateBillLogRequest {
    private Double electricity;
    private Double water;
    private MultipartFile waterImage;
    private MultipartFile electricityImage;
}
