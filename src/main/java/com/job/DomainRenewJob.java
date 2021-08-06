package com.job;

import com.bot.Bot;
import com.entity.User;
import com.model.Article;
import com.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DomainRenewJob {
    private final UserRepository userRepository;
    private final Bot bot;

    @Autowired
    public DomainRenewJob(UserRepository userRepository, Bot bot) {
        this.userRepository = userRepository;
        this.bot = bot;
    }

    /**
     * Метод парсит статьи с сайта habr.com
     */
    private List<Article> parseArticles() {
        List<Article> list = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://habr.com/ru/news/").get();
            Elements listNews = doc.select(".tm-article-snippet__title_h2");
            listNews.stream().limit(10).forEach(news -> {
                String url = "https://habr.com" + news.select("h2 a").attr("href");
                String title = news.select("h2 a span").text();
                list.add(new Article(url, title));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Метод совершает рассылку новых статей каждый день 10 часов по мск
     */
    @Scheduled(cron = "0 0 11 ? * *")
    public void sendMessageToAllUsers() {
        List<User> users = userRepository.findAll();
        String message = parseArticles().stream().map(article ->
                article.getTitle() + "." + "\n" + "Подробнее:  " + article.getUrl() + "\n")
                .collect(Collectors.joining("\n"));
        users.forEach(user -> {
            try {
                bot.execute(new SendMessage(String.valueOf(user.getUserTgId()), message));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }
}
