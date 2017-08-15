package com.example.cyber_proovikas.blackjackServer.gameControl;

import lombok.Data;
import lombok.Value;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class Hand {
    @Id @GeneratedValue
    long id;

    String owner;
    long card;
    long deck;
}
