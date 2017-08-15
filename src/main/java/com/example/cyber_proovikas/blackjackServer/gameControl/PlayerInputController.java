package com.example.cyber_proovikas.blackjackServer.gameControl;

import com.example.cyber_proovikas.blackjackServer.playerControl.Player;
import com.example.cyber_proovikas.blackjackServer.playerControl.PlayerController;
import lombok.Data;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
public class PlayerInputController
{
    @Autowired
    PlayerController playerController;

    Logger logger = LoggerFactory.getLogger(PlayerInputController.class);

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity loginRequest(@RequestBody String body)
    {
        JSONObject request;
        String username = "";
        PlayerLoginResponse response = new PlayerLoginResponse();;

        try
        {
            request = new JSONObject(body);
            username = (String)request.get("username");
        }
        catch (JSONException e)
        {
            logger.error(e.toString());
        }

        if (username.isEmpty())
        {
            logger.warn("Did not get username from request body.");
            response.setStatus("Failed: No username specified.");
            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
        }

        logger.info(String.format("Got username: %s", username));
        Player player = playerController.getPlayerByUsername(username);

        if (player == null)
        {
            logger.info("Player not found in database. Creating a new one.");
            playerController.addPlayer(username);
            response.setStatus("Success: Player created.");
            return new ResponseEntity(response, HttpStatus.OK);
        }
        logger.info("Player found in database. Not creating a new one.");
        response.setStatus("Failure: Player already present.");

        return new ResponseEntity(response, HttpStatus.CONFLICT);
    }
}

@Data
class PlayerLoginResponse
{
    String status;
}