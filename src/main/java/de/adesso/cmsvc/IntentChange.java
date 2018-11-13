package de.adesso.cmsvc;

class IntentChange {
    Type type;
    String intent;
    String example;
    IntentChange(Type type, String intent, String example) {
        this.type = type;
        this.intent = intent;
        this.example = example;
    }

    enum Type {
        Addition,
        Removal
    }
}
