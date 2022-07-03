package Events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class Commands extends ListenerAdapter {

    private static int size = 1; //global size int for dataBase
    private static int currentSize = 0; //global size to protect already entered data.
    public static String[] activeList = new String[20]; //needs better solution. Wasteful. Has limited capacity.

    public void onMessageReceived(MessageReceivedEvent event) { //AddKeyword + DelKeyword + CurrentList Commands

        String[] keyword = event.getMessage().getContentRaw().split(" ");

        if (keyword.length == 1 && (keyword[0].equalsIgnoreCase("!AddKeyword") || keyword[0].equalsIgnoreCase("!DelKeyword"))) {

            event.getChannel().sendMessage("To use this command, type ![add][del]Keyword followed by the desired keyword.").queue();

        } else if (keyword[0].equalsIgnoreCase("!AddKeyword")) {

            for (int i = 0; i < size; i++)
            {

                if (Objects.equals(activeList[i], keyword[1])) //checks for already in use keywords
                {
                    event.getChannel().sendMessage("This is already in the active keywords list.").queue();
                    return;
                }

            }

            addKeyword(keyword[1]);

            event.getChannel().sendMessage("The Keyword '" + keyword[1] + ("' has been added")).queue();

        } else if (keyword[0].equalsIgnoreCase("!DelKeyword")) {
            boolean isThere = false;

            for (int i = 0; i < size - 1; i++)
            {
                String temp = activeList[i];

                if (temp.equalsIgnoreCase(keyword[1])) //checks for already in use keywords
                {
                    delKeyword(keyword[1]);
                    isThere = true;

                    event.getChannel().sendMessage("The Keyword '" + keyword[1] + ("' has been deleted")).queue();

                }
            }

            if (!isThere){
                event.getChannel().sendMessage("The Keyword '" + keyword[1] + ("' is not in the active list")).queue();
            }


        }

        if (keyword[0].equalsIgnoreCase("!CurrentList")) { // Builds an embedded message.
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.ORANGE);
            embed.addField("These are your active keywords: " , displayActive() , true);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();

        }
    }


    public void addKeyword(String keyword){ //adds keywords to the "database"

        String[] arrayOfKeywords = new String[size];

        arrayOfKeywords[currentSize] = keyword;
        activeList[currentSize] = arrayOfKeywords[currentSize];

        size++ ;
        currentSize++;

    }

    public String displayActive(){

        String[] tempArray = new String[size - 1];

        System.arraycopy(activeList, 0, tempArray, 0, currentSize ); // copies tempArray to activeList

        return Arrays.toString(tempArray);
    }

    public void delKeyword(String keyword){ // deletes keywords out of the database
        int deletedIndex = 50; // Higher number than activeList size.

        for (int i = 0; i < size - 1; i++)
        {
            if (activeList[i].equalsIgnoreCase(keyword)) // Deletes the keyword in the list.
            {
                activeList[i] = null;
                deletedIndex = i; // saves the index of deletion
                break;
            }
        }

        if (deletedIndex != 50){
            for (int i = deletedIndex; i < size - 1; i++){ // TLDR if attempted replace index is null, make tail index null and reduce array size.
                if (activeList[i+1] == null){
                    activeList[i] = null;
                    currentSize--;
                    size--;
                    break;
                }
                else if (activeList[i+1] != null){
                    activeList[i] = activeList[i+1];
                }
            }
        }

    }

}

    /*
    ---------------------NOTES/TO-DO-------------------------------------
    -Currently, the bot loses ALL keywords when it shuts down. This needs to be fixed, perhaps a file database?
    -Along with file database, needs to separate for servers
    -Multi-server compatibility
     */
