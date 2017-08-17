package com.example.cyber_proovikas.blackjackServer.playerControl;

import com.example.cyber_proovikas.blackjackServer.gameControl.BlackJackGameController;
import com.example.cyber_proovikas.blackjackServer.gameControl.GameActionController;
import com.example.cyber_proovikas.blackjackServer.gameControl.GameInfoController;
import com.example.cyber_proovikas.blackjackServer.gameControl.HandController;

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
    @Autowired
    GameInfoController gameInfoController;

    private Logger logger = LoggerFactory.getLogger(PlayerInputController.class);

    private String getUsernameFromRequest(String requestBody) throws JSONException {
        JSONObject request;

        request = new JSONObject(requestBody);
        return (String)request.get("username");
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity loginRequest(@RequestBody String body)
    {
        // Get the username from the request body.
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

        // Next, check if there already is someone logged in with that username.
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
        //First, get the username from the request body.
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

        // Next, check to see if we have such a username logged in.
        Player player = playerController.getPlayerByUsername(username);

        if(player == null)
        {
            logger.error("Player username not found.");
            return new ResponseEntity(HttpStatus.FAILED_DEPENDENCY);
        }

        // Check if the username has a game still running.
        long gameId = playerController.getGameByUsername(username);

        if (gameId == -1) {
            // Game doesn't exist, creating a new one.
            gameId = BlackJackGameController.newBlackJackGame(username, handController);
            playerController.setGameByUsername(username, gameId);
            gameInfoController.insertGame(gameId);
        }

        // Putting together the JSON object with our response to the user.
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

    @RequestMapping(value = "/game/play", method = RequestMethod.GET)
    public ResponseEntity gameActionRequest(@RequestBody String body)
    {
        // Get username
        String username = "";
        try {
            username = getUsernameFromRequest(body);
        } catch (JSONException e) {
            logger.error(e.toString());
        }

        if (username.isEmpty())
        {
            logger.warn("Did not get username from request body.");

            String response = "{\"message\": \"username not found\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

            return new ResponseEntity(response, headers, HttpStatus.BAD_REQUEST);
        }

        // Find out if user exists.
        if(playerController.getPlayerByUsername(username) == null)
        {
            logger.error("Player username not found.");

            String response = "{\"message\": \"No such username\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

            return new ResponseEntity(response, headers, HttpStatus.FAILED_DEPENDENCY);
        }

        // Get gameId
        long gameId = playerController.getGameByUsername(username);
        if (gameId == -1)
        {
            logger.error("User %s does not have a running game.");

            String response = "{\"message\": \"No game running\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

            return new ResponseEntity(response, headers, HttpStatus.FAILED_DEPENDENCY);
        }

        String gameAction = "";
        try {
            JSONObject request = new JSONObject(body);
            gameAction = (String) request.get("gameAction");
        } catch (JSONException e) {
            logger.error(e.toString());
        }

        if (gameAction.isEmpty())
        {
            logger.error("GameAction not found.");

            String response = "{\"message\": \"gameAction not found\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

            return new ResponseEntity(response, headers, HttpStatus.BAD_REQUEST);
        }

        if (gameAction.equals("hit"))
        {
            logger.info(String.format("Player %s requests hit in game %d", username, gameId));
            return GameActionController.hit(username, handController, gameInfoController, gameId);
        }

        if (gameAction.equals("stand"))
        {
            logger.info(String.format("Player %s requests stand in game %d", username, gameId));
            return GameActionController.stand(username, handController, gameInfoController, gameId);
        }

        if (gameAction.equals("end"))
        {
            logger.info(String.format("Player %s requests end in game %d", username, gameId));
            playerController.setGameByUsername(username, -1);
            return GameActionController.end(username, handController, gameInfoController, gameId);
        }
        logger.error(String.format("Request made no sense.\n%s", body));
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
