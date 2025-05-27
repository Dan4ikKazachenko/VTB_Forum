package org.example.vtb.dto;


import lombok.Data;

import java.util.UUID;

@Data
public class ProductRequest {
    private String title;
    private String description;
    private UUID categoryId;
}