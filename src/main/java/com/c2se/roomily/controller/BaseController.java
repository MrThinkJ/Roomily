package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.UserInfoResponse;

public abstract class BaseController {
    protected UserInfoResponse getUserInfo() {
        return new UserInfoResponse();
    }
}
