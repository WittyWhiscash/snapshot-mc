package me.wittywhiscash.snapshot.mixin;

import me.wittywhiscash.snapshot.Snapshot;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    @Shadow
    @Final
    public WorldCreator worldCreator;

    protected CreateWorldScreenMixin(Text component) {
        super(component);
    }

    @Inject(method = "createLevel", at = @At(value = "HEAD"), cancellable = true)
    private void createLevel(CallbackInfo ci) {
        if (Snapshot.CONFIG.useTemplates()) {
            File gameDirectory = this.client.runDirectory.getAbsoluteFile();
            File templateDirectory = new File(gameDirectory + File.separator + Snapshot.TEMPLATE_DIRECTORY);

            this.queueLoadScreen();

            try {
                FileUtils.copyDirectory(templateDirectory, new File(gameDirectory + File.separator + "saves" + File.separator + this.worldCreator.getWorldName()));
            } catch (IOException e) {
                Snapshot.LOGGER.error("The template world does not exist at " + templateDirectory, e);
                return;
            }

            try {
                LevelStorage.Session session = this.client.getLevelStorage().createSession(this.worldCreator.getWorldName());
                session.save(this.worldCreator.getWorldName());
                if (!Snapshot.CONFIG.useTemplateWorldSettings()) {
                    Snapshot.LOGGER.info("Replacing world data...");

                    GeneratorOptionsHolder optionsHolder = this.worldCreator.getGeneratorOptionsHolder();
                    boolean isFlat = Optional
                            .of(this.worldCreator.getWorldType().comp_1238().matches(preset -> preset == WorldPresets.FLAT))
                            .orElse(false);
                    boolean isDebug = optionsHolder.comp_1028().isDebug();

                    LevelInfo levelInfo = this.createLevelInfo(isDebug);
                    LevelProperties.SpecialProperty specialProperty = isDebug ? LevelProperties.SpecialProperty.DEBUG : isFlat ? LevelProperties.SpecialProperty.FLAT : LevelProperties.SpecialProperty.NONE;
                    SaveProperties properties = new LevelProperties(levelInfo, optionsHolder.generatorOptions(), specialProperty, optionsHolder.dimensionOptionsRegistry().getLifecycle());

                    session.backupLevelDataFile(optionsHolder.getCombinedRegistryManager(), properties);
                }

                session.close();
            } catch (IOException e) {
                SystemToast.addWorldAccessFailureToast(this.client, this.worldCreator.getWorldName());
                Snapshot.LOGGER.error("Failed to rename level " + this.worldCreator.getWorldName(), e);
            }

            this.client.createIntegratedServerLoader().start(this, this.worldCreator.getWorldName());

            ci.cancel();
        }
    }

    @Shadow
    abstract LevelInfo createLevelInfo(boolean bl);

    private void queueLoadScreen() {
        this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
    }
}
