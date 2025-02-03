package com.c2se.roomily.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
    Date timestamp;
    String message;
    String details;
}
