package hu.benkoata.imdb.services;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@RequiredArgsConstructor
public class ConfigFile {
    private final Class<?> aClass;
    private final String fileName;
    private static final Charset charset = StandardCharsets.UTF_8;

    public Properties read() {
        if (aClass != null) {
            return readFromResource();
        }
        return readFromFile();
    }

    private Properties readFromResource() {
        Properties props = new Properties();
        try (InputStream is = aClass.getResourceAsStream(fileName)) {
            try (InputStreamReader isr = new InputStreamReader(is, charset)) {
                props.load(isr);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(String.format("Can not read file (%s)!", fileName), exception);
        }
        return props;
    }

    private Properties readFromFile() {
        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), charset)) {
            props.load(reader);
        } catch (IOException exception) {
            throw new IllegalStateException(String.format("Can not read file (%s)!", fileName), exception);
        }
        return props;
    }
}
