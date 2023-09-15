package dev.vita.deletelinkbot.service;


import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import dev.vita.deletelinkbot.command.StartCommand;
import dev.vita.deletelinkbot.config.DeleteLinkBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
//    private static final String urlPattern
//            = "/(https:\\/\\/www\\.|http:\\/\\/www\\.|https:\\/\\/|" +
//            "http:\\/\\/)?[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})(\\.[a-zA-Z]{2,})?\\/[a-zA-Z0-9]{2,}|((https:\\/\\/www\\.|" +
//            "http:\\/\\/www\\.|https:\\/\\/|http:\\/\\/)?[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})(\\.[a-zA-Z]{2,})?)|(" +
//            "https:\\/\\/www\\.|http:\\/\\/www\\.|" +
//            "https:\\/\\/|http:\\/\\/)?[a-zA-Z0-9]{2,}\\.[a-zA-Z0-9]{2,}\\.[a-zA-Z0-9]{2,}(\\.[a-zA-Z0-9]{2,})?/g";

    private final DeleteLinkBotConfig config;

    @Autowired
    public TelegramBot(DeleteLinkBotConfig config) {
        this.config = config;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Welcome message"));
        String commandsStr = commands.stream().map(curCom -> (curCom.getCommand() + "")).toString();
        log.info("[BOT]>>> Bot command: {}  were registered", commandsStr);
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("[BOT]>>> Something went wrong while bot initialization");
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("[BOT]>>> Update received");
        if (update.hasMessage() || update.hasEditedMessage()) {
            Message msg = update.getMessage();
            if (msg.hasText()) {
                String text = msg.getText();
                if(text.startsWith("/start")){

                }


                Long chatId = msg.getChatId();
                Integer msgId = msg.getMessageId();
                Long userId = msg.getFrom().getId();
                List<String> urls = msg.getEntities()
                        .stream()
                        .map(MessageEntity::getUrl)
                        .collect(Collectors.toUnmodifiableList());
                Set<Long> admins = getChatAdministrators(chatId);
                if ((hasUrlInText(text) || !urls.isEmpty()) && !admins.contains(userId)) {
                    deleteMessage(chatId, msgId);
                    log.info("[BOT]>>> Message was deleted with text = {}", text);
                }
            }
        }
    }

    private void deleteMessage(Long chatId, Integer msgId) {
        DeleteMessage delete = new DeleteMessage();
        delete.setChatId(chatId);
        delete.setMessageId(msgId);
        try {
            execute(delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasUrlInText(String text) {
//        Pattern pattern = Pattern.compile(urlPattern);
//        Matcher matcher = pattern.matcher(text);
//        return matcher.find();
        UrlDetector parser = new UrlDetector(text, UrlDetectorOptions.Default);
        List<Url> found = parser.detect();
        return !found.isEmpty();

    }

    public Set<Long> getChatAdministrators(Long chatId) {
        List<ChatMember> chatAdministrators = Collections.emptyList();
        try {
            chatAdministrators = execute(new GetChatAdministrators(String.valueOf(chatId)));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return chatAdministrators.stream().map(member -> member.getUser().getId()).collect(Collectors.toUnmodifiableSet());
    }
}
