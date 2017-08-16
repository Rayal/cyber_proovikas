package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.blackjackServer.playerControl.Player;
import com.example.cyber_proovikas.blackjackServer.playerControl.PlayerController;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PlayerInputController
{
    @Autowired
    PlayerController playerController;
    @Autowired
    HandController handController;

    private Logger logger = LoggerFactory.getLogger(PlayerInputController.class);

    private String getUsernameFromRequest(String requestBody) throws JSONException {
        JSONObject request;

        request = new JSONObject(requestBody);
        return (String)request.get("username");
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity loginRequest(@RequestBody String body)
    {
        String username = "";

        try
        {
            username = getUsernameFromRequest(body);
        }
        catch (JSONException e)
        {
            logger.error(e.toString());
        }

        if (username.isEmpty())
        {
            logger.warn("Did not get username from request body.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        logger.info(String.format("Got username: %s", username));
        Player player = playerController.getPlayerByUsername(username);

        if (player == null)
        {
            logger.info("Player not found in database. Creating a new one.");
            playerController.addPlayer(username);
            return new ResponseEntity(HttpStatus.OK);
        }
        logger.info("Player found in database. Not creating a new one.");

        return new ResponseEntity(HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/game", method = RequestMethod.PUT)
    public ResponseEntity newGameRequest(@RequestBody String body)
    {
        String username = "";

        try
        {
            username = getUsernameFromRequest(body);
        }
        catch (JSONException e)
        {
            logger.error(e.toString());
        }

        if (username.isEmpty())
        {
            logger.warn("Did not get username from request body.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        logger.info(String.format("Got username: %s", username));
        Player player = playerController.getPlayerByUsername(username);

        if(player == null)
        {
            logger.error("Player username not found.");
            return new ResponseEntity(HttpStatus.FAILED_DEPENDENCY);
        }

        long gameId = playerController.getGameByUsername(username);

        if (gameId == -1) {
            // Game doesn't exist, creating a new one.
            gameId = BlackJackGame.newBlackJackGame(username, handController);
            playerController.setGameByUsername(username, gameId);
        }

        JSONObject response = new JSONObject();
        try {
            response.accumulate("gameId", gameId);
            response.accumulate("playerHand", handController.getCardsbyOwner(username, gameId));
            response.accumulate("dealerHand", handController.getCardsbyOwner("dealer", gameId).get(0));
        } catch (JSONException e) {
            logger.error(e.toString());
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");

        return new ResponseEntity(response.toString(), httpHeaders, HttpStatus.OK);
    }
}
