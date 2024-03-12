package gradlepracticeapp;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CsvFileProcessor {
	public static void createDevideFile(String filePath, String outPath) {
        String inputCsvFile = filePath; // 読み込むCSVファイル
        Map<String, BufferedWriter> fileWriters = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(inputCsvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // CSVの行をカンマで分割
                if (values.length == 0) continue;

                String fileName = outPath + values[0] + ".txt"; // ファイル名（例：最初の列の値.txt）
                BufferedWriter writer = fileWriters.get(fileName);
                
                if (writer == null) {
                    writer = new BufferedWriter(new FileWriter(fileName));
                    fileWriters.put(fileName, writer);
                }

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // すべてのBufferedWriterを閉じる
            fileWriters.values().forEach(writer -> {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
