package net.shelmarow.betterlockon.client.render.compat;

import net.shelmarow.combat_evolution.ai.CEHumanoidPatch;
import net.shelmarow.combat_evolution.ai.util.CEPatchUtils;
import net.shelmarow.combat_evolution.client.hud.execution.ExecutionHUD;
import net.shelmarow.combat_evolution.execution.ExecutionHandler;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CombatEvolutionCompat {
    public static float isCEPatch(LivingEntityPatch<?> livingEntityPatch) {
        if (livingEntityPatch instanceof CEHumanoidPatch<?> ceHumanoidPatch) {
            return CEPatchUtils.getStaminaPercent(ceHumanoidPatch);
        }
        return -1F;
    }

    public static boolean canExecution(LivingEntityPatch<?> livingEntityPatch) {
        if(livingEntityPatch != null){
            return ExecutionHUD.isShowExecutionIcon();
        }
        return false;
    }
}
