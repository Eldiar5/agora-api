package agora.controller;

import agora.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
@Tag(name = "Image API", description = "Загрузка изображений")
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить изображение", description = "Возвращает URL загруженного файла")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        String url = imageService.uploadImage(file);
        return Map.of("url", url);
    }

}
