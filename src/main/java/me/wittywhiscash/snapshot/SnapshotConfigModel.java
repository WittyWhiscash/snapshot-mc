package me.wittywhiscash.snapshot;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "snapshot", wrapperName = "SnapshotConfig")
public class SnapshotConfigModel {
    public boolean useTemplates = true;
    public boolean useTemplateWorldSettings = true;
}
