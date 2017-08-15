package com.example.cyber_proovikas.blackjackServer.playerControl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Repository
public interface PlayerController extends JpaRepository<Player, String>{
    @Modifying
    @Transactional
    @Query(value = "insert into Player (username) values (:username)", nativeQuery = true)
    void addPlayer(@Param("username") String username);

    @Query("select p from Player p where p.username = :username")
    Player getPlayerByUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Player p set p.funds = :funds where p.username = :username", nativeQuery = true)
    void setFundsByUsername(@Param("username") String username,@Param("funds") BigDecimal funds);

    @Query("select p.funds from Player p where p.username = :username")
    BigDecimal getFundsByUsername(@Param("username") String username);
}
