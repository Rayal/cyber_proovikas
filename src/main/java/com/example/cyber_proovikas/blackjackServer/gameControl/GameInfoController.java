package com.example.cyber_proovikas.blackjackServer.gameControl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface GameInfoController extends JpaRepository <Game, Long> {
    @Modifying
    @Transactional
    @Query(value = "insert into Game (id, stand) values (:gameId, FALSE)", nativeQuery = true)
    public void insertGame(@Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(value = "update Game g set g.stand = TRUE where g.id = :gameId")
    public void setStandGame(@Param("gameId") long gameId);

    @Query("select g.stand from Game g where g.id = :gameId")
    public boolean getStandGame(@Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(value = "delete from Game g where g.id = :gameId", nativeQuery = true)
    public void deleteGameInfoById(@Param("gameId") long gameId);

}
