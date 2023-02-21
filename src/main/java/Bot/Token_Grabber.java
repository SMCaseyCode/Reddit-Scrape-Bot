package Bot;

import masecla.reddit4j.client.Reddit4J;
import masecla.reddit4j.client.UserAgentBuilder;
import masecla.reddit4j.exceptions.AuthenticationException;

import java.io.IOException;

import static Data.ProtectedData.*;

public class Token_Grabber {

    //Grabs client info
    public static Reddit4J getClient() {

        Reddit4J client = Reddit4J.rateLimited().setUsername(USERNAME.getContent()).setPassword(PASSWORD.getContent())
                .setClientId(CLIENTID.getContent()).setClientSecret(CLIENTSECRET.getContent())
                .setUserAgent(new UserAgentBuilder().appname("DealScrapingBot").author("TTVDocSnipe").version("2.0"));

        try {
            client.connect();
        } catch (IOException | AuthenticationException | InterruptedException e) {
            System.out.println("Connection Error in Token_Grabber");
        }

        return client;

    }

}
