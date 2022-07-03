package Events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class Events extends ListenerAdapter {

    public void newPostEvent(String post, String url, String thumbnail, String domain, String miniUrl, String author){

        if (!(Objects.equals(thumbnail, "default"))) { //Embedded Post if a thumbnail is detected
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.CYAN);
            embed.setTitle(post, url);
            embed.setImage(thumbnail);
            embed.setAuthor(domain);
            embed.addField("Author: ", author + "                 ", true);
            embed.addField("Original Post:", "https://redd.it/" + miniUrl, true);

            Objects.requireNonNull(bot.getTextChannelById("Discord Text Channel ID")).sendMessageEmbeds(embed.build()).queue();
        }
        else {
            EmbedBuilder et = new EmbedBuilder(); //Embedded post if a thumbnail is NOT detected
            et.setColor(Color.CYAN);
            et.setTitle(post, url);
            et.setAuthor(domain);
            et.addField("Author: ", author + "                 ", true);
            et.addField("Original Post:", "https://redd.it/" + miniUrl, true);

            Objects.requireNonNull(bot.getTextChannelById("Discord Text Channel ID")).sendMessageEmbeds(et.build()).queue();
        }

    }

    public static void getBot(JDA API){
        bot = API;
    }

    static private JDA bot;

}
