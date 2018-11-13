package de.adesso.cmsvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
class IntentsUtil {

    private final
    IntentsRepository intentsRepository;

    @Autowired
    public IntentsUtil(IntentsRepository intentsRepository) {
        this.intentsRepository = intentsRepository;
    }

    void applyChanges(List<IntentChange> intentChanges) {
        List<String> log = new ArrayList<>();

        IntentsSnapshot latest = intentsRepository.findFirstByOrderByIdDesc();
        Map<String, IntentEntity> intentEntities = latest.entities.stream().collect(Collectors.toMap(i -> i.intent, i -> i));

        for (IntentChange intentChange : intentChanges) {
            IntentEntity intentEntity = intentEntities.computeIfAbsent(intentChange.intent,
                    (__) -> new IntentEntity(intentChange.intent, new ArrayList<>()));

            if (intentChange.type == IntentChange.Type.Addition) {
                if (intentEntity.examples.contains(intentChange.example)) {
                    log.add("\"" + intentChange.intent + ":" + intentChange.example + "\" already exists");
                } else {
                    intentEntity.examples.add(intentChange.example);
                }
            } else if (intentChange.type == IntentChange.Type.Removal) {
                if (intentEntity.examples.contains(intentChange.example)) {
                    intentEntity.examples.remove(intentChange.example);
                } else {
                    log.add("\"" + intentChange.intent + ":" + intentChange.example + "\" was already removed");
                }
            }
        }

        intentsRepository.insert(new IntentsSnapshot(new ArrayList<>(intentEntities.values()), intentChanges, log));
    }
}
