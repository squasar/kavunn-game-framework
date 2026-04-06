package ashwake;

enum AshwakeBenchmarkScenario {
    PROJECTILE_HELL("projectile-hell", "Projectile Hell Chamber"),
    BOSS_ARENA("boss-arena", "Boss Arena"),
    REWARD_CHAMBER("reward-chamber", "Reward Chamber");

    private final String cliName;
    private final String title;

    AshwakeBenchmarkScenario(String cliName, String title) {
        this.cliName = cliName;
        this.title = title;
    }

    public String cliName() {
        return this.cliName;
    }

    public String title() {
        return this.title;
    }

    public static AshwakeBenchmarkScenario parse(String value) {
        for (AshwakeBenchmarkScenario scenario : values()) {
            if (scenario.cliName.equalsIgnoreCase(value) || scenario.name().equalsIgnoreCase(value)) {
                return scenario;
            }
        }
        throw new IllegalArgumentException("Unknown Ashwake benchmark scenario: " + value);
    }
}
