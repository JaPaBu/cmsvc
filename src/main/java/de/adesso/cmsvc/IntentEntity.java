package de.adesso.cmsvc;

import java.util.List;

class IntentEntity {

    String intent;

    List<String> examples;

    IntentEntity(String intent, List<String> examples) {
        this.intent = intent;
        this.examples = examples;
    }
}
