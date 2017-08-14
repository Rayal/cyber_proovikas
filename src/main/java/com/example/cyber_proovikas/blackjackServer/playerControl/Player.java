package com.example.cyber_proovikas.blackjackServer.playerControl;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
@Entity
public class Player {
    @Id
    String username;

    String name;
    BigDecimal funds;
}
