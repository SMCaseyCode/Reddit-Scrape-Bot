package Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class EventManager extends ListenerAdapter {

    public void newPostEvent(String post, String url, String thumbnail, String domain, String miniUrl, String author, String channelID){

            //Creats embed based on available info
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.CYAN);
            if (url == null){
                embed.setTitle(post);
            }else {
                embed.setTitle(post, url);
            }
            if (thumbnail != null && !thumbnail.equals("self")){
                embed.setImage(thumbnail);
            }
            if (author != null){
                embed.setAuthor(domain);
                embed.addField("Author: ", author, true);
            }
            if (miniUrl != null){
                embed.addField("Original Post:", "https://redd.it/" + miniUrl, true);
            }

            Objects.requireNonNull(bot.getTextChannelById(channelID)).sendMessageEmbeds(embed.build()).queue();

    }

    public static void getBot(JDA API){
        bot = API;
    }

    static private JDA bot;
}
