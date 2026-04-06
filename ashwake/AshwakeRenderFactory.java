package ashwake;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import core.PrimaryTypeValue;
import core.Relation;
import render.DefaultRenderingPipeline;
import render.Java2DRenderBackend;
import render.Java2DRenderTask;
import render.Matter;
import render.Planet;
import render.Universe;

final class AshwakeRenderFactory {

    private AshwakeRenderFactory() {
    }

    public static DefaultRenderingPipeline createJava2DPipeline() {
        DefaultRenderingPipeline pipeline = new DefaultRenderingPipeline();
        pipeline.addTask(new AshwakeBackdropTask());
        pipeline.addTask(new AshwakeArenaTask());
        pipeline.addTask(new AshwakeHazardTask());
        pipeline.addTask(new AshwakePickupTask());
        pipeline.addTask(new AshwakeActorTask());
        pipeline.addTask(new AshwakeProjectileTask());
        pipeline.addTask(new AshwakeOverlayTask());
        pipeline.addTask(new AshwakeHudTask());
        return pipeline;
    }

    static void configureGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static final class AshwakeBackdropTask extends Java2DRenderTask {

        @Override
        protected void execute(Java2DRenderBackend backend, Universe universe, Planet planet, Matter matter) {
            if (!AshwakeRenderSupport.hasLayer(matter, "backdrop") || !(planet instanceof AshwakeRunWorld world)) {
                return;
            }

            Graphics2D g2 = backend.getGraphics();
            int width = world.getRoomWorld().getWidth();
            int height = world.getRoomWorld().getHeight();

            g2.setPaint(new GradientPaint(0, 0, new Color(18, 11, 20), 0, height, new Color(47, 21, 18)));
            g2.fillRect(0, 0, width, height);

            g2.setColor(new Color(255, 164, 111, 32));
            for (int index = 0; index < 28; index++) {
                double phase = world.getElapsedTime() * 0.42 + index * 0.37;
                double x = (index * 37 + (Math.sin(phase) * 24.0)) % width;
                double y = (index * 23 + (Math.cos(phase * 1.3) * 28.0) + 40.0) % height;
                double size = 4.0 + ((index % 5) * 1.8);
                g2.fill(new Ellipse2D.Double(x, y, size, size));
            }
        }
    }

    private static final class AshwakeArenaTask extends Java2DRenderTask {

        @Override
        protected void execute(Java2DRenderBackend backend, Universe universe, Planet planet, Matter matter) {
            if (!AshwakeRenderSupport.hasLayer(matter, "arena") || !(planet instanceof AshwakeRunWorld world)) {
                return;
            }

            Graphics2D g2 = backend.getGraphics();
            int width = world.getRoomWorld().getWidth();
            int height = world.getRoomWorld().getHeight();

            g2.setColor(new Color(12, 10, 15, 165));
            g2.fillRoundRect(20, 20, width - 40, height - 40, 36, 36);

            Color top = switch (world.getRoomWorld().getRoomKind()) {
                case REWARD -> new Color(53, 34, 26);
                case EVENT -> new Color(27, 35, 34);
                case ELITE -> new Color(46, 19, 22);
                case BOSS -> new Color(52, 16, 17);
                default -> new Color(28, 22, 23);
            };
            Color bottom = switch (world.getRoomWorld().getRoomKind()) {
                case REWARD -> new Color(88, 53, 34);
                case EVENT -> new Color(31, 59, 53);
                case ELITE -> new Color(76, 26, 30);
                case BOSS -> new Color(95, 30, 24);
                default -> new Color(56, 32, 24);
            };

            g2.setPaint(new GradientPaint(0, 36, top, 0, height - 36, bottom));
            g2.fillRoundRect(44, 44, width - 88, height - 88, 34, 34);

            g2.setColor(new Color(255, 220, 180, 22));
            for (int row = 0; row < 10; row++) {
                int y = 70 + row * 52;
                g2.drawLine(74, y, width - 74, y);
            }
            for (int col = 0; col < 15; col++) {
                int x = 74 + col * 54;
                g2.drawLine(x, 70, x, height - 70);
            }

            g2.setColor(new Color(240, 192, 128, 54));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(44, 44, width - 88, height - 88, 34, 34);

            if (world.isAwaitingRoomAdvance() && !world.isVictory()) {
                g2.setColor(new Color(244, 196, 120, 160));
                g2.fillRoundRect(width / 2 - 56, 36, 112, 18, 18, 18);
            }
        }
    }

