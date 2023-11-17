package polis.bot;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import polis.commands.AccountsList;
import polis.commands.AddGroup;
import polis.commands.AddOkAccount;
import polis.commands.AddOkGroup;
import polis.commands.AddTgChannel;
import polis.commands.AddVkAccount;
import polis.commands.AddVkGroup;
import polis.commands.Autoposting;
import polis.commands.GroupDescription;
import polis.commands.Help;
import polis.commands.MainMenu;
import polis.commands.NonCommand;
import polis.commands.Notifications;
import polis.commands.OkAccountDescription;
import polis.commands.StartCommand;
import polis.commands.SyncGroupDescription;
import polis.commands.SyncOkTg;
import polis.commands.SyncVkTg;
import polis.commands.TgChannelDescription;
import polis.commands.TgChannelsList;
import polis.commands.TgSyncGroups;
import polis.commands.VkAccountDescription;
import polis.data.domain.Account;
import polis.data.domain.ChannelGroup;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentChannel;
import polis.data.domain.CurrentGroup;
import polis.data.domain.CurrentState;
import polis.data.domain.UserChannels;
import polis.data.repositories.AccountsRepository;
import polis.data.repositories.ChannelGroupsRepository;
import polis.data.repositories.CurrentAccountRepository;
import polis.data.repositories.CurrentChannelRepository;
import polis.data.repositories.CurrentGroupRepository;
import polis.data.repositories.CurrentStateRepository;
import polis.data.repositories.UserChannelsRepository;
import polis.keyboards.ReplyKeyboard;
import polis.posting.IPostsProcessor;
import polis.util.IState;
import polis.util.SocialMedia;
import polis.util.State;
import polis.util.Substate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static polis.commands.NonCommand.VK_GROUP_ADDED;
import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_ANSWER;
import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
import static polis.datacheck.OkDataCheck.OK_AUTH_STATE_WRONG_AUTH_CODE_ANSWER;
import static polis.datacheck.OkDataCheck.OK_GROUP_ADDED;
import static polis.datacheck.OkDataCheck.SAME_OK_ACCOUNT;
import static polis.datacheck.OkDataCheck.USER_HAS_NO_RIGHTS;
import static polis.datacheck.OkDataCheck.WRONG_LINK_OR_USER_HAS_NO_RIGHTS;
import static polis.datacheck.VkDataCheck.SAME_VK_ACCOUNT;
import static polis.datacheck.VkDataCheck.VK_AUTH_STATE_ANSWER;
import static polis.datacheck.VkDataCheck.VK_AUTH_STATE_SERVER_EXCEPTION_ANSWER;
import static polis.keyboards.Keyboard.GO_BACK_BUTTON_TEXT;
import static polis.keyboards.Keyboard.GO_BACK_CALLBACK_DATA;
import static polis.telegram.TelegramDataCheck.BOT_NOT_ADMIN;
import static polis.telegram.TelegramDataCheck.RIGHT_LINK;
import static polis.telegram.TelegramDataCheck.WRONG_LINK_OR_BOT_NOT_ADMIN;

@Configuration
@Component("Bot")

public class Bot extends TelegramLongPollingCommandBot implements TgFileLoader, TgNotificator {
    @Override
    public String getBotUsername() {
        return null;
    }

    @Override
    public void processNonCommandUpdate(Update update) {

    }

    @Override
    public void sendNotification(long userChatId, String message) {

    }
}