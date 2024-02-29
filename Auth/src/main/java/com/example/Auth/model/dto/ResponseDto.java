package com.example.Auth.model.dto;

import com.example.Auth.model.Status;
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
    Status status;
    String error;
    Object data;
    String message;
}
