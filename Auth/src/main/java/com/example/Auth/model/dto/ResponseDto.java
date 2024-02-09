package com.example.Auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import netscape.javascript.JSObject;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    String status;
    String error;
    JSObject data;
    int statusCode;
}
