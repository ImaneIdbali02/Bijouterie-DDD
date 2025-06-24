package com.enaya.product_service.presentation.controller;

import com.enaya.product_service.application.dto.response.ImageResponse;
import com.enaya.product_service.application.service.ImageApplicationService;
import com.enaya.product_service.domain.model.collection.valueobjects.ImageCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageApplicationService imageApplicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("altText") String altText,
            @RequestParam("type") String type,
            @RequestParam(value = "description", required = false) String description) {
        
        ImageCollection imageCollection = imageApplicationService.uploadImage(file, altText, type, description);
        return ResponseEntity.ok(toImageResponse(imageCollection));
    }

    @PostMapping(value = "/with-dimensions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImageWithDimensions(
            @RequestParam("file") MultipartFile file,
            @RequestParam("altText") String altText,
            @RequestParam("width") int width,
            @RequestParam("height") int height) {
        
        ImageCollection imageCollection = imageApplicationService.uploadImageWithDimensions(file, altText, width, height);
        return ResponseEntity.ok(toImageResponse(imageCollection));
    }

    @PostMapping(value = "/with-details", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImageWithFullDetails(
            @RequestParam("file") MultipartFile file,
            @RequestParam("altText") String altText,
            @RequestParam("displayOrder") int displayOrder,
            @RequestParam("type") String type,
            @RequestParam("width") int width,
            @RequestParam("height") int height,
            @RequestParam(value = "description", required = false) String description) {
        
        ImageCollection imageCollection = imageApplicationService.uploadImageWithFullDetails(
                file, altText, displayOrder, type, width, height, description);
        return ResponseEntity.ok(toImageResponse(imageCollection));
    }

    @DeleteMapping("/{imageUrl}")
    public ResponseEntity<Void> deleteImage(@PathVariable String imageUrl) {
        ImageCollection imageCollection = ImageCollection.of(imageUrl, "", 0);
        imageApplicationService.deleteImage(imageCollection);
        return ResponseEntity.noContent().build();
    }

    private ImageResponse toImageResponse(ImageCollection imageCollection) {
        return ImageResponse.builder()
                .url(imageCollection.getUrl())
                .altText(imageCollection.getAltText())
                .type(imageCollection.getType())
                .displayOrder(imageCollection.getDisplayOrder())
                .width(imageCollection.getWidth())
                .height(imageCollection.getHeight())
                .fileSize(imageCollection.getFormattedFileSize())
                .aspectRatio(imageCollection.getAspectRatio())
                .isSquare(imageCollection.isSquare())
                .isLandscape(imageCollection.isLandscape())
                .isPortrait(imageCollection.isPortrait())
                .isWidescreen(imageCollection.isWidescreen())
                .build();
    }
}
