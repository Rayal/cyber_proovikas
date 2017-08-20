package com.example.cyber_proovikas.blackjackServer.playerControl;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
@Entity
@Embeddable
/*
 * Player Class that describes the SQL table of the same name.
 * Primary key : username (String)
 *
 * game (long) links to the game ID the player is currently playing. -1 means that there is no such game.
 * funds (BigDecimal) is the value of the player's funds at present.
 */
public class Player {
    @Id
    String username;

    long game;
    BigDecimal funds;
}
