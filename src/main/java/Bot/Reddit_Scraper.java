package Bot;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditPost;
import masecla.reddit4j.objects.Sorting;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static Data.DatabaseManager.connect;

public class Reddit_Scraper {

    public static void redditScrape(){
        Reddit4J client = Token_Grabber.getClient();
        List<RedditPost> toPost = new ArrayList<>();
        List<RedditPost> currentPosts;
        List<String> currentTitles = new ArrayList<>();
        EventManager event = new EventManager();

        try {
            currentPosts = client.getSubredditPosts("buildapcsales", Sorting.NEW).submit();
            //sets current titles. i-- because it needs to be placed in backwards
            for (int i = currentPosts.size() - 1; i >= 0; i--){
                currentTitles.add(currentPosts.get(i).getTitle());
            }

        }catch (AuthenticationException | IOException | InterruptedException e){
            System.out.println("Initial Post Grab ERROR: " + e);
        }

        //Infinite scrape loop
        do {
            //Delays infinite loop by X seconds
            delayScrape();
            int foundCount = 0;

            try {
                List<RedditPost> newPosts = client.getSubredditPosts("buildapcsales", Sorting.NEW).submit();
                for (RedditPost post : newPosts) {
                    //Checks for new posts.

                    if (!currentTitles.contains(post.getTitle())) {
                        toPost.add(post);
                        //Removes front of currentTitle, and places newPost to the back.
                        currentTitles.add(post.getTitle());
                        foundCount++;
                    }
                }

                if (foundCount > 0) {
                    currentTitles.subList(0, foundCount).clear();
                }

                //If new posts were found run this code
                if (toPost.size() > 0){
                    for (RedditPost redditPost : toPost) {
                        System.out.println("NEW POST: " + redditPost.getTitle());
                        System.out.println("---------------------------------------------------");
                    }
                    getServerKeywords(toPost, event);
                    toPost.clear();
                }

            } catch (AuthenticationException | IOException | InterruptedException e) {
                System.out.println("SCRAPE ERROR: " + e);
            }

        }while (true);

    }

    //Delays redditScrape loop
    private static void delayScrape(){
        try {
            //Delay length, change as desired.
            TimeUnit.SECONDS.sleep(30);
        }catch (InterruptedException e){
            System.out.println("delayScrape ERROR: " + e);
        }
    }

    //Gets keywords for each server and handles sending events to post to channels
    public static void getServerKeywords(List<RedditPost> toPost, EventManager event){
        try(Connection conn = connect()){

            String[] postTitleSplitArray;
            List<String> alreadyPosted = new ArrayList<>();

            String getQuery = "select * from server join channel c on server.serverID = c.serverID order by server.serverID";
            PreparedStatement ps = conn.prepareStatement(getQuery);
            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                for (int i = 0; i < toPost.size(); i++){
                    String rawTitle = toPost.get(i).getTitle().replaceAll("^\"|\"$", "");

                    postTitleSplitArray = rawTitle.split(" ");
                    for (int j = 0; j < postTitleSplitArray.length; j++){
                        if (Objects.equals(rs.getString("keyword"), postTitleSplitArray[j].toUpperCase(Locale.ROOT))){
                            String channelID = rs.getString("channelID");
                            RedditPost post = toPost.get(i);

                            //Makes sure it doesn't post multiple times if a title contains > 1 keyword
                            if (!alreadyPosted.contains(channelID)){
                                event.newPostEvent(post.getTitle(),
                                        post.getUrl(),
                                        post.getThumbnail(),
                                        post.getDomain(),
                                        post.getId(),
                                        post.getAuthor(),
                                        channelID);

                                alreadyPosted.add(channelID);
                            }

                            break;
                        }

                    }
                }
            }
            ps.close();

        }catch (SQLException e){
            System.out.println("GETSERVERKEYWORDS SQL ERROR: " + e);
        }

    }
}
