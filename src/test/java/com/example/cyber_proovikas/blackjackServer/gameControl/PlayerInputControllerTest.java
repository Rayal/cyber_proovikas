package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.CyberProovikasApplication;
import com.example.cyber_proovikas.blackjackServer.playerControl.PlayerController;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CyberProovikasApplication.class)
public class PlayerInputControllerTest
{
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private String username = "test";

    @Before
    public void setup()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void loginResponseTest1() throws Exception
    {
        String request = String.format("{\"username\" : \"%s\"}", username);

        // This request has no body. Should return BAD REQUEST.
        mockMvc.perform(post("/login"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void loginResponseTest2() throws Exception
    {
        String request = String.format("{\"username\" : \"%s\"}", username);

        // This request is perfect. Should succeed.
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk());
    }

    @Test
    public void loginResponseTest3() throws Exception
    {
        String request = String.format("{\"username\" : \"%s\"}", username);

        // Now we already have a user with name "test". Should return CONFLICT.
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    public void newGameRequestTest() throws Exception
    {
        String request = String.format("{\"username\" : \"%s%s\"}", username, "game");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

        mockMvc.perform(get("/game"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").exists());
    }
}