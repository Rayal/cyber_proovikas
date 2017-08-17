package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.CyberProovikasApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CyberProovikasApplication.class)
public class GameInfoControllerTest {
    @Autowired
    GameInfoController gameInfoController;

    @Test
    public void insertGameTest()
    {
        gameInfoController.insertGame(50);
        assertThat(gameInfoController.getStandGame(50) == false);
    }

    @Test
    public void setStandTest()
    {
        gameInfoController.insertGame(51);
        gameInfoController.setStandGame(51);
        assertThat(gameInfoController.getStandGame(51) == true);
    }

    @Test
    public void deleteGameTest()
    {
        gameInfoController.insertGame(52);
        gameInfoController.deleteGameInfoById(52);
        assertThat(gameInfoController.getOne((long) 52) == null);

    }
}
