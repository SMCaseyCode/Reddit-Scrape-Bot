package Bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import static Bot.EventManager.getBot;
import static Bot.Reddit_Scraper.redditScrape;
import static Data.DatabaseManager.fillData;
import static Data.ProtectedData.TOKEN;

public class DiscordBot {
    public static void main(String[] args) {

        //Java Discord API call, Discord periodically updates what is required here.
        JDA bot = JDABuilder.createLight(TOKEN.getContent(), GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Scraping r/buildapcsales!"))
                .build();

        //EventManager.java
        registerEventListener(bot);
        //CommandManager.java
        registerCommandListener(bot);
        //Gets bot
        getBot(bot);
        //Method in DatabaseManager.java
        fillData();
        //Starts the reddit scraper
        redditScrape();
    }

    private static void registerCommandListener(JDA api) {
        api.addEventListener(new EventManager());
    }

    private static void registerEventListener(JDA api) {
        api.addEventListener(new CommandManager());
    }
}