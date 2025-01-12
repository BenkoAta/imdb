package hu.benkoata.imdb.services;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@RequiredArgsConstructor
public class ResourceFileReaderService {
    private final Class<?> aClass;
    private final String fileName;

    public String getAsString() {
        try (InputStream is = aClass.getResourceAsStream(fileName);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             Scanner scanner = new Scanner(isr)) {
            scanner.useDelimiter("\\A");
            return scanner.next();
        } catch (IOException e) {
            throw new IllegalStateException("Can not read file!", e);
        }
    }
}
