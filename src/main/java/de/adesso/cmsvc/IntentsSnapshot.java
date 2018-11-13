package de.adesso.cmsvc;

import org.springframework.data.annotation.Id;

import java.util.List;

public class IntentsSnapshot {

    @Id
    String id;

    List<IntentEntity> entities;
    List<IntentChange> changes;
    List<String> log;
    String author;

    public IntentsSnapshot(List<IntentEntity> entities, List<IntentChange> changes, List<String> log) {
        this.entities = entities;
        this.changes = changes;
        this.log = log;
        this.author = "Not used";
    }
}
