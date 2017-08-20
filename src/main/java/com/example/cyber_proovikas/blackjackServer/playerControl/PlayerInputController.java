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

import java.math.BigDecimal;

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

    private BigDecimal getFundsFromRequest(String requestBody) throws JSONException {
        JSONObject request;

        request = new JSONObject(requestBody);
        try {
            return new BigDecimal((int) request.get("funds"));
        }
        catch (Exception e){
            return new BigDecimal(0);
        }
    }

    private BigDecimal getWithdrawFromRequest(String requestBody) throws JSONException
    {
        JSONObject request;

        request = new JSONObject(requestBody);
        try {
            return new BigDecimal((int) request.get("withdraw"));
        }
        catch (Exception e){
            return new BigDecimal(0);
        }
    }

    private BigDecimal getBetFromRequest(String requestBody) throws JSONException {
        JSONObject request;

        request = new JSONObject(requestBody);
        return new BigDecimal(String.valueOf(request.get("bet")));
    }

    private String getUsernameFromRequest(String requestBody) throws JSONException {
        JSONObject request;

        request = new JSONObject(requestBody);
        return (String)request.get("username");
    }

    // Check if the player has enough funds to play the game.
    private boolean checkForBets(String username, BigDecimal bets)
    {
        BigDecimal playerFunds = playerController.getFundsByUsername(username);
        try
        {
            playerFunds = playerFunds.subtract(bets);
        }
        catch (NullPointerException e)
        {
            playerController.setFundsByUsername(username, new BigDecimal(0));
            return false;
        }

        if (playerFunds.intValue() < 0)
        {
            return false;
        }
        playerController.setFundsByUsername(username, playerFunds);
        return true;
    }

    // Mostly unused. For debugging purposes.
    @RequestMapping(value = "/ping", method = RequestMethod.POST)
    public ResponseEntity pingRequest(@RequestBody String body)
    {
        logger.debug(body);
        System.out.println(body);
        return new ResponseEntity(body, HttpStatus.OK);
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

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject response = new JSONObject();

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

            try {
                response.accumulate("message","Username not found");
            } catch (JSONException e) {
                logger.error(e.toString());
            }

            return new ResponseEntity(response.toString(), httpHeaders, HttpStatus.BAD_REQUEST);
        }

        logger.info(String.format("Got username: %s", username));

        // Next, check to see if we have such a username logged in.
        Player player = playerController.getPlayerByUsername(username);

        if(player == null)
        {
            logger.error("Player username not found.");

            try {
                response.accumulate("message","Username not found in database");
            } catch (JSONException e) {
                logger.error(e.toString());
            }

            return new ResponseEntity(response.toString(), httpHeaders, HttpStatus.FAILED_DEPENDENCY);
        }

        // Check if the username has a game still running.
        long gameId = playerController.getGameByUsername(username);

        if (gameId == -1) {// Game doesn't exist, creating a new one.

            // Make sure the player has enough money to bet.
            BigDecimal bet = BlackJackGameController.minBet;
            try {
                bet = getBetFromRequest(body);
            } catch (JSONException e) {
                logger.warn(e.toString());
            }

            if (bet.subtract(BlackJackGameController.minBet).intValue() < 0)
            {
                try
                {
                    response.accumulate("warning",
                            String.format("Bet too low. Using default of %d",
                                    BlackJackGameController.minBet.intValue())
                    );
                }
                catch (JSONException e)
                {
                    logger.error(e.toString());
                }

                bet = BlackJackGameController.minBet;
            }

            if (!checkForBets(username, bet))
            {
                logger.warn("Inadequate funds to start game");
                try
                {
                    response.accumulate("message","Inadequate funds to start the game");
                }
                catch (JSONException e)
                {
                    logger.error(e.toString());
                }

                return new ResponseEntity(response.toString(), httpHeaders, HttpStatus.FAILED_DEPENDENCY);
            }
            gameId = BlackJackGameController.newBlackJackGame(username, handController);
            playerController.setGameByUsername(username, gameId);
            gameInfoController.insertGame(gameId, bet);
        }

        // Putting together the JSON object with our response to the user.
        try {
            response.accumulate("gameId", gameId);
            response.accumulate("playerHand", handController.getCardsbyOwner(username, gameId));
            response.accumulate("dealerHand", handController.getCardsbyOwner("dealer", gameId).get(0));
        } catch (JSONException e) {
            logger.error(e.toString());
        }

        return new ResponseEntity(response.toString(), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/game/play", method = RequestMethod.POST)
    public ResponseEntity gameActionRequest(@RequestBody String body)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
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

            String response = "{\"message\": \"Username not found\"}";

            return new ResponseEntity(response, headers, HttpStatus.BAD_REQUEST);
        }

        // Find out if user exists.
        if(playerController.getPlayerByUsername(username) == null)
        {
            logger.error("Player username not found.");

            String response = "{\"message\": \"No such username\"}";

            return new ResponseEntity(response, headers, HttpStatus.FAILED_DEPENDENCY);
        }

        // Get gameId
        long gameId = playerController.getGameByUsername(username);
        if (gameId == -1)
        {
            logger.error("User %s does not have a running game.");

            String response = "{\"message\": \"No game running\"}";

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
            return GameActionController.end(username, handController, gameInfoController, playerController, gameId);
        }
        logger.error(String.format("Request made no sense.\n%s", body));
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/funds", method = RequestMethod.POST)
    public ResponseEntity addFundsRequest(@RequestBody String body)
    {
        logger.info("Transaction requested");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");

        String username = "";
        try {
            username = getUsernameFromRequest(body);
        } catch (JSONException e) {
            logger.error(e.toString());
        }

        if (username.isEmpty())
        {
            logger.error("Username not found");

            String response = "{\"message\": \"Username not found.\"}";
            return new ResponseEntity(response, httpHeaders, HttpStatus.BAD_REQUEST);
        }

        // Assume that the player wants to deposit.

        boolean deposit = true;

        BigDecimal funds = new BigDecimal(0);
        try {
            funds = getFundsFromRequest(body);
        } catch (JSONException e) {
            logger.warn(e.toString());
        }

        // If this was not the case, check to see if they wanted to withdraw.
        if (funds.intValue() == 0) {
            try {
                funds = getWithdrawFromRequest(body);
                deposit = false;
            } catch (JSONException e) {
                logger.warn(e.toString());
            }
        }

        if (funds.intValue() == 0)
        {
            logger.error("Transaction not found");

            String response = "{\"message\": \"Transaction not found.\"}";
            return new ResponseEntity(response, httpHeaders, HttpStatus.BAD_REQUEST);
        }

        BigDecimal playerFunds = playerController.getFundsByUsername(username);
        /*try {
            playerFunds
        }
        catch (NullPointerException e)
        {
            logger.warn(String.format("%s has no funds yet", username));
            playerFunds = new BigDecimal(0);
        }*/
        if (playerFunds == null)
        {
            logger.warn(String.format("%s has no funds yet", username));
            playerFunds = new BigDecimal(0);
        }

        if (deposit) {
            funds = funds.add(playerFunds);
            playerController.setFundsByUsername(username, funds);
        }
        else
        {
            if (playerFunds.intValue() >= funds.intValue())
            {
                playerFunds = playerFunds.subtract(funds);
                playerController.setFundsByUsername(username, playerFunds);
            }
            else
            {
                logger.warn("Player attempted to withdraw more than they have.");

                String response = "{\"message\": \"Inadequate funds.\"}";
                return new ResponseEntity(response, httpHeaders, HttpStatus.FORBIDDEN);
            }
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
