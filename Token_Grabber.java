package Events;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.client.UserAgentBuilder;
import masecla.reddit4j.exceptions.AuthenticationException;

import java.io.IOException;

public class Token_Grabber {

    public static String token = "";

    public static Reddit4J getClient() { //Gets you that juicy Oauth Token.

        Reddit4J client = Reddit4J.rateLimited().setUsername("Username").setPassword("Password")
                .setClientId("Client").setClientSecret("Secret")
                .setUserAgent(new UserAgentBuilder().appname("AppName").author("r/user").version("1.0"));

        try {
            client.connect();
        } catch (IOException | AuthenticationException | InterruptedException e) {
            System.out.println("Connection Error in Token_Grabber");
        }

        token = client.getToken();

        System.out.println("Grabbed Token: " + client.getToken());
        System.out.println("Token Time Left: " + client.getTokenExpirationDate());


        return client;
    }



}
