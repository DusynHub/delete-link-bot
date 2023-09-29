package dev.vita.deletelinkbot.service;


import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import dev.vita.deletelinkbot.config.DeleteLinkBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
  private final DeleteLinkBotConfig config;

    @Autowired
    public TelegramBot(DeleteLinkBotConfig config) {
        this.config = config;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Welcome message"));
        String commandsStr = commands.stream().map(curCom -> (curCom.getCommand() + " ")).toString();
        log.info("[DELETE LINK BOT]>>> Bot command: {}  were registered", commandsStr);
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("[DELETE LINK BOT]>>> Something went wrong while bot initialization");
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
        log.info("[DELETE LINK BOT]>>> Update received");
        if (update.hasMessage() || update.hasEditedMessage()) {
            Message msg = update.getMessage();
            Long chatId = msg.getChatId();
            Integer msgId = msg.getMessageId();
            Long userId = msg.getFrom().getId();
            Set<Long> admins = new HashSet<>(0);

            if(msg.isSuperGroupMessage() || msg.isGroupMessage() || msg.isChannelMessage()){
                admins = getChatAdministrators(chatId);
            }


            if (msg.hasText()) {
                String text = msg.getText();
                List<String> urls = getUrlsFromLinksInMessage(msg);

                if(text.startsWith("/start")){
                    if(msg.isUserMessage()){
                        handleStart(msg);
                    }
                    return;
                }


                if ((hasUrlInText(text) || !urls.isEmpty()) && !admins.contains(userId)) {
                    deleteMessage(chatId, msgId);
                    log.info("[DELETE LINK BOT]>>> Message was deleted with text = {}", text);
                }
                return;
            }

            if(msg.hasPhoto() || msg.hasDocument()){
                String caption = msg.getCaption();
                if ((hasUrlInText(caption)) && !admins.contains(userId)) {
                    deleteMessage(chatId, msgId);
                    log.info("[DELETE LINK BOT]>>> Message was deleted with caption = {}", caption);
                }
            }
        }
    }

    private List<String> getUrlsFromLinksInMessage(Message msg) {
        if( msg.getEntities() != null && !msg.getEntities().isEmpty()){
            return msg.getEntities()
                    .stream()
                    .map(MessageEntity::getUrl)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private void deleteMessage(Long chatId, Integer msgId) {
        DeleteMessage delete = new DeleteMessage();
        delete.setChatId(chatId);
        delete.setMessageId(msgId);
        try {
            execute(delete);
        } catch (Exception e) {
            log.info("[DELETE LINK BOT]>>> Error while deleting message");
            throw new RuntimeException(e);
        }
    }


    /**
     Проверяет, содержит ли заданный текст какие-либо URL-адреса.

     @param text текст для проверки
     @return true, если текст содержит хотя бы один URL-адрес, иначе false
     */
    public boolean hasUrlInText(String text) {
        UrlDetector parser = new UrlDetector(text, UrlDetectorOptions.Default);
        List<Url> found = parser.detect();
        return !found.isEmpty();

    }

    private Set<Long> getChatAdministrators(Long chatId) {
        List<ChatMember> chatAdministrators;
        try {
            chatAdministrators = execute(new GetChatAdministrators(String.valueOf(chatId)));
        } catch (TelegramApiException e) {
            log.info("[DELETE LINK BOT]>>> Error while getting chat admins");
            throw new RuntimeException(e);
        }
        return chatAdministrators.stream().map(member -> member.getUser().getId()).collect(Collectors.toSet());
    }

    private void handleStart(Message message){
        User currentUser = message.getFrom();
        String startAnswer =
                String.format(  "Добрый день, %s это Delete link bot \n" +
                                "Этот бот при добавлении в чат удаляет все" +
                                " сообщения, которые содержат ссылки и отправлены не администраторами. " +
                                "Администраторы могут отправлять сообщения, содержащие ссылки. " +
                                " \n" +
                                "Для распознавания ссылок в тексте  используется библиотека \"URL Detector\" от linkedin.com \n" +
                                " \n" +
                                "--------------------------------[ВАЖНОЕ УСЛОВИЕ]--------------------------------\n" +
                                "Для работы бота необходимо сделать его администратором и дать разрешение на удаление сообщений"
                        , currentUser.getFirstName());
        try {
            this.execute(getSendMessage(message.getChatId(), message.getMessageId(), startAnswer));
        } catch (TelegramApiException e) {
            log.info("[DELETE LINK BOT]>>> Failed to answer to '/start' command");
            throw new RuntimeException(e);
        }
        log.info("[DELETE LINK BOT]>>> Answered to '/start' command");
    }

    private SendMessage getSendMessage(long chatId, int msgId, String startAnswer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(msgId);
        sendMessage.setText(startAnswer);
        return sendMessage;
    }
}
