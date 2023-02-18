package Bot;

import Data.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CommandManager extends ListenerAdapter { //TODO: Change from Guild to Global commands. (Can take up to an hour)

    public static HashMap<String, List<String>> keywordMap = new HashMap<>(); //ServerID, Keywords
    DatabaseManager db = new DatabaseManager();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        List<String> keywordList = new ArrayList<>();
        String command = event.getName();
        String keyword;
        String channel;
        String serverID = event.getGuild().getId();

        //Fills keywordList if it already has entries
        if (keywordMap.containsKey(serverID)){
            keywordList = keywordMap.get(serverID);
        }

        //addkeyword Command
        if (command.equals("addkeyword")) {
            OptionMapping messageOption = event.getOption("keyword");
            keyword = messageOption.getAsString().toUpperCase();
            if (keywordMap.containsKey(serverID)){
                if (keywordList.contains(keyword.toUpperCase(Locale.ROOT))){
                    event.reply(keyword + " is already active.").queue();
                    return;
                }
            }
            keywordList.add(keyword);
            keywordMap.put(serverID, keywordList);

            db.insertKeyword(serverID,keyword);

            event.reply(keyword + " was added to the active keywords.").queue();

        }

        //delkeyword Command
        if (command.equals("delkeyword")) {
            OptionMapping messageOption = event.getOption("keyword");
            keyword = messageOption.getAsString().toUpperCase();
            if (keywordList.contains(keyword)){
                for (int i = 0; i < keywordList.size(); i++){
                    if (Objects.equals(keywordList.get(i), keyword)){
                        keywordList.remove(i);
                    }
                }
                db.deleteKeyword(serverID,keyword);
                event.reply(keyword + " was deleted from the active keywords").queue();
            } else {
                event.reply(keyword + " is not an active keyword.").queue();
            }

        }

        //wipelist Command
        if (command.equals("wipelist")) {
            keywordList.clear();
            db.wipeKeywords(serverID);
            event.reply("Active keyword list has been wiped.").queue();
        }

        //viewlist Command
        if (command.equals("viewlist")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.ORANGE);
            embed.addField("Active Keywords: ", keywordList.toString().replace("[","").replace("]", ""), true);
            event.replyEmbeds(embed.build()).queue();
        }

        //setchannel Command
        if (command.equals("setchannel")) {
            channel = event.getChannel().getId();
            boolean dupe = db.dupeCheck(serverID);
            if (!dupe){
                db.setChannel(serverID, channel);
                event.reply("The channel '" + event.getChannel().getName() + "' was set.").queue();
            }else {
                event.reply("You already have a set channel! Please use /delchannel first.").queue();
            }
        }

        //delchannel Command
        if (command.equals("delchannel")) {
            boolean exists = db.dupeCheck(serverID);
            if (exists){
                db.delChannel(serverID);
                event.reply("The channel '" + event.getChannel().getName() + "' was removed.").queue();
            }else {
                event.reply("This server doesn't have a set channel! Please use /setchannel first.").queue();
            }
        }
    }

    //Fills commands into bot
    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();

        //AddKeyword
        OptionData option1 = new OptionData(OptionType.STRING, "keyword", "keyword to add", true);
        commandData.add(Commands.slash("addkeyword", "Adds a keyword ").addOptions(option1));

        //DelKeyword
        OptionData option2 = new OptionData(OptionType.STRING, "keyword", "keyword to delete", true);
        commandData.add(Commands.slash("delkeyword", "Deletes a keyword ").addOptions(option2));

        //WipeList
        commandData.add(Commands.slash("wipelist", "Deletes ALL keywords"));

        //ViewList
        commandData.add(Commands.slash("viewlist", "Shows all active keywords "));

        //SetChannel
        commandData.add(Commands.slash("setchannel", "Sets desired channel for posts"));

        //DelChannel
        commandData.add(Commands.slash("delchannel", "Removes desired channel for posts"));

        //Fill Commands
        event.getJDA().updateCommands().addCommands(commandData).queue();
    }
}
