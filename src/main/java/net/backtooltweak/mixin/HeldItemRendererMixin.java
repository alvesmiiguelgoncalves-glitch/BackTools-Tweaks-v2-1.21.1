package net.backtooltweak.mixin;

import net.backtooltweak.TweakConfig;
import net.backtooltweak.ItemTweak;
import net.backtooltweak.Pose;
import net.backtooltweak.TweakRender;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HeldItemRenderer.renderItem(LivingEntity, ItemStack, ModelTransformationMode, boolean, MatrixStack, VertexConsumerProvider, int)
 * Nome em intermediary (method_3233), igual ao que o BackSlot chama. Por isso remap = false.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    private static final String RENDER_ITEM =
            "method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V";

    @Inject(method = RENDER_ITEM, at = @At("HEAD"), cancellable = true, remap = false)
    private void backtooltweak$pre(LivingEntity entity, ItemStack stack, @Coerce Object mode, boolean left,
                                   MatrixStack matrices, VertexConsumerProvider providers, int light,
                                   CallbackInfo ci) {
        if (TweakRender.SLOT == TweakRender.Slot.NONE) return;
        ItemTweak it = TweakConfig.get(stack.getItem());
        if (it == null) return;
        Pose t = it.pose(TweakRender.SLOT == TweakRender.Slot.BELT);
        if (!t.visible) { ci.cancel(); return; }
        matrices.push();
        TweakRender.apply(matrices, t);
        TweakRender.PUSHED++;
    }

    @Inject(method = RENDER_ITEM, at = @At("RETURN"), remap = false)
    private void backtooltweak$post(LivingEntity entity, ItemStack stack, @Coerce Object mode, boolean left,
                                    MatrixStack matrices, VertexConsumerProvider providers, int light,
                                    CallbackInfo ci) {
        if (TweakRender.PUSHED > 0) {
            TweakRender.PUSHED--;
            matrices.pop();
        }
    }
}
