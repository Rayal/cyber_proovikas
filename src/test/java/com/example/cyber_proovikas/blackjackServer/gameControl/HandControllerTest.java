package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.CyberProovikasApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.linesOf;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CyberProovikasApplication.class)
public class HandControllerTest
{
    @Autowired
    HandController handController;

    private String owner = "user";
    private long gameId = 1;

    @Test
    public void insertCardTest()
    {
        long card = 11;
        handController.insertCardToHand(owner, card, gameId);

        Hand hand = handController.getHandbyOwner(owner).iterator().next();

        assertThat(hand.getCard()== card);
        assertThat(hand.getOwner().equals(owner));
        assertThat(hand.getDeck() == gameId);
    }

    @Test
    public void getCardsTest()
    {
        long card = 12;
        handController.insertCardToHand(owner, card, gameId);

        List<Long> hand = handController.getCardsbyOwner(owner);

        assertThat(hand.contains(card));
    }

    @Test
    public void moveCardTest()
    {
        long card = 13;
        handController.insertCardToHand("deck", card, gameId);

        handController.moveCardToHand(owner, gameId);

        List<Long> hand = handController.getCardsbyOwner(owner);

        assertThat(hand.contains(card));
    }

    @Test
    public void deleteDeckTest()
    {
        long card = 13;
        handController.insertCardToHand(owner, card, gameId);

        handController.cleanGameDeck(gameId);

        List<Long> deck = handController.getCardsbyDeck(gameId);

        assertThat(deck.isEmpty());
    }

}
