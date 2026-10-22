package net.backtooltweak.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.backtooltweak.TweakRender;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/** Marca que o BackSlot esta desenhando o slot BELT (BeltSlotFeatureRenderer). */
@Pseudo
@Mixin(targets = "net.backslot.client.feature.BeltSlotFeatureRenderer")
public abstract class BeltRenderScopeMixin {
    @WrapMethod(method = "render(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;FFFFFF)V", remap = false)
    private void backtooltweak$scope(MatrixStack matrices, VertexConsumerProvider providers, int light,
                                     AbstractClientPlayerEntity player,
                                     float a, float b, float c, float d, float e, float f,
                                     Operation<Void> original) {
        TweakRender.Slot previous = TweakRender.SLOT;
        TweakRender.SLOT = TweakRender.Slot.BELT;
        try {
            original.call(matrices, providers, light, player, a, b, c, d, e, f);
        } finally {
            TweakRender.SLOT = previous;
        }
    }
}
