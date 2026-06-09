package net.shelmarow.betterlockon.util;

import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class ArmatureUtil {

    public static Vec3 getJointWorldPosition(LivingEntityPatch<?> entityPatch, Joint joint,Pose pose, Vec3 localOffset) {
        Vec3 pos = entityPatch.getOriginal().position();
        return getVec3(entityPatch, joint, localOffset, pos, pose, 1F);
    }


    public static Vec3 getJointWorldPosition(LivingEntityPatch<?> entityPatch, Joint joint, Vec3 localOffset) {
        Vec3 pos = entityPatch.getOriginal().position();
        Pose pose = entityPatch.getAnimator().getPose(1F);
        return getVec3(entityPatch, joint, localOffset, pos, pose, 1F);
    }

    public static Vec3 getJointWorldPosition(LivingEntityPatch<?> entityPatch, Joint joint, Vec3 localOffset, float partialTicks) {
        Pose pose = entityPatch.getAnimator().getPose(partialTicks);
        Vec3 position = entityPatch.getOriginal().getPosition(partialTicks);
        return getVec3(entityPatch, joint, localOffset, position, pose, partialTicks);
    }


    private static Vec3 getVec3(LivingEntityPatch<?> entityPatch, Joint joint, Pose pose, float partialTicks) {
        Vec3 pos = entityPatch.getOriginal().getPosition(partialTicks);
        return getVec3(entityPatch, joint, Vec3.ZERO, pos, pose, partialTicks);
    }

    private static Vec3 getVec3(LivingEntityPatch<?> entityPatch, Joint joint, Vec3 localOffset, Vec3 pos, Pose pose, float partialTicks) {
        OpenMatrix4f modelTf = OpenMatrix4f.createTranslation((float) pos.x, (float) pos.y, (float) pos.z)
                .mulBack(OpenMatrix4f.createRotatorDeg(180.0F, Vec3f.Y_AXIS).mulBack(entityPatch.getModelMatrix(partialTicks)));
        OpenMatrix4f JointTf = new OpenMatrix4f(entityPatch.getArmature().getBoundTransformFor(pose, joint)).mulFront(modelTf);
        return OpenMatrix4f.transform(JointTf, localOffset);
    }

}
