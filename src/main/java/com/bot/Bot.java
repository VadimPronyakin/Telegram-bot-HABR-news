package com.bot;

import com.entity.User;
import com.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private final UserRepository userRepository;

    @Autowired
    public Bot(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Метод отправляет приветственное сообщение пользователю
     */
    @Override
    public void onUpdateReceived(Update update) {
        try {
            String message = update.getMessage().getFrom().getFirstName() + ", привет! " +
                    "Данный бот ежедневно в 10 утра по МСК совершает рассылку 10-ти новых статей с сайта habr.com." +
                    " Теперь ты подписан на рассылку.";
            writeUserInfoToDb(update);
            execute(new SendMessage(String.valueOf(update.getMessage().getChatId()), message));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }

    /**
     * Метод добавляет пользователя в бд
     */
    private void writeUserInfoToDb(Update update) {
        String userName = update.getMessage().getFrom().getUserName();
        User user;
        if (userRepository.existsByUserName(userName)) {
            user = userRepository.findByUserName(userName);
        } else {
            user = new User();
            user.setUserName(userName);
            user.setUserTgId(update.getMessage().getFrom().getId());
        }
        userRepository.save(user);
    }
}
