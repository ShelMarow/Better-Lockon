package net.shelmarow.betterlockon.client.render.compat;

import net.minecraft.world.entity.LivingEntity;
import net.shelmarow.combat_evolution.ai.CEHumanoidPatch;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.combat_evolution.client.hud.execution.ExecutionHUD;
import net.shelmarow.combat_evolution.execution.ExecutionHandler;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class CombatEvolutionCompat {
    public static float isCEPatch(LivingEntityPatch<?> livingEntityPatch) {
        if (livingEntityPatch instanceof CEHumanoidPatch ceHumanoidPatch) {
            LivingEntity original = livingEntityPatch.getOriginal();
            if(original.getAttributes().hasAttribute(EpicFightAttributes.MAX_STAMINA.get())) {
                return CEPatchUtils.getStaminaPercent(ceHumanoidPatch);
            }
        }
        return -1F;
    }

    public static boolean canExecution(LivingEntityPatch<?> livingEntityPatch) {
//        if(livingEntityPatch != null){
//            AssetAccessor<? extends StaticAnimation> animation = livingEntityPatch.getAnimator().getPlayerFor(null).getRealAnimation();
//            return ExecutionHandler.isTargetGuardBreak(animation, livingEntityPatch);
//        }
        return false;
    }
}
