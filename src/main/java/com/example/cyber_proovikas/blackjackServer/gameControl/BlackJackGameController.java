package com.example.cyber_proovikas.blackjackServer.gameControl;

import java.util.ArrayList;

public class BlackJackGameController {
    private static ArrayList<BlackJackGameController> runningGames = null;

    public static int createNewGame()
    {
        if(runningGames == null)
            runningGames = new ArrayList<BlackJackGameController>();

        runningGames.add(new BlackJackGameController());
        return runningGames.size()-1;
    }
}
