package com.c2se.roomily.payload.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {
  private String fullName;
  private String email;
  private String phone;
  private String address;
  private MultipartFile profilePicture;
}
