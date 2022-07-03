package Events;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.client.UserAgentBuilder;
import masecla.reddit4j.exceptions.AuthenticationException;

import java.io.IOException;

public class Token_Grabber {

    public static String token = "";

    public static Reddit4J getClient() throws AuthenticationException, IOException, InterruptedException { //Gets you that juicy Oauth Token.

        Reddit4J client = Reddit4J.rateLimited().setUsername("Username").setPassword("Password")
                .setClientId("Client ID").setClientSecret("Client Secret")
                .setUserAgent(new UserAgentBuilder().appname("AppName").author("u/name").version("1.0"));

        client.connect();

        token = client.getToken();

        return client;
    }



}
