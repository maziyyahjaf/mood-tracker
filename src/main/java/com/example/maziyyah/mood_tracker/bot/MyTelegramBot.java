// package com.example.maziyyah.mood_tracker.bot;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import org.springframework.stereotype.Component;
// import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
// import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
// import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
// import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
// import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
// import org.telegram.telegrambots.meta.api.objects.Update;
// import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
// import org.telegram.telegrambots.meta.generics.TelegramClient;

// @Component
// public class MyTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

//     private final TelegramClient telegramClient;

//     public MyTelegramBot() {
//         telegramClient = new OkHttpTelegramClient(getBotToken());

//     }

//     // Inside your class
//     private static final Logger logger = LoggerFactory.getLogger(MyTelegramBot.class);

//     @Override
//     public String getBotToken() {
//         return "7606199243:AAHe-N0jVunz3wv7pmZaAK-07KXVUwOMEo8";
//     }

//     @Override
//     public LongPollingUpdateConsumer getUpdatesConsumer() {
//         return this;
//     }

//     @Override
//     public void consume(Update update) {

//         // logger.info("Received update: {}", update);

//         // to do
//         // We check if the update has a message and the message has text
//         if (update.hasMessage() && update.getMessage().hasText()) {

//             String message_text = update.getMessage().getText();
//             String chatId = update.getMessage().getChatId().toString();
            

//             logger.info("Chat ID: {}, Message Text: {}", chatId, message_text);

          

//             SendMessage message = SendMessage
//                     .builder()
//                     .chatId(chatId)
//                     .text("You said: " + message_text)
//                     .build();

//             try {
//                 telegramClient.execute(message);
//             } catch (TelegramApiException e) {
//                 logger.error("Failed to send message", e);
//             }

//         }
//     }

// }
