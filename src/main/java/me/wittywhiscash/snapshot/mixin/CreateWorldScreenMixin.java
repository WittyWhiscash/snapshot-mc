package me.wittywhiscash.snapshot.mixin;

import me.wittywhiscash.snapshot.Snapshot;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow
    private String levelName;

    @Shadow
    private boolean hardcore;

    @Shadow
    @Final
    public MoreOptionsDialog moreOptionsDialog;

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
                FileUtils.copyDirectory(templateDirectory, new File(gameDirectory + File.separator + "saves" + File.separator + this.levelName));
            } catch (IOException e) {
                Snapshot.LOGGER.error("The template world does not exist at " + templateDirectory, e);
                return;
            }

            try {
                LevelStorage.Session session = this.client.getLevelStorage().createSession(this.levelName);
                session.save(this.levelName);
                if (!Snapshot.CONFIG.useTemplateWorldSettings()) {
                    Snapshot.LOGGER.info("Replacing world data...");

                    GeneratorOptionsHolder optionsHolder = this.moreOptionsDialog.getGeneratorOptionsHolder(this.hardcore);
                    LevelInfo levelInfo = this.createLevelInfo(optionsHolder.generatorOptions().isDebugWorld());
                    SaveProperties properties = new LevelProperties(levelInfo, optionsHolder.generatorOptions(), optionsHolder.comp_617());

                    session.backupLevelDataFile(this.moreOptionsDialog.getRegistryManager(), properties);
                }

                session.close();
            } catch (IOException e) {
                SystemToast.addWorldAccessFailureToast(this.client, this.levelName);
                Snapshot.LOGGER.error("Failed to rename level " + this.levelName, e);
            }

            this.client.createIntegratedServerLoader().start(this, this.levelName);

            ci.cancel();
        }
    }

    @Shadow
    abstract LevelInfo createLevelInfo(boolean bl);

    private void queueLoadScreen() {
        this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
    }
}
