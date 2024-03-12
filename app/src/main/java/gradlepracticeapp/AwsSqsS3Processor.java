package gradlepracticeapp;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
//AWS SDK for Java v2 modify
public class AwsSqsS3Processor {

    public static void main(String[] args) throws Exception {
        String queueUrl = "your-sqs-queue-url";
        SqsClient sqs = SqsClient.builder()
                                 .region(Region.of("your-region"))
                                 .credentialsProvider(DefaultCredentialsProvider.create())
                                 .build();

        S3Client s3 = S3Client.builder()
                              .region(Region.of("your-region"))
                              .credentialsProvider(DefaultCredentialsProvider.create())
                              .build();

        while (true) {
            List<Message> messages = sqs.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .build())
                    .messages();

            for (Message message : messages) {
            	try {
                    processMessage(message, s3);
                } catch (Exception e) {
                    e.printStackTrace(); // または適切なエラーロギング
                } finally {
                    // メッセージの処理が成功しようと失敗しようと、メッセージを削除
                    sqs.deleteMessage(builder -> 
                        builder.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
                }
            }

            Thread.sleep(1000); // Wait before polling again
        }
    }

    private static void processMessage(Message message, S3Client s3) throws Exception {
        // Parse the message to get S3 bucket and key
        String bucket = ""; // extract bucket from message
        String key = ""; // extract key from message

        try (ZipArchiveInputStream zipInput = new ZipArchiveInputStream(
                s3.getObject(GetObjectRequest.builder()
                                             .bucket(bucket)
                                             .key(key)
                                             .build()))) {
            
            // Iterate through the entries in the zip file
            while (zipInput.getNextZipEntry() != null) {
                Path outputFile = Files.createTempFile("unzipped-", ".tmp");
                try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile.toFile()))) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInput.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
                
                // Process the unzipped file
                processUnzippedFile(outputFile);
            }
        }
    }

    private static void processUnzippedFile(Path file) {
        // Implement your file processing logic here
        // For example, parsing the file, processing its contents, etc.
    }
}
