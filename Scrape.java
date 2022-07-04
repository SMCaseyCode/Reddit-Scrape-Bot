package Events;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static Events.Commands.activeList;
import static Events.Token_Grabber.token;

public class Scrape {

    public static OkHttpClient client = new OkHttpClient();

    public static boolean hasStartupPosts = false;

    public static void run() throws IOException, InterruptedException {

        int apiLimit = 25; //25 because that's the default Reddit API grab limit!, change this value if you change the limit.

        Events event = new Events();
        String rawData;
        String[] redditData = new String[apiLimit]; //Holds all data in the "data" portion of JSON
        String[] redditPosts = new String[apiLimit]; //Holds all Posts in the "data" section of JSON
        String[] redirectLinks = new String[apiLimit]; //Holds all RedirectLinks in the "data" section of JSON
        String[] alreadyPostedArray = new String[apiLimit]; //On startup collects all posts to compare to new ones
        String[] thumbnailUrls = new String[apiLimit]; //Holds all thumbnail URLS for each post
        String[] domainArray = new String[apiLimit]; //Holds all domains to be put as author in embed
        String[] miniUrl = new String[apiLimit]; //Holds the ID of the post. Helps create MiniURL in embeds
        String[] authorArray = new String[apiLimit]; //Holds the u/name of the user that posted said deal
        String tempString = "";
        boolean containsKeyword = false;
        boolean repeat = false;//loops scrape

        fillArray(alreadyPostedArray,apiLimit); //fills the array with non-null values. There is probably a better way. Perhaps using size and size++

        Request request = new Request.Builder()
                .header("User-Agent", "ScraperBot/1.0")
                .header("Authorization", "bearer " + token)
                .url("https://oauth.reddit.com/r/buildapcsales/new")
                .build();

        do {

            timePause();

            Call call = client.newCall(request); //Sets up call request
            Response response = call.execute(); //Actually Requests
            rawData = response.body().string(); //Places Request Body into a String and places it into rawData

            jsonParse(rawData, apiLimit, redditData, redditPosts, hasStartupPosts, alreadyPostedArray, redirectLinks, thumbnailUrls, domainArray, miniUrl, authorArray);
            hasStartupPosts = true;

            keywordCheck(apiLimit, tempString, redditPosts, containsKeyword, alreadyPostedArray, redirectLinks, event, thumbnailUrls, domainArray, miniUrl, authorArray);

            repeat = true;

        } while (repeat);

    }

    public static void fillArray(String[] nullArray, int apiLimit){ //replaces array's null values with non-nulls

        for (int i = 0; i < apiLimit; i++)
        {
            nullArray[i] = " ";
        }

    }

    public static void  timePause() throws InterruptedException {

        TimeUnit.SECONDS.sleep(60); // Loops the scrape, but on an X-second delay.

    }

    public static void jsonParse(String rawData, int apiLimit, String[] redditData, String[] redditPosts, boolean hasStartupPosts, String[] alreadyPostedArray, String[] redirectLinks, String[] thumbnailUrls, String[] domainArray, String[] miniUrl, String[] authorArray) throws JsonProcessingException {

        try {
            JsonNode node = parse(rawData); //parses rawData JSON
            node = node.get("data").get("children"); //gets node to == .json/data/children

            String filteredNode = node.toString(); //Makes node into a string and places it into filteredNode
            JsonArray endNode = JsonParser.parseString(filteredNode).getAsJsonArray(); //Places the parsed filteredNode String into endNode array to get each post and its data


            setRedditPosts(apiLimit, redditData, redditPosts, endNode, redirectLinks, thumbnailUrls, domainArray, miniUrl, authorArray);

            hasStartupPosts(hasStartupPosts, alreadyPostedArray, redditPosts, apiLimit);

        }catch (Exception e){
            System.out.println("Jackson Error");
        }

    }