    private static final class AshwakeHazardTask extends Java2DRenderTask {

        @Override
        protected void execute(Java2DRenderBackend backend, Universe universe, Planet planet, Matter matter) {
            if (!AshwakeRenderSupport.hasLayer(matter, "hazards") || !(planet instanceof AshwakeRunWorld world)) {
                return;
            }

            Graphics2D g2 = backend.getGraphics();
            for (AshwakeHazard hazard : world.getRoomWorld().getHazards()) {
                Color outer = hazard.isFriendly() ? new Color(255, 176, 101, 58) : new Color(184, 72, 82, 64);
                Color inner = hazard.isFriendly() ? new Color(255, 198, 126, 115) : new Color(222, 92, 98, 118);
                double pulse = 0.88 + (0.08 * Math.sin(hazard.getPulsePhase()));
                double size = hazard.getRadius() * 2.0 * pulse;
                double x = hazard.getX() - size / 2.0;
                double y = hazard.getY() - size / 2.0;
                g2.setColor(outer);
                g2.fill(new Ellipse2D.Double(x, y, size, size));
                g2.setColor(inner);
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new Ellipse2D.Double(hazard.getX() - hazard.getRadius(), hazard.getY() - hazard.getRadius(), hazard.getRadius() * 2.0, hazard.getRadius() * 2.0));
            }
        }
    }

    private static final class AshwakePickupTask extends Java2DRenderTask {

        @Override
        protected void execute(Java2DRenderBackend backend, Universe universe, Planet planet, Matter matter) {
            if (!AshwakeRenderSupport.hasLayer(matter, "pickups") || !(planet instanceof AshwakeRunWorld world)) {
                return;
            }

            Graphics2D g2 = backend.getGraphics();
            for (AshwakePickup pickup : world.getRoomWorld().getPickups()) {
                double centerY = pickup.getY() + pickup.getBobOffset();
                Color glow = switch (pickup.getType()) {
                    case HEALTH_ORB -> new Color(121, 255, 162, 82);
                    case ENERGY_BLOOM -> new Color(129, 205, 255, 82);
                    case EMBER_SHARD -> new Color(255, 192, 116, 76);
                };
                Color fill = switch (pickup.getType()) {
                    case HEALTH_ORB -> new Color(124, 242, 155);
                    case ENERGY_BLOOM -> new Color(113, 196, 255);
                    case EMBER_SHARD -> new Color(255, 180, 96);
                };
                double glowSize = pickup.getRadius() * 2.9;
                g2.setColor(glow);
                g2.fill(new Ellipse2D.Double(pickup.getX() - glowSize / 2.0, centerY - glowSize / 2.0, glowSize, glowSize));
                g2.setColor(fill);
                g2.fill(new Ellipse2D.Double(
                    pickup.getX() - pickup.getRadius(),
                    centerY - pickup.getRadius(),
                    pickup.getRadius() * 2.0,
                    pickup.getRadius() * 2.0
                ));
            }
        }
    }

    private static final class AshwakeActorTask extends Java2DRenderTask {

        @Override
        protected void execute(Java2DRenderBackend backend, Universe universe, Planet planet, Matter matter) {
            if (!AshwakeRenderSupport.hasLayer(matter, "actors") || !(planet instanceof AshwakeRunWorld world)) {
                return;
            }

            Graphics2D g2 = backend.getGraphics();

            List<Relation> actors = new ArrayList<>(world.getRoomWorld().getEnemies());
            actors.add(world.getPlayer());
            actors.sort(Comparator.comparingDouble(AshwakeRenderSupport::sortBias));

            for (Relation actor : actors) {
                if (actor instanceof AshwakeEnemy enemy) {
                    drawEnemy(g2, enemy);
                } else if (actor instanceof AshwakePlayer player) {
                    drawPlayer(g2, player);
                }
            }
        }

        private void drawEnemy(Graphics2D g2, AshwakeEnemy enemy) {
            String animationState = AshwakeRenderSupport.entityString(enemy, "animationState", "move");
            Color fill = switch (enemy.getKind()) {
                case CHASER -> new Color(214, 110, 92);
                case CASTER -> new Color(172, 106, 220);
                case DASH_STRIKER -> new Color(242, 146, 82);
                case AREA_SEEDER -> new Color(92, 198, 156);
                case SUMMONER -> new Color(96, 164, 222);
                case BOSS -> new Color(231, 84, 74);
            };

            if ("telegraph".equals(animationState)) {
                fill = fill.brighter();
            }

            double radius = enemy.getRadius();
            double x = enemy.getX() - radius;
            double y = enemy.getY() - radius;

            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillOval((int) (x - 3), (int) (enemy.getY() + radius * 0.5), (int) (radius * 2.0 + 6), 10);
            g2.setColor(fill);
            g2.fillOval((int) x, (int) y, (int) (radius * 2.0), (int) (radius * 2.0));
            g2.setColor(new Color(255, 255, 255, 90));
            g2.drawOval((int) x, (int) y, (int) (radius * 2.0), (int) (radius * 2.0));

            if (enemy.getKind() == AshwakeEnemy.Kind.BOSS || enemy.getMaxHealth() > 40.0) {
                int barWidth = (int) (radius * 2.4);
                int barX = (int) (enemy.getX() - barWidth / 2.0);
                int barY = (int) (enemy.getY() - radius - 10.0);
                g2.setColor(new Color(22, 13, 17, 220));
                g2.fillRoundRect(barX, barY, barWidth, 6, 6, 6);
                g2.setColor(new Color(244, 123, 102));
                g2.fillRoundRect(barX, barY, (int) (barWidth * (enemy.getHealth() / enemy.getMaxHealth())), 6, 6, 6);
            }
        }

        private void drawPlayer(Graphics2D g2, AshwakePlayer player) {
            double radius = player.getRadius();
            double x = player.getX() - radius;
            double y = player.getY() - radius;

            g2.setColor(new Color(0, 0, 0, 82));
            g2.fillOval((int) (x - 2), (int) (player.getY() + radius * 0.54), (int) (radius * 2.0 + 4), 10);
            g2.setColor(new Color(246, 215, 133));
            g2.fillOval((int) x, (int) y, (int) (radius * 2.0), (int) (radius * 2.0));
            g2.setColor(new Color(255, 245, 214, 185));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval((int) x, (int) y, (int) (radius * 2.0), (int) (radius * 2.0));

            double facingX = player.getFacingX() * (radius + 7.0);
            double facingY = player.getFacingY() * (radius + 7.0);
            g2.setColor(new Color(255, 165, 88, 175));
            g2.drawLine((int) player.getX(), (int) player.getY(), (int) (player.getX() + facingX), (int) (player.getY() + facingY));

            if (player.hasWardAura()) {
                g2.setColor(new Color(121, 219, 204, 34));
                double size = player.getAuraRadius() * 2.0;
                g2.fill(new Ellipse2D.Double(player.getX() - player.getAuraRadius(), player.getY() - player.getAuraRadius(), size, size));
            }
        }
    }

    private static final class AshwakeProjectileTask extends Java2DRenderTask {

        @Override
        protected void execute(Java2DRenderBackend backend, Universe universe, Planet planet, Matter matter) {
            if (!AshwakeRenderSupport.hasLayer(matter, "projectiles") || !(planet instanceof AshwakeRunWorld world)) {
                return;
            }

            Graphics2D g2 = backend.getGraphics();
            for (AshwakeProjectile projectile : world.getRoomWorld().getProjectiles()) {
                Color fill = switch (projectile.getKind()) {
                    case EMBER_BOLT -> new Color(255, 191, 112);
                    case CURSE_ORB -> new Color(203, 123, 244);
                    case SHADOW_NEEDLE -> new Color(168, 151, 232);
                    case PULSE_SPARK -> new Color(120, 220, 255);
                    case BOSS_COMET -> new Color(255, 116, 88);
                };
                double radius = projectile.getRadius();
                g2.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 70));
                g2.fill(new Ellipse2D.Double(projectile.getX() - radius * 1.55, projectile.getY() - radius * 1.55, radius * 3.1, radius * 3.1));
                g2.setColor(fill);
                g2.fill(new Ellipse2D.Double(projectile.getX() - radius, projectile.getY() - radius, radius * 2.0, radius * 2.0));
            }
        }
    }

    private static final class AshwakeOverlayTask extends Java2DRenderTask {

        @Override
        protected void execute(Java2DRenderBackend backend, Universe universe, Planet planet, Matter matter) {
            if (!AshwakeRenderSupport.hasLayer(matter, "overlay") || !(planet instanceof AshwakeRunWorld world)) {
                return;
            }

            Graphics2D g2 = backend.getGraphics();

            if (world.isChoosingModifier()) {
                drawModifierOverlay(g2, world);
            }

            if (world.isPaused() || world.isRunOver() || world.isVictory()) {
                g2.setColor(new Color(8, 10, 16, 180));
                g2.fillRoundRect(210, 180, 540, 220, 28, 28);

                g2.setColor(new Color(248, 243, 229));
                g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 34));
                String title = world.isVictory() ? "Ashwake Complete" : world.isRunOver() ? "Run Lost" : "Paused";
                g2.drawString(title, 260, 245);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                g2.setColor(new Color(215, 214, 207));
                String subtitle = world.isVictory()
                    ? "The ritual engine held under pressure."
                    : world.isRunOver()
                        ? "Reset and pressure the framework again."
                        : "Press P to resume the chamber.";
                g2.drawString(subtitle, 260, 286);
            }
        }

        private void drawModifierOverlay(Graphics2D g2, AshwakeRunWorld world) {
            g2.setColor(new Color(9, 10, 16, 168));
            g2.fillRect(0, 0, world.getRoomWorld().getWidth(), world.getRoomWorld().getHeight());

            g2.setColor(new Color(242, 237, 225));
            g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 30));
            g2.drawString("Choose a Blessing", 334, 146);

            List<AshwakeModifier> options = world.getOfferedModifiers();
            for (int index = 0; index < options.size(); index++) {
                AshwakeModifier modifier = options.get(index);
                int x = 120 + index * 240;
                int y = 196;
                g2.setColor(new Color(22, 18, 25, 220));
                g2.fillRoundRect(x, y, 200, 220, 24, 24);
                g2.setColor(new Color(255, 180, 112, 180));
                g2.drawRoundRect(x, y, 200, 220, 24, 24);

                g2.setColor(new Color(255, 217, 176));
                g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
                g2.drawString(String.valueOf(index + 1), x + 20, y + 34);

                g2.setColor(new Color(245, 239, 228));
                g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
                g2.drawString(modifier.title(), x + 20, y + 78);

                g2.setColor(new Color(212, 208, 201));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                drawWrapped(g2, modifier.description(), x + 20, y + 112, 164, 22);
            }
        }
    }

    private static final class AshwakeHudTask extends Java2DRenderTask {

        @Override
        protected void execute(Java2DRenderBackend backend, Universe universe, Planet planet, Matter matter) {
            if (!AshwakeRenderSupport.hasLayer(matter, "hud") || !(planet instanceof AshwakeRunWorld world)) {
                return;
            }

            Graphics2D g2 = backend.getGraphics();
            int width = world.getRoomWorld().getWidth();

            g2.setColor(new Color(10, 10, 16, 182));
            g2.fillRoundRect(18, 16, width - 36, 84, 24, 24);

            g2.setColor(new Color(245, 239, 229));
            g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
            g2.drawString("Ashwake", 34, 50);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            g2.setColor(new Color(205, 202, 196));
            g2.drawString(world.getRoomTitle() + "  " + world.getCurrentRoomNumber() + "/" + world.getTotalRooms(), 36, 74);
            g2.drawString("Kills " + world.getKills() + "   Essence " + world.getEssence() + "   Score " + world.getScore(), 278, 74);

            drawBar(g2, 670, 30, 220, 12, world.getPlayer().getHealth() / world.getPlayer().getMaxHealth(), new Color(221, 96, 92), "HP");
            drawBar(g2, 670, 50, 220, 10, world.getPlayer().getWard() / world.getPlayer().getMaxWard(), new Color(114, 202, 186), "Ward");
            drawBar(g2, 670, 67, 220, 10, world.getPlayer().getEnergy() / world.getPlayer().getMaxEnergy(), new Color(112, 177, 245), "Energy");

            g2.setColor(new Color(245, 233, 215, 210));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2.drawString("Move WASD  Aim/Fire Arrows  Dash Shift/Space  Secondary Q  Pause P  Advance E", 24, 622);

            if (!world.getActiveModifiers().isEmpty()) {
                g2.setColor(new Color(12, 10, 16, 160));
                g2.fillRoundRect(width - 250, 112, 220, 180, 20, 20);
                g2.setColor(new Color(246, 228, 196));
                g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
                g2.drawString("Run Modifiers", width - 226, 140);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                int y = 164;
                for (AshwakeModifier modifier : world.getActiveModifiers()) {
                    g2.drawString("- " + modifier.title(), width - 226, y);
                    y += 20;
                }
            }

            if (!world.getStatusLine().isBlank()) {
                g2.setColor(new Color(10, 10, 16, world.getBannerTime() > 0.0 ? 196 : 136));
                g2.fillRoundRect(250, 108, 460, 34, 20, 20);
                g2.setColor(new Color(244, 229, 205));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                g2.drawString(world.getStatusLine(), 272, 130);
            }
        }

        private void drawBar(Graphics2D g2, int x, int y, int width, int height, double fillRatio, Color fill, String label) {
            g2.setColor(new Color(20, 18, 24, 210));
            g2.fillRoundRect(x, y, width, height, height, height);
            g2.setColor(fill);
            g2.fillRoundRect(x, y, (int) (width * Math.max(0.0, Math.min(1.0, fillRatio))), height, height, height);
            g2.setColor(new Color(240, 238, 232));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(label, x - 36, y + height);
        }
    }

    private static void drawWrapped(Graphics2D g2, String text, int x, int y, int width, int lineHeight) {
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        int drawY = y;

        for (String word : words) {
            String trial = current.isEmpty() ? word : current + " " + word;
            if (g2.getFontMetrics().stringWidth(trial) > width) {
                g2.drawString(current.toString(), x, drawY);
                current = new StringBuilder(word);
                drawY += lineHeight;
            } else {
                current = new StringBuilder(trial);
            }
        }

        if (!current.isEmpty()) {
            g2.drawString(current.toString(), x, drawY);
        }
    }

    private static final class AshwakeRenderSupport {

        private AshwakeRenderSupport() {
        }

        static boolean hasLayer(Matter matter, String expectedLayer) {
            return expectedLayer.equals(matterString(matter, "layerHint", ""));
        }

        static String matterString(Matter matter, String key, String fallback) {
            return paramsString(matter.getRenderParam(key), fallback);
        }

        static String entityString(core.Params params, String key, String fallback) {
            Object value = entityValue(params, key);
            return value instanceof String string ? string : fallback;
        }

        static double sortBias(Relation relation) {
            if (relation instanceof core.Params params) {
                Object value = entityValue(params, "sortBias");
                if (value instanceof Number number) {
                    return number.doubleValue();
                }
            }
            return 0.0;
        }

        private static Object entityValue(core.Params params, String key) {
            if (params.get(key) instanceof PrimaryTypeValue<?> value) {
                return value.getValue();
            }
            return null;
        }

        private static String paramsString(core.Params params, String fallback) {
            if (params == null) {
                return fallback;
            }
            Object value = entityValue(params, "value");
            return value instanceof String string ? string : fallback;
        }
    }
}
