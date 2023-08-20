## Purpose and Function

Snapshot is a mod that intercepts the creation of the world to copy the contents of a template world instead.

This means that when the world is created, it will use the same world, same seed, same settings, every time.

This is useful for modpacks who provide a static world for the player, and the world must be generated the same every time.

## Usage

To use, copy the **CONTENTS** of your world into the `templates` folder within your Minecraft instance. Once copied and with the config setting `useTemplates` set to `true`, the world will be instantiated as your world each and every time you hit "Create World".

## Config settings:

Config can be found in 

`useTemplates`: If set to `true`, will use the template listed. Set to false if you don't want to generate the same world each and every time.

`useTemplateWorldSettings`: If set to `true`, will use the world settings set initially by the template. Set to false if you want to allow the player to customize their settings.

## Requirements:
owo-lib: >= 0.9.2