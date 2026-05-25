package com.trpg.repository;

import com.trpg.model.CharacterSheet;
import com.trpg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterSheetRepository extends JpaRepository<CharacterSheet, Long> {

    List<CharacterSheet> findByOwner(User owner);

    Optional<CharacterSheet> findByOwnerAndId(User owner, Long id);

    List<CharacterSheet> findByIsPublicTrue();
}
