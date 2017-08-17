package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.CyberProovikasApplication;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

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

        mockMvc.perform(put("/game"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.playerHand").isNotEmpty())
                .andExpect(jsonPath("$.dealerHand").isNotEmpty());
    }

    @Test
    public void hitTest() throws Exception
    {
        JSONObject request = new JSONObject(
                String.format("{\"username\" : \"%s%s\"}",
                        username, "hitTest")
        );

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()));

        MvcResult result = mockMvc.perform(put("/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andReturn();

        JSONObject content = new JSONObject(result.getResponse().getContentAsString());

        request.accumulate("gameAction", "hit");

        String returnedContent = mockMvc.perform(get("/game/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject nContent = new JSONObject(returnedContent);
        assertThat(nContent.get("playerHand").toString().length() > content.get("playerHand").toString().length());
    }

    @Test
    public void standTest() throws Exception
    {
        JSONObject request = new JSONObject(
                String.format("{\"username\" : \"%s%s\"}",
                        username, "standTest")
        );

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()));

        mockMvc.perform(put("/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andReturn();

        request.accumulate("gameAction", "stand");

        String returnedContent = mockMvc.perform(get("/game/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject content = new JSONObject(returnedContent);

        String[] hand = ((String) content.get("dealerHand"))
                .replaceAll("\\[", "")
                .replaceAll("]", "")
                .split(", ");

        long sum = 0;
        for (String card : hand)
        {
            long nCard = Long.parseLong(card);
            nCard = (nCard / 13) + 1;
            sum += nCard;
        }

        assertThat(sum >= 17);
    }

    @Test
    public void endGameTest() throws Exception
    {
        JSONObject request = new JSONObject(
                String.format("{\"username\" : \"%s%s\"}",
                        username, "endGameTest")
        );

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()));

        mockMvc.perform(put("/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()));

        request.accumulate("gameAction", "end");

        String returnedContent = mockMvc.perform(get("/game/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONObject content = new JSONObject(returnedContent);

        String[] hand = ((String) content.get("dealerHand"))
                .replaceAll("\\[", "")
                .replaceAll("]", "")
                .split(", ");

        long sum = 0;
        for (String card : hand)
        {
            long nCard = Long.parseLong(card);
            nCard = (nCard / 13) + 1;
            sum += nCard;
        }

        assertThat(sum >= 17);

        mockMvc.perform(get("/game/play")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isFailedDependency());
    }
}
