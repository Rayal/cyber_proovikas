package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.blackjackServer.playerControl.PlayerController;
import com.sun.imageio.plugins.gif.GIFImageMetadata;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

public class GameActionController {
    static Logger logger = LoggerFactory.getLogger(GameActionController.class);

    public static ResponseEntity hit(String username, HandController handController, GameInfoController gameInfoController, long gameId)
    {
        logger.info(String.format("Player %s requests a hit.", username));

        JSONObject response = new JSONObject();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpStatus status = null;

        if (gameInfoController.getStandGame(gameId))
        {
            logger.error("Cannot hit: user already stood.");
            try
            {
                response.accumulate("message", "Cannot hit: Player already Stood.");
                response.accumulate("dealerHand", handController.getCardsbyOwner("dealer", gameId));
            } catch (JSONException e)
            {
                logger.error(e.toString());
            }
            status = HttpStatus.BAD_REQUEST;
        }
        else if (BlackJackGameController.value(username, handController, gameId)[1] >  21)
        {
            // The Player is already bust.
            logger.warn("Player bust but tried to hit.");

            try
            {
                response.accumulate("message", "Cannot hit: Player bust.");
                response.accumulate("dealerHand", handController.getCardsbyOwner("dealer", gameId));
            }
            catch (JSONException e)
            {
                logger.error(e.toString());
            }
            status = HttpStatus.BAD_REQUEST;
        }
        else
        {
            BlackJackGameController.hit(username, handController, gameId);
            // Check for bust.
            if (BlackJackGameController.value(username, handController, gameId)[1] >  21)
            {
                BlackJackGameController.stand(handController, gameId);
                try
                {
                    response.accumulate("dealerHand", handController.getCardsbyOwner("dealer", gameId));
                }
                catch (JSONException e)
                {
                    logger.error(e.toString());
                }
            }
            status = HttpStatus.OK;
        }
        try
        {
            response.accumulate("playerHand", handController.getCardsbyOwner(username, gameId));
            if (!response.has("dealerHand"))
                response.accumulate("dealerHand", handController.getCardsbyOwner("dealer", gameId).get(0).toString());// Not the best... it gives the card with the smallest value, always... but for our purposes.....
        }
        catch (JSONException e)
        {
            logger.error(e.toString());
        }

        return new ResponseEntity(response.toString(), headers, status);
    }

    public static ResponseEntity stand(String username, HandController handController, GameInfoController gameInfoController, long gameId)
    {
        logger.info(String.format("Player %s requests a stand", username));

        gameInfoController.setStandGame(gameId);

        JSONObject response = new JSONObject();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpStatus status = null;

        BlackJackGameController.stand(handController, gameId);

        try
        {
            response.accumulate("dealerHand", handController.getCardsbyOwner("dealer", gameId));
            response.accumulate("playerHand", handController.getCardsbyOwner(username, gameId));
        }
        catch (JSONException e)
        {
            logger.error(e.toString());
        }
        status = HttpStatus.OK;

        return new ResponseEntity(response.toString(), headers, status);
    }

    public static ResponseEntity end(String username,
                                     HandController handController,
                                     GameInfoController gameInfoController,
                                     PlayerController playerController,
                                     long gameId) {
        logger.info(String.format("Game %d ending", gameId));

        stand(username, handController, gameInfoController, gameId);

        JSONObject response = new JSONObject();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpStatus status = HttpStatus.OK;

        long playerValue = 0;
        for (long value : BlackJackGameController.value(username, handController, gameId))
        {
            if (value <= 21 && value > playerValue)
                playerValue = value;
        }

        long dealerValue = 0;
        for (long value : BlackJackGameController.value("dealer", handController, gameId))
        {
            if (value <= 21 && value > dealerValue)
                dealerValue = value;
        }

        logger.info(String.format("Player: %s, Dealer: %s", playerValue, dealerValue));
        if (playerValue > dealerValue)
        {
            logger.info("Player won.");
            BigDecimal playerFunds = (playerController.getFundsByUsername(username));
            BigDecimal bet = gameInfoController.getBetById(gameId);
            logger.info(String.format("Player funds: %d Bet: %d", playerFunds.intValue(), bet.intValue()));
            playerFunds = playerFunds.add(new BigDecimal(bet.intValue() * 2));
            logger.info(String.format("Player funds: %d", playerFunds.intValue()));
            playerController.setFundsByUsername(username, playerFunds);
        }

        try
        {
            response.accumulate("dealerHand", handController.getCardsbyOwner("dealer", gameId));
            response.accumulate("playerHand", handController.getCardsbyOwner(username, gameId));
        }
        catch (JSONException e)
        {
            logger.error(e.toString());
        }

        gameInfoController.deleteGameInfoById(gameId);

        BlackJackGameController.end(handController, gameId);

        return new ResponseEntity(response.toString(), headers, status);
    }
}
