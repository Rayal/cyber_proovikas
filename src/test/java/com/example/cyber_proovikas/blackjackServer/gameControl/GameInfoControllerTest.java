package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.CyberProovikasApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CyberProovikasApplication.class)
public class GameInfoControllerTest {
    @Autowired
    GameInfoController gameInfoController;

    @Test
    public void insertGameTest()
    {
        BigDecimal bet = new BigDecimal(100);
        gameInfoController.insertGame(50, bet);
        assertThat(gameInfoController.getStandGame(50) == false);
        assertThat(gameInfoController.getBetById(50).equals(bet));
    }

    @Test
    public void setStandTest()
    {
        gameInfoController.insertGame(51, new BigDecimal(100));
        gameInfoController.setStandGame(51);
        assertThat(gameInfoController.getStandGame(51) == true);
    }

    @Test
    public void deleteGameTest()
    {
        gameInfoController.insertGame(52, new BigDecimal(100));
        gameInfoController.deleteGameInfoById(52);
        assertThat(gameInfoController.getOne((long) 52) == null);

    }
}
