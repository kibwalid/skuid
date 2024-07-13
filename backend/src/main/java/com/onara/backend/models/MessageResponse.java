package com.onara.backend.models;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter
public class MessageResponse {
    private String message;
    private LocalDateTime date = LocalDateTime.now();

    public MessageResponse(String message) {
        this.message = message;
    }

}
