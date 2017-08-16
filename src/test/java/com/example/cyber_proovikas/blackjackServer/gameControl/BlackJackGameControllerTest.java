package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.CyberProovikasApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CyberProovikasApplication.class)
public class BlackJackGameControllerTest {
    @Autowired
    HandController handController;

    @Test
    public void newGameTest()
    {
        String player = "createBJTester";
        long gameId = BlackJackGameController.newBlackJackGame(player, handController);

        assertThat(handController.getCardsbyDeck(gameId).size() == 52);
        assertThat(handController.getCardsbyOwner(player, gameId).size() == 2);
        assertThat(handController.getCardsbyOwner("dealer", gameId).size() == 2);
    }

    @Test
    public void hitTest()
    {
        String player = "hitBJTester";
        long gameId = BlackJackGameController.newBlackJackGame(player, handController);

        BlackJackGameController.hit(player, handController, gameId);

        assertThat(handController.getCardsbyDeck(gameId).size() == 52);
        assertThat(handController.getCardsbyOwner(player, gameId).size() == 3);
        assertThat(handController.getCardsbyOwner("dealer", gameId).size() == 2);
    }

    @Test
    public void handValueTest()
    {
        String player = "handValueTester";
        long gameId = BlackJackGameController.newBlackJackGame(player, handController);

        long[] value = BlackJackGameController.value(player, handController, gameId);

        List<Long> hand = handController.getCardsbyOwner(player, gameId);

        long soft_sum = 0;
        long hard_sum = 0;
        for (long card : hand)
        {
            card = (card % 13) + 1;
            if (card > 10)
            {
                soft_sum += 10;
                hard_sum += 10;
            }
            else if (card == 1)
            {
                soft_sum += 10;
                hard_sum ++;
            }
            else
            {
                soft_sum += card;
                hard_sum += card;
            }
        }

        assertThat(value[0] == soft_sum);
        assertThat(value[1] == hard_sum);
    }

    @Test
    public void standTest()
    {
        String player = "standBJTester";
        long gameId = BlackJackGameController.newBlackJackGame(player, handController);

        BlackJackGameController.stand(handController, gameId);

        long value = BlackJackGameController.value("dealer", handController, gameId)[0];

        assertThat(handController.getCardsbyDeck(gameId).size() == 52);
        assertThat(handController.getCardsbyOwner(player, gameId).size() == 2);
        assertThat(value >= 17);
    }
}
