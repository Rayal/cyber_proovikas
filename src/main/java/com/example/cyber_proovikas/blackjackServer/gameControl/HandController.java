package com.example.cyber_proovikas.blackjackServer.gameControl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface HandController extends JpaRepository<Hand,Long>{
    @Query("select h from Hand h where h.owner = :owner")
    List<Hand> getHandbyOwner(@Param("owner") String owner);

    @Query("select h.card from Hand h where h.deck = :deck")
    List<Long> getCardsbyDeck(@Param("deck") long deck);

    @Query("select h.card from Hand h where h.owner = :owner")
    List<Long> getCardsbyOwner(@Param("owner") String owner);

    @Modifying
    @Transactional
    @Query(value = "insert into Hand (owner, card, deck) values (:owner, :card, :gameId)", nativeQuery = true)
    void insertCardToHand(@Param("owner") String owner, @Param("card") long card, @Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(value = "update Hand h set h.owner= :owner where h.deck = :gameId", nativeQuery = true)
    void moveCardToHand(@Param("owner") String owner, @Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(value = "delete from Hand h where h.deck = :gameId")
    void cleanGameDeck(@Param("gameId") long gameId);
}
