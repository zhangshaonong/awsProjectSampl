package gradlepracticeapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
//AWS SDK for Java v1
public class SqsS3Processor {

    public static void main(String[] args) {
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        final String queueUrl = "your-sqs-queue-url";
        final String bucketName = "your-s3-bucket-name";

        try {
            List<Message> messages = sqs.receiveMessage(new ReceiveMessageRequest(queueUrl)
                    .withMaxNumberOfMessages(10)).getMessages();
            for (Message message : messages) {
                String s3Path = message.getBody();

                S3Object s3Object = s3.getObject(bucketName, s3Path);
                ZipInputStream zis = new ZipInputStream(s3Object.getObjectContent());

                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    String fileName = zipEntry.getName();
                    File newFile = new File("local-directory/" + fileName);

                    new File(newFile.getParent()).mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    IOUtils.copy(zis, fos);
                    fos.close();
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
                zis.close();

                sqs.deleteMessage(queueUrl, message.getReceiptHandle());
            }
        } catch (AmazonServiceException | IOException e) {
            e.printStackTrace();
        }
    }
}
