package com.onara.backend.models;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppResponse {
    private Object data;
    private boolean success;
}
