package me.SMCaseyCode;

import Events.Commands;
import Events.Events;
import masecla.reddit4j.exceptions.AuthenticationException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.IOException;

import static Events.Events.getBot;
import static Events.Scrape.run;
import static Events.Token_Grabber.getClient;

public class DiscordBot {

    public static void main(String[] args) throws LoginException, AuthenticationException, IOException, InterruptedException {  //Bot build + registers

        JDA bot = JDABuilder.createDefault("Discord Bot Token")
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.playing("Scraping r/buildapcsales!"))
                .build();

        registerEventListener(bot);
        registerCommandListener(bot);

        getBot(bot);
        getClient();
        run(); //Runs Scraper


    }
    public static void registerEventListener(JDA api){

        api.addEventListener(new Events());

    }

    public static void registerCommandListener(JDA api){

        api.addEventListener(new Commands());

    }

}
