package com.trpg.repository;

import com.trpg.model.GameMap;
import com.trpg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameMapRepository extends JpaRepository<GameMap, Long> {

    List<GameMap> findByOwner(User owner);

    Optional<GameMap> findByOwnerAndId(User owner, Long id);

    List<GameMap> findByIsPublicTrue();
}
