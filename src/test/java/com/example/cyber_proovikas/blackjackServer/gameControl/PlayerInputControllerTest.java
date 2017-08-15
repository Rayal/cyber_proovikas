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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CyberProovikasApplication.class)
public class PlayerInputControllerTest
{
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void loginResponseTest() throws Exception
    {
        String request = "{\"username\" : \"test\"}";

        // This request has no body. Should return BAD REQUEST.
        mockMvc.perform(post("/login"))
                .andExpect(status().isBadRequest());

        // This request is perfect. Should succeed.
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk());

        // Now we already have a user with name "test". Should return CONFLICT.
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isConflict());
    }

}