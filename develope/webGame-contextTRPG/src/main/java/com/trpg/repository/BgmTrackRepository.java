package com.trpg.repository;

import com.trpg.model.BgmTrack;
import com.trpg.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BgmTrackRepository extends JpaRepository<BgmTrack, Long> {

    List<BgmTrack> findByOwner(User owner);

    List<BgmTrack> findByIsPublicTrue();
}
