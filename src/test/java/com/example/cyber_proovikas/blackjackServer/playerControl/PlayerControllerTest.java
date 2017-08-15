package com.example.cyber_proovikas.blackjackServer.playerControl;

import com.example.cyber_proovikas.CyberProovikasApplication;
import com.example.cyber_proovikas.blackjackServer.playerControl.PlayerController;
import com.example.cyber_proovikas.blackjackServer.playerControl.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CyberProovikasApplication.class)
public class PlayerControllerTest {
    @Autowired
    private PlayerController playerController;

    String username = "username";

    @Test
    public void createPlayerTest()
    {
        playerController.addPlayer(username);
        Player player = playerController.getPlayerByUsername(username);

        assertThat(player.getUsername().equals(username));
        assertThat(player.getGame() == -1);
    }

    @Test
    public void addFundsTest()
    {
        playerController.setFundsByUsername(username, new BigDecimal(100));
        Player player = playerController.getPlayerByUsername(username);

        assertThat(player.getFunds().equals(100));
    }

    @Test
    public void checkFundsTest()
    {
        assertThat(playerController.getFundsByUsername("username").equals(100));
    }

    @Test
    public void getSetRunningGameIdTest()
    {
        playerController.setGameByUsername(username, 1);
        int game = playerController.getGameByUsername(username);
        Player player = playerController.getPlayerByUsername(username);

        assertThat(player.getGame() == 1);
        assertThat(game == player.getGame());
    }
}
