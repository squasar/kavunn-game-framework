package ashwake;

enum AshwakeRoomKind {
    COMBAT("Combat Chamber", "Break the ambush and move on."),
    REWARD("Reliquary", "Choose one blessing and shape the run."),
    EVENT("Silent Shrine", "Recover, gather, and prepare."),
    ELITE("Ash Crucible", "A dense elite clash with heavier pressure."),
    BOSS("Cinder Heart", "Survive the boss ritual and end the run.");

    private final String title;
    private final String description;

    AshwakeRoomKind(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() {
        return this.title;
    }

    public String description() {
        return this.description;
    }

    public boolean isCombatRoom() {
        return this == COMBAT || this == ELITE || this == BOSS;
    }
}
