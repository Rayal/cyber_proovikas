package com.example.cyber_proovikas;

import com.example.cyber_proovikas.blackjackServer.playerControl.PlayerController;
import com.example.cyber_proovikas.blackjackServer.playerControl.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CyberProovikasApplication.class)
public class PlayerControllerTest {
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private PlayerController playerController;

    private MockMvc mockMvc;

    /*@Before
    public void setup()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }*/

    @Test
    public void createPlayerTest()
    {
        playerController.addPlayer("Test Player", "username");
        Player player = playerController.getPlayerByUsername("username");

        assertThat(player.getName().equals("Test Player"));
    }

    @Test
    public void addFundsTest()
    {
        playerController.setFundsByUsername("username", new BigDecimal(100));
        Player player = playerController.getPlayerByUsername("username");

        assertThat(player.getFunds().equals(100));
    }

    @Test
    public void checkFundsTest()
    {
        assertThat(playerController.getFundsByUsername("username").equals(100));
    }
}
