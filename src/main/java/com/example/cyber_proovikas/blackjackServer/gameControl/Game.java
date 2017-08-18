package com.example.cyber_proovikas.blackjackServer.gameControl;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
@Data
public class Game {
    @Id
    long id;

    BigDecimal bet;
    boolean stand;
}
