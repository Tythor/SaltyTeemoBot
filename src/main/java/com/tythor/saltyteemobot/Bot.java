package com.tythor.saltyteemobot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.tythor.saltyteemobot.features.ChannelNotificationOnDonation;
import com.tythor.saltyteemobot.features.ChannelNotificationOnFollow;
import com.tythor.saltyteemobot.features.ChannelNotificationOnSubscription;
import com.tythor.saltyteemobot.features.WriteChannelChatToConsole;

import java.io.InputStream;
import java.util.HashMap;

public class Bot {

    /**
     * Holds the Bot Configuration
     */
    private Configuration configuration;

    /**
     * Twitch4J API
     */
    private TwitchClient twitchClient;

    /**
     * Constructor
     */
    public Bot() {
        // Load Configuration
        loadConfiguration();

        TwitchClientBuilder clientBuilder = TwitchClientBuilder.builder();

        //region Auth
        OAuth2Credential credential = new OAuth2Credential(
                "twitch",
                configuration.getCredentials().get("irc")
        );
        //endregion

        //region TwitchClient
        twitchClient = clientBuilder
                .withClientId(configuration.getApi().get("twitch_client_id"))
                .withClientSecret(configuration.getApi().get("twitch_client_secret"))
                .withEnableHelix(true)
                /*
                 * Chat Module
                 * Joins irc and triggers all chat based events (viewer join/leave/sub/bits/gifted subs/...)
                 */
                .withChatAccount(credential)
                .withEnableChat(true)
                /*
                 * GraphQL has a limited support
                 * Don't expect a bunch of features enabling it
                 */
                .withEnableGraphQL(true)
                /*
                 * Kraken is going to be deprecated
                 * see : https://dev.twitch.tv/docs/v5/#which-api-version-can-you-use
                 * It is only here so you can call methods that are not (yet)
                 * implemented in Helix
                 */
                .withEnableKraken(true)
                /*
                 * Build the TwitchClient Instance
                 */
                .build();
        //endregion
        WriteChannelChatToConsole writeChannelChatToConsole = new WriteChannelChatToConsole(twitchClient.getEventManager());
    }

    /**
     * Method to register all features
     */
    public void registerFeatures() {
        // Register Event-based features
        ChannelNotificationOnDonation channelNotificationOnDonation = new ChannelNotificationOnDonation(twitchClient.getEventManager());
        ChannelNotificationOnFollow channelNotificationOnFollow = new ChannelNotificationOnFollow(twitchClient.getEventManager());
        ChannelNotificationOnSubscription channelNotificationOnSubscription = new ChannelNotificationOnSubscription(twitchClient.getEventManager());
        ChannelNotificationOnDonation channelNotificationOnDonation1 = new ChannelNotificationOnDonation(twitchClient.getEventManager());
    }

    /**
     * Load the Configuration
     */
    private void loadConfiguration() {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("localConfig.yaml");

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            configuration = mapper.readValue(is, Configuration.class);
        } catch (Exception ex) {
            try {
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                InputStream is = classloader.getResourceAsStream("config.yaml");

                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                configuration = mapper.readValue(is, Configuration.class);

                HashMap<String, String> api = new HashMap<>();
                api.put("twitch_client_id", System.getenv("twitch_client_id"));
                api.put("twitch_client_secret", System.getenv("twitch_client_secret"));
                configuration.setApi(api);

                HashMap<String, String> credentials = new HashMap<>();
                credentials.put("irc", System.getenv("irc"));
                configuration.setCredentials(credentials);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Unable to load Configuration ... Exiting.");
                System.exit(1);
            }
        }

        System.out.println(configuration.toString());
    }

    public void start() {
        // Connect to all channels
        for (String channel : configuration.getChannels()) {
            twitchClient.getChat().joinChannel(channel);
        }
    }

}
