package eu.mistaste.questgecko.client;

import eu.mistaste.questgecko.QuestGecko;
import eu.mistaste.questgecko.entity.QuestNPC;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = QuestGecko.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CinematicCamera {
    public static float CAM_SPEED = 0.015F;

    private static final double SEARCH_RADIUS = 32.0;
    private static final double WIDE_CAM_DIST = 4.8;
    private static final double CLOSE_CAM_DIST = 2.2;
    private static final int TRANSITION_TICKS = 30;

    public static ArmorStand cameraDummy;
    public static int stage;
    public static int ticks;
    public static int transitionTicks;
    public static boolean isActive;

    public static double sourceX;
    public static double sourceY;
    public static double sourceZ;
    public static float sourceYaw;
    public static float sourcePitch;

    public static void advanceStage() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || stage >= 5) {
            return;
        }

        stage++;
        ticks = 0;

        if (stage > 4) {
            stage = 5;
            transitionTicks = TRANSITION_TICKS;
            return;
        }

        if (!isActive) {
            cameraDummy = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
            cameraDummy.setNoGravity(true);
            cameraDummy.setInvisible(true);
            cameraDummy.noPhysics = true;
            cameraDummy.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());

            mc.setCameraEntity(cameraDummy);
            mc.player.setInvisible(true);
            isActive = true;
        }

        rememberStartPos();
        transitionTicks = 0;
    }

    public static void stop() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
            mc.player.setInvisible(false);
        }

        cameraDummy = null;
        isActive = false;
        stage = 0;
        ticks = 0;
        transitionTicks = 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isActive || cameraDummy == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            stop();
            return;
        }

        ticks++;
        if (stage == 5) {
            if (ticks > 40) {
                stop();
            }
            return;
        }

        // ждем 30 тиков пока камера долетит
        if (transitionTicks < TRANSITION_TICKS) {
            transitionTicks++;
        }

        List<QuestNPC> npcs = findNpcs(mc);
        QuestNPC npc1 = npcs.isEmpty() ? null : npcs.get(0);
        QuestNPC npc2 = npcs.size() < 2 ? npc1 : npcs.get(npcs.size() - 1);

        double centerX = centerX(npc1, npc2);
        double centerY = centerY(mc, npc1, npc2);
        double centerZ = centerZ(npc1, npc2);

        double camX = cameraDummy.getX();
        double camY = centerY + 1.15;
        double camZ = cameraDummy.getZ();
        double feetY = camY - cameraDummy.getEyeHeight();

        float yaw = cameraDummy.getYRot();
        float pitch = cameraDummy.getXRot();

        syncPreviousFrame();

        if (stage == 1) {
            double p = Math.min(1.0, ticks * CAM_SPEED);
            camX = Mth.lerp(p, centerX - 2.0, centerX);
            camZ = Mth.lerp(p, centerZ + 6.0, centerZ + WIDE_CAM_DIST);

            float[] angles = lookAt(camX, camY, camZ, centerX, centerY + 1.35, centerZ);
            yaw = angles[0];
            pitch = angles[1];
        } else if (stage == 2) {
            camX = centerX;
            camZ = centerZ + WIDE_CAM_DIST;

            float[] angles = lookAt(camX, camY, camZ, centerX, centerY + 1.35, centerZ);
            yaw = angles[0];
            pitch = angles[1];
        } else if (stage == 3) {
            QuestNPC focus = npc1;
            double[] pos = closePos(focus, centerX - 1.5, centerZ + 1.5);
            camX = pos[0];
            camZ = pos[1];

            float[] angles = lookAt(camX, camY, camZ, focusX(focus, centerX - 1.5), faceY(focus, centerY), focusZ(focus, centerZ));
            yaw = angles[0];
            pitch = angles[1];
        } else if (stage == 4) {
            QuestNPC focus = npc2;
            double[] pos = closePos(focus, centerX + 1.5, centerZ + 1.5);
            camX = pos[0];
            camZ = pos[1];

            float[] angles = lookAt(camX, camY, camZ, focusX(focus, centerX + 1.5), faceY(focus, centerY), focusZ(focus, centerZ));
            yaw = angles[0];
            pitch = angles[1];
        }

        // костыль для фикса дергания камеры
        if (transitionTicks < TRANSITION_TICKS) {
            float p = (float) transitionTicks / TRANSITION_TICKS;
            float smooth = (float) (1.0 - Math.pow(1.0 - p, 3));

            camX = Mth.lerp(smooth, sourceX, camX);
            feetY = Mth.lerp(smooth, sourceY, feetY);
            camZ = Mth.lerp(smooth, sourceZ, camZ);
            yaw = rotLerp(smooth, sourceYaw, yaw);
            pitch = Mth.lerp(smooth, sourcePitch, pitch);
        }

        cameraDummy.setPos(camX, feetY, camZ);
        cameraDummy.setYRot(yaw);
        cameraDummy.setXRot(pitch);
        cameraDummy.yHeadRot = yaw;
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (!isActive || cameraDummy == null) {
            return;
        }

        float partial = (float) event.getPartialTick();
        event.setPitch(Mth.lerp(partial, cameraDummy.xRotO, cameraDummy.getXRot()));
        event.setYaw(rotLerp(partial, cameraDummy.yRotO, cameraDummy.getYRot()));
        event.setRoll(0);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (isActive) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Pre event) {
        if (!isActive) {
            return;
        }

        String id = event.getOverlay().id().getPath();
        if (id.equals("crosshair") || id.equals("hotbar") || id.equals("experience_bar")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (isActive) {
            event.setResult(Event.Result.DENY);
        }
    }

    private static List<QuestNPC> findNpcs(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            return List.of();
        }

        return mc.level.getEntitiesOfClass(QuestNPC.class, mc.player.getBoundingBox().inflate(SEARCH_RADIUS), QuestNPC::isAlive)
                .stream()
                .sorted(Comparator.comparingDouble(QuestNPC::getX))
                .toList();
    }

    private static double[] closePos(QuestNPC npc, double fallbackX, double fallbackZ) {
        if (npc == null) {
            return new double[]{fallbackX, fallbackZ};
        }

        double yaw = npc.getYRot() * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin((float) yaw);
        double forwardZ = Mth.cos((float) yaw);
        double sideX = Mth.cos((float) yaw);
        double sideZ = Mth.sin((float) yaw);

        return new double[]{
                npc.getX() + forwardX * CLOSE_CAM_DIST + sideX * 1.15,
                npc.getZ() + forwardZ * CLOSE_CAM_DIST + sideZ * 1.15
        };
    }

    private static float[] lookAt(double camX, double camY, double camZ, double targetX, double targetY, double targetZ) {
        double dx = targetX - camX;
        double dy = targetY - camY;
        double dz = targetZ - camZ;

        double flat = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, flat) * Mth.RAD_TO_DEG);
        return new float[]{yaw, pitch};
    }

    private static float rotLerp(float p, float from, float to) {
        float delta = ((to - from) % 360.0F + 540.0F) % 360.0F - 180.0F;
        return from + p * delta;
    }

    private static void rememberStartPos() {
        if (cameraDummy == null) {
            return;
        }

        sourceX = cameraDummy.getX();
        sourceY = cameraDummy.getY();
        sourceZ = cameraDummy.getZ();
        sourceYaw = cameraDummy.getYRot();
        sourcePitch = cameraDummy.getXRot();
        syncPreviousFrame();
    }

    private static void syncPreviousFrame() {
        cameraDummy.xo = cameraDummy.getX();
        cameraDummy.yo = cameraDummy.getY();
        cameraDummy.zo = cameraDummy.getZ();
        cameraDummy.xRotO = cameraDummy.getXRot();
        cameraDummy.yRotO = cameraDummy.getYRot();
    }

    private static double centerX(QuestNPC npc1, QuestNPC npc2) {
        if (npc1 == null && npc2 == null) return 0.0;
        if (npc1 == null) return npc2.getX();
        if (npc2 == null) return npc1.getX();
        return (npc1.getX() + npc2.getX()) * 0.5;
    }

    private static double centerY(Minecraft mc, QuestNPC npc1, QuestNPC npc2) {
        if (npc1 == null && npc2 == null) return mc.player.getY();
        if (npc1 == null) return npc2.getY();
        if (npc2 == null) return npc1.getY();
        return (npc1.getY() + npc2.getY()) * 0.5;
    }

    private static double centerZ(QuestNPC npc1, QuestNPC npc2) {
        if (npc1 == null && npc2 == null) return 0.0;
        if (npc1 == null) return npc2.getZ();
        if (npc2 == null) return npc1.getZ();
        return (npc1.getZ() + npc2.getZ()) * 0.5;
    }

    private static double focusX(QuestNPC npc, double fallback) {
        return npc == null ? fallback : npc.getX();
    }

    private static double focusZ(QuestNPC npc, double fallback) {
        return npc == null ? fallback : npc.getZ();
    }

    private static double faceY(QuestNPC npc, double fallback) {
        return npc == null ? fallback + 1.65 : npc.getY() + npc.getBbHeight() * 0.86;
    }
}
