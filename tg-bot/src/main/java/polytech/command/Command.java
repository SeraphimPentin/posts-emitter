package polytech.command.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import polytech.keyboards.InlineKeyboard;
import polytech.keyboards.ReplyKeyboard;

@Component
public abstract class Command implements IBotCommand {

    @Autowired
    private InlineKeyboard inlineKeyboard;

    @Autowired
    private ReplyKeyboard replyKeyboard;


    protected abstract void doExecute(AbsSender absSender, User user, Chat chat);
}
