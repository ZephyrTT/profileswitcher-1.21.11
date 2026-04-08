package com.profileswitcher.mixin;

import com.profileswitcher.ProfileSwitcherClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "squeek.appleskin.client.HudOverlayHandler", remap = false)
public class AppleSkinHudMixin {

    @Inject(method = "onHudRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void profileswitcher$cancelHud(CallbackInfo ci) {
        if (ProfileSwitcherClient.pvpMode) ci.cancel();
    }

    @Inject(method = "onTooltipRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void profileswitcher$cancelTooltip(CallbackInfo ci) {
        if (ProfileSwitcherClient.pvpMode) ci.cancel();
    }
}
