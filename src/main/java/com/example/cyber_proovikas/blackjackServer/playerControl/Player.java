package com.example.cyber_proovikas.blackjackServer.playerControl;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
@Entity
@Embeddable
public class Player {
    @Id
    String username;

    long game;
    BigDecimal funds;
}
