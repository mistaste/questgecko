package eu.mistaste.questgecko.story;

import eu.mistaste.questgecko.entity.QuestNPC;
import eu.mistaste.questgecko.registry.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class StoryScene {
    private static final double CLEANUP_RADIUS = 50.0;
    private static final double WALK_SPEED = 0.25;
    private static final int REACTION_DELAY = 30;

    public static int stage = 0;
    public static QuestNPC npc1;
    public static QuestNPC npc2;

    public static void advance(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double y = player.getY();

        if (stage == 0) {
            List<QuestNPC> oldNpcs = level.getEntitiesOfClass(
                    QuestNPC.class,
                    player.getBoundingBox().inflate(CLEANUP_RADIUS)
            );
            oldNpcs.forEach(QuestNPC::discard);

            // todo вынести хардкод
            npc1 = spawnNpc(level, "Мистер Милис", player.getX() + 4, y, player.getZ(), 90.0F);
            npc2 = spawnNpc(level, "Олег", player.getX() - 4, y, player.getZ(), -90.0F);

            stage = 1;
            player.sendSystemMessage(Component.literal("Сцена 1: Мистер Милис и Олег готовы."));
        } else if (stage == 1) {
            if (!npcsAlive()) {
                reset();
                return;
            }

            double midX = (npc1.getX() + npc2.getX()) * 0.5;
            npc1.getNavigation().moveTo(midX + 1.0, y, player.getZ(), WALK_SPEED);
            npc2.getNavigation().moveTo(midX - 1.0, y, player.getZ(), WALK_SPEED);

            npc1.getLookControl().setLookAt(npc2, 30.0F, 30.0F);
            npc2.getLookControl().setLookAt(npc1, 30.0F, 30.0F);

            stage = 2;
            player.sendSystemMessage(Component.literal("Сцена 2: Собеседование начинается..."));
        } else if (stage == 2) {
            if (!npcsAlive()) {
                reset();
                return;
            }

            player.sendSystemMessage(Component.literal("<Мистер Милис> Олег, беру тебя кодером для сюжета. Потянешь?"));

            QuestNPC target = npc2;
            level.getServer().doRunTask(new TickTask(level.getServer().getTickCount() + REACTION_DELAY, () -> {
                if (target.isAlive()) {
                    target.triggerAnim("action", "distrust");
                }
            }));

            stage = 3;
        } else if (stage == 3) {
            if (!npcsAlive()) {
                reset();
                return;
            }

            player.sendSystemMessage(Component.literal("<Олег> Потяну, если ТЗ не написано на салфетке."));
            npc1.triggerAnim("action", "correction");
            stage = 4;
        } else if (stage == 4) {
            if (!npcsAlive()) {
                reset();
                return;
            }

            player.sendSystemMessage(Component.literal("<Мистер Милис> Отлично. Тогда не спорь с камерой и делай магию."));
            npc1.triggerAnim("action", "uncorrection");
            npc2.triggerAnim("action", "right");

            reset();
        }
    }

    private static QuestNPC spawnNpc(ServerLevel level, String name, double x, double y, double z, float yaw) {
        QuestNPC npc = ModEntities.QUEST_NPC.get().create(level);
        if (npc == null) {
            return null;
        }

        npc.setPos(x, y, z);
        npc.setYRot(yaw);
        npc.setYHeadRot(yaw);
        npc.setInvulnerable(true);
        npc.setCustomName(Component.literal(name));
        level.addFreshEntity(npc);

        return npc;
    }

    private static boolean npcsAlive() {
        return npc1 != null && npc1.isAlive() && npc2 != null && npc2.isAlive();
    }

    private static void reset() {
        stage = 0;
        npc1 = null;
        npc2 = null;
    }
}
