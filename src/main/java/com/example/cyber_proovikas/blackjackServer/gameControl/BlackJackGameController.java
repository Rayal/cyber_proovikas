package com.example.cyber_proovikas.blackjackServer.gameControl;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Data
public class BlackJackGameController
{
    public static BigDecimal minBet = new BigDecimal(100);
    private static long nextId = 0;

    static Logger logger = LoggerFactory.getLogger(BlackJackGameController.class);
    static Random randomGenerator = new Random();

    public static long newBlackJackGame(String player, HandController handController)
    {
        logger.info(String.format("Creating a new Blackjack game. ID: %d", nextId));

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

    public static long[] value(String player, HandController handController, long gameId)
    {
        logger.info(String.format("Evaluating %s's hand from game %d.", player, gameId));

        // First is soft, second is hard.
        long value[] = {0, 0};

        for (long card : handController.getCardsbyOwner(player, gameId))
        {
            card = (card % 13) + 1;
            if(card > 10)
            {
                value[1] += 10;
                value[0] += 10;
            }
            else if (card == 1)
            {
                value[1] += 1;
                value[0] += 10;
            }
            else
            {
                value[1] += card;
                value[0] += card;
            }
        }

        return value;
    }

    public static void stand(HandController handController, long gameId)
    {
        logger.info(String.format("Player Stand in game %d. Dealer's turn.", gameId));

        long value = value("dealer", handController, gameId)[0];
        while (value < 17)
        {
            hit("dealer", handController, gameId);
            value = value("dealer", handController, gameId)[0];
        }
    }

    public static void end(HandController handController, long gameId)
    {
        logger.info(String.format("Game %d ended.", gameId));

        handController.cleanGameDeck(gameId);
    }
}
