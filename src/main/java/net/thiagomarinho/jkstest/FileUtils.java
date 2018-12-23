package net.thiagomarinho.jkstest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public void writeFile(String filename, String content) {
        try {
            Files.write(Paths.get(filename), content.getBytes());
        } catch (IOException e) {
            logger.error("Error while trying to write message to file", e);
            throw new ApplicationException(e);
        }               
    }

    public String readFile(String filename) {
        StringBuilder contentBuilder = new StringBuilder();
        
        try (Stream<String> stream = Files.lines(Paths.get(filename), UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
            return contentBuilder.toString().trim();
        } catch (IOException e) {
            logger.error("Error while trying to read file " + filename, e);
            throw new ApplicationException(e);
        }
    }
}
