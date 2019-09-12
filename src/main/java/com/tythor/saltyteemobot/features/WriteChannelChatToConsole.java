package com.tythor.saltyteemobot.features;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class WriteChannelChatToConsole {
    int blue = 0;
    int red = 0;
    int farm = 0;

    /**
     * Register events of this class with the EventManager
     *
     * @param eventManager EventManager
     */
    public WriteChannelChatToConsole(EventManager eventManager) {
        eventManager.onEvent(ChannelMessageEvent.class).subscribe(event -> onChannelMessage(event));
    }

    /**
     * Subscribe to the ChannelMessage Event and write the output to the console
     */
    public void onChannelMessage(ChannelMessageEvent event) {
        if (event.getMessage().contains("TripleFury") || event.getUser().getName().equals("TripleFury"))
            System.out.printf("User[%s] - Message[%s]%n", event.getUser().getName(), event.getMessage());
        betting(event);
        farming(event);

        /*if(event.getMessage().contains("!"))
            event.getTwitchChat().sendMessage(event.getChannel().getName(), "hi");*/
    }

    private void betting(ChannelMessageEvent event) {
        if (event.getMessage().contains("Bet complete for BLUE")) {
            blue++;
        } else if (event.getMessage().contains("Bet complete for RED")) {
            red++;
        } else if(event.getMessage().contains("Betting has ended")) {
            blue = 0;
            red = 0;
        }

        if (blue + red > 50) {
            String team;
            int amount;

            if (blue > red) {
                team = "blue";
                amount = blue;
            } else {
                team = "red";
                amount = red;
            }
            amount *= 5;

            System.out.println("[Log] Blue total: " + blue + " | Red total: " + red);
            System.out.println(String.format("[Log] Betting %d for %s...", amount, team));
            event.getTwitchChat().sendMessage(event.getChannel().getName(), String.format("!%s %d", team, amount));

            blue = 0;
            red = 0;
        }
    }

    private void farming(ChannelMessageEvent event) {
        if (event.getMessage().contains("!farm") || event.getMessage().contains("!forage")) {
            if (farm == 0) {
                System.out.println("[Log] Attempting to farm...");
                String farmString;
                if (Math.random() <= .5)
                    farmString = "!farm";
                else
                    farmString = "!forage";
                event.getTwitchChat().sendMessage(event.getChannel().getName(), farmString);
            }

            farm++;
            if (farm > 200)
                farm = 0;
        }
    }

}
