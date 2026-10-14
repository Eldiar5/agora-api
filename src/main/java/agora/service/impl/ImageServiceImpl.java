package agora.service.impl;

import agora.service.ImageService;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    @PostConstruct
    @SneakyThrows
    public void initBucket() {
        // 1. Проверяем существование бакета
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        if (!found) {
            // 2. Создаем
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            System.out.println("✅ [MinIO] Бакет '" + bucketName + "' успешно создан.");

            // 3. Делаем публичным (Policy)
            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": {"AWS": ["*"]},
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """.formatted(bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("\uD83D\uDD13 [MinIO] Бакет '{}' теперь публичный (readonly).", bucketName);
        } else {
            log.info("ℹ️ [MinIO] Бакет '{}' уже существует.", bucketName);
        }
    }

    @Override
    @SneakyThrows
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new RuntimeException("Image must have a name.");
        }

        // 1. Генерируем уникальное имя файла (uuid + расширение)
        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;

        // 2. Загружаем в MinIO
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        // 3. Возвращаем публичную ссылку
        // Внимание: если ты запускаешь это локально, MinIO вернет localhost.
        // Для продакшена тут может понадобиться настройка внешнего домена.
        // Но для Docker-сети мы соберем URL вручную, чтобы он был доступен фронтенду.

        // Фронтенд обращается к MinIO через порт 9005 (внешний), а не 9000.
        // Поэтому хардкодим внешний URL для возврата
        return "http://localhost:9005/" + bucketName + "/" + fileName;
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

}
