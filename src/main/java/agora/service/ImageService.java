package agora.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    void initBucket();

    String uploadImage(MultipartFile file);

}
