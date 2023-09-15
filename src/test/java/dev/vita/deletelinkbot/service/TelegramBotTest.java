package dev.vita.deletelinkbot.service;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import org.apache.http.impl.io.IdentityOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TelegramBotTest {

    @Autowired
    private TelegramBot telegramBot;

    @Test
    void getBotUsername() {
    }

    @Test
    void getBotToken() {
    }

    @ParameterizedTest
    @CsvFileSource(resources="/strings_containig_valid_urls.csv")
    void when_hasUrlInText_shouldReturnTrue(String text) {
        UrlDetector parser = new UrlDetector(text, UrlDetectorOptions.Default);
        List<Url> found =  parser.detect();
        found.forEach(url -> System.out.println("--- --- --- --- found = " + url));
        assertTrue(!found.isEmpty());
    }

    @ParameterizedTest
    @CsvFileSource(resources="/strings _without_urls.csv")
    void when_hasUrlInText_shouldReturnFalse(String text) {
        UrlDetector parser = new UrlDetector(text, UrlDetectorOptions.Default);
        List<Url> found = parser.detect();
        found.forEach(url -> System.out.println("--- --- --- --- found = " + url));
        assertTrue(found.isEmpty());
    }

}