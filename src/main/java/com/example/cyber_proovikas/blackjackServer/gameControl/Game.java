package com.example.cyber_proovikas.blackjackServer.gameControl;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Game {
    @Id
    long id;
    boolean stand;
}
