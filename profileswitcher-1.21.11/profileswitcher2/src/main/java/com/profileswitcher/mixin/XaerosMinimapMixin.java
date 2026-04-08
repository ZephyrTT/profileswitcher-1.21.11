package com.profileswitcher.mixin;

import com.profileswitcher.ProfileSwitcherClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "xaero.minimap.element.render.GuiMinimapElement", remap = false)
public class XaerosMinimapMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void profileswitcher$cancelMinimap(CallbackInfo ci) {
        if (ProfileSwitcherClient.pvpMode) ci.cancel();
    }
}
