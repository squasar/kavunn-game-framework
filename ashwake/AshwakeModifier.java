package ashwake;

import java.util.List;

enum AshwakeModifier {
    SPLIT_SHOT("Split Shot", "Basic fire emits two angled side bolts."),
    PIERCING_SIGIL("Piercing Sigil", "Projectiles can push through extra targets."),
    SHOCK_DASH("Shock Dash", "Dash leaves a damaging ember ring."),
    EMBER_BLOOM("Ember Bloom", "Defeated enemies drop more ember shards."),
    WARD_AURA("Ward Aura", "A slow-burning aura damages nearby threats."),
    RITUAL_VELOCITY("Ritual Velocity", "Attack rate and projectile speed increase."),
    CRITICAL_ASH("Critical Ash", "Critical chance rises and hits spike harder."),
    CHAINBURST("Chainburst", "Every fifth attack erupts into a radial burst.");

    private final String title;
    private final String description;

    AshwakeModifier(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() {
        return this.title;
    }

    public String description() {
        return this.description;
    }

    public void apply(AshwakePlayer player) {
        switch (this) {
            case SPLIT_SHOT -> player.addSplitShots(2);
            case PIERCING_SIGIL -> player.addProjectilePierce(1);
            case SHOCK_DASH -> player.enableShockDash();
            case EMBER_BLOOM -> player.enableEmberBloom();
            case WARD_AURA -> player.enableWardAura(96.0, 14.0);
            case RITUAL_VELOCITY -> {
                player.multiplyAttackInterval(0.82);
                player.multiplyProjectileSpeed(1.16);
                player.multiplyMoveSpeed(1.06);
            }
            case CRITICAL_ASH -> player.addCritChance(0.18);
            case CHAINBURST -> player.enableChainburst();
        }
    }

    public static List<AshwakeModifier> all() {
        return List.of(values());
    }
}