    public static void setRedditPosts(int apiLimit, String[] redditData, String[] redditPosts, JsonArray endNode, String[] redirectLinks, String[] thumbnailUrls, String[] domainArray, String[] miniUrl, String[] authorArray) throws JsonProcessingException {

        for (int i = 0; i < apiLimit; i++)
        {
            redditData[i] = endNode.get(i).toString(); //gets EACH post individually and places it into redditData[]

            JsonNode singlePost = parse(redditData[i]); //Parses redditData[] into a JsonNode called singlePost
            redditPosts[i] = singlePost.get("data").get("title").toString(); //writes a single post from .json/data/title into redditPosts[]

            try { //Stops from crashing when no link is added to post.

                redirectLinks[i] = singlePost.get("data").get("url_overridden_by_dest").toString(); //gets redirect links

            } catch (Exception ignored) {

            }

            // Sets up everything needed for the embeds
            thumbnailUrls[i] = singlePost.get("data").get("thumbnail").toString().replaceAll("^\"|\"$", "");

            domainArray[i] = singlePost.get("data").get("domain").toString().replaceAll("^\"|\"$", "");

            miniUrl[i] = singlePost.get("data").get("id").toString().replaceAll("^\"|\"$", "");

            authorArray[i] = singlePost.get("data").get("author").toString().replaceAll("^\"|\"$", "");

        }

    }

    public static void hasStartupPosts(boolean hasStartupPosts, String[] alreadyPostedArray, String[]redditPosts, int apiLimit){

        for (int i = 0; i < apiLimit; i++)
        {
            if (!hasStartupPosts) //checks for startup posts then seals off once it has them.
            {
                alreadyPostedArray[i] = redditPosts[i];
            }
        }

    }

    public static void  keywordCheck(int apiLimit, String tempString, String[] redditPosts, boolean containsKeyword, String[] alreadyPostedArray, String[] redirectLinks, Events event, String[] thumbnailUrls, String[] domainArray, String[] miniUrl, String[] authorArray ) throws InterruptedException {

        int index = 100; //100 because an int can't be null
        String filteredPost; //post without quotes
        String filteredLink; //links without quotes

        for (int i = 0; i < apiLimit; i++) {


            if (!(Objects.equals(alreadyPostedArray[0], redditPosts[0]))) { //Checks for the amount of posts that have changed

                if (alreadyPostedArray[0].equals(redditPosts[i])) {
                    index = i;
                    break;
                }

            }

            else {

                break;

            }

        }

        if(!(index == 100)){ //if a new post was found

            for(int j = 0; j < index; j++){
                boolean alreadyPosted = false;

                System.out.println("New Post: " + redditPosts[j]); //shows the new post in the console
                System.out.println("------------------------------------------------------------------");

                tempString = redditPosts[j].replaceAll("[^a-zA-Z ]", "");
                String[] tempStringArray = tempString.split(" ");

                for (int k = 0; k < tempStringArray.length; k++){

                    for (int l = 0; l < activeList.length; l++){

                        if (tempStringArray[k].equalsIgnoreCase(activeList[l]) && !alreadyPosted){ //Checks for Keywords

                            filteredPost = redditPosts[j].replaceAll("^\"|\"$", "");
                            filteredLink = redirectLinks[j].replaceAll("^\"|\"$", "");

                            event.newPostEvent(filteredPost, filteredLink, thumbnailUrls[j], domainArray[j], miniUrl[j], authorArray[j]);
                            alreadyPosted = true; //otherwise posts twice if title has the keyword multiple times!
                            TimeUnit.SECONDS.sleep(2); //Gives JDA 2 seconds to catch up. Not doing so results in error.
                            break;

                        }
                    }
                }
            }
        }

        //Copies redditPosts to alreadyPostedArray. Very important. Without this line it will post every scrape attempt.
        if (apiLimit >= 0) System.arraycopy(redditPosts, 0, alreadyPostedArray, 0, apiLimit);

    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////// JSON MAPPING STARTS HERE ////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final ObjectMapper objectMapper = getDefaultObjectMapper(); //Heart of the parsing

    private static ObjectMapper getDefaultObjectMapper(){
        ObjectMapper defaultObjectMapper = new ObjectMapper();
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return defaultObjectMapper;
    }

    public static JsonNode parse(String src) throws JsonProcessingException {

        return objectMapper.readTree(src);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////// JSON MAPPING ENDS HERE //////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
