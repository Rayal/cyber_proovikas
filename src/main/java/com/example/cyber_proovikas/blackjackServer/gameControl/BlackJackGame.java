package com.example.cyber_proovikas.blackjackServer.gameControl;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class BlackJackGame
{
    private static long nextId = 0;

    static Logger logger = LoggerFactory.getLogger(BlackJackGame.class);
    static Random randomGenerator = new Random();

    public static long newBlackJackGame(String player, HandController handController)
    {
        logger.info("Creating a new Blackjack game.");

        BlackJackGame game = new BlackJackGame();
        //Random randomGenerator = new Random();

        // Create a new deck of cards and add it to the hand repo.
        for (long i = 1; i <= 52; i++)
        {
            handController.insertCardToHand("deck", i, nextId);
        }

        // Shuffle and deal cards.
        List<Long> cards = handController.getCardsbyOwner("deck", nextId);

        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0)
            {//
                hit(player, handController, nextId);
            }
            else
            {
                hit("dealer", handController, nextId);
            }
        }
        return nextId++;
    }

    public static long hit(String player, HandController handController, long gameId)
    {
        logger.info(String.format("Hitting player %s in game %d", player, gameId));
        List<Long> cards = handController.getCardsbyOwner("deck", gameId);

        int index = randomGenerator.nextInt(cards.size());
        long card = cards.get(index);

        handController.moveCardToHand(player, card, gameId);

        return card;
    }

}
