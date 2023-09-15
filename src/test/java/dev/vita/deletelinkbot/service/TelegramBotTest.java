package dev.vita.deletelinkbot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


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
        assertTrue(telegramBot.hasUrlInText(text));
    }

    @ParameterizedTest
    @CsvFileSource(resources="/strings _without_urls.csv")
    void when_hasUrlInText_shouldReturnFalse(String text) {
        assertFalse(telegramBot.hasUrlInText(text));
    }

}