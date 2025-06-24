package com.enaya.product_service.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageResponse {
    private String url;
    private String altText;
    private String type;
    private int displayOrder;
    private int width;
    private int height;
    private String fileSize;
    private double aspectRatio;
    private boolean isSquare;
    private boolean isLandscape;
    private boolean isPortrait;
    private boolean isWidescreen;
} 