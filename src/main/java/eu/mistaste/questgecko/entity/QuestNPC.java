package eu.mistaste.questgecko.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class QuestNPC extends PathfinderMob implements GeoEntity {
    private static final String MOVEMENT = "movement";
    private static final String ACTION = "action";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("main");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walking");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public QuestNPC(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, MOVEMENT, 5, state ->
                state.setAndContinue(state.isMoving() ? WALK : IDLE)
        ));

        controllers.add(new AnimationController<>(this, ACTION, 0, state -> PlayState.STOP)
                .triggerableAnim("distrust", RawAnimation.begin().thenPlayAndHold("distrust"))
                .triggerableAnim("correction", RawAnimation.begin().thenPlayAndHold("correction"))
                .triggerableAnim("uncorrection", RawAnimation.begin().thenPlayAndHold("uncorrection"))
                .triggerableAnim("right", RawAnimation.begin().thenPlayAndHold("right"))
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
