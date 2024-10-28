# AdvancementsScreen Optimizer

> [!IMPORTANT]
> Mojang fixed this performance issue in 1.21.2+, so this mod is no longer required

Optimizes how the advancements screen is rendered

## How it works

Minecraft has to render a lot of textures when drawing the advancements screen. These include
 - the background
 - the icons
 - the lines

For each one it draws all the textures individually, requiring communication with the GPU each time.

The mod improves performance by batching most draw calls, so only a single one is made.
Furthermore, it avoids drawing textures which are outside the advancements screen.

### Before

![A picture with the mod disabled](https://i.imgur.com/AW8Mdrm.png)

### After

![A picture with the mod enabled](https://i.imgur.com/LYQ4vAa.png)