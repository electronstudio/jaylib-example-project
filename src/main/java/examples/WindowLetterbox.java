package examples;

import java.util.ArrayList;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

/**
 *   raylib [core] example - window letterbox</br>
 *   Example complexity rating: [★★☆☆] 2/4</br>
 *   Example originally created with raylib 2.5, last time updated with raylib 4.0</br>
 *   Example contributed by Anata (@anatagawa) and reviewed by Ramon Santamaria (@raysan5)</br>
 *   Example licensed under an unmodified zlib/libpng license, which is an OSI-certified,
 *   BSD-like license that allows static linking with closed source software</br>
 *   Copyright (c) 2019-2025 Anata (@anatagawa) and Ramon Santamaria (@raysan5)</br>
 *   Ported to Jaylib by Samir Rolemberg (@srolemberg)</br>
*/

@SuppressWarnings("resource")
public class WindowLetterbox {
    final static int screenWidth = 800;
    final static int screenHeight = 450;

    static void main() {
        // Enable config flags for resizable window and vertical synchro
        SetConfigFlags(FLAG_WINDOW_RESIZABLE | FLAG_VSYNC_HINT);
        InitWindow(screenWidth, screenHeight, "raylib [core] example - window letterbox");
        SetWindowMinSize(320, 240);

        int gameScreenWidth = 640;
        int gameScreenHeight = 480;

        // Render texture initialization, used to hold the rendering result so we can easily resize it
        RenderTexture target = LoadRenderTexture(gameScreenWidth, gameScreenHeight);
        SetTextureFilter(target.texture(), TEXTURE_FILTER_BILINEAR);  // Texture scale filter to use
        final ArrayList<Color> colors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            colors.add(new Color().r((byte) GetRandomValue(100, 250)).g((byte) GetRandomValue(50, 150)).b((byte) GetRandomValue(10, 100)).a((byte) 255));
        }
        SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        // Main game loop - Detect window close button or ESC key
        while (!WindowShouldClose()) {
            // Update
            //----------------------------------------------------------------------------------
            // Compute required framebuffer scaling
            final float scale = Math.min((float) GetScreenWidth() / gameScreenWidth, (float) GetScreenHeight() / gameScreenHeight);

            if (IsKeyPressed(KEY_SPACE)) {
                // Recalculate random colors for the bars
                colors.clear();
                for (int i = 0; i < 10; i++) {
                    colors.add(new Color().r((byte) GetRandomValue(100, 250)).g((byte) GetRandomValue(50, 150)).b((byte) GetRandomValue(10, 100)).a((byte) 255));
                }
            }

            // Update virtual mouse (clamped mouse value behind game screen)
            Vector2 mouse = GetMousePosition();
            Vector2 virtualMouse = new Vector2();
            virtualMouse.x((mouse.x() - (GetScreenWidth() - (gameScreenWidth * scale)) * 0.5f) / scale);
            virtualMouse.y((mouse.y() - (GetScreenHeight() - (gameScreenHeight * scale)) * 0.5f) / scale);
            virtualMouse = Vector2Clamp(virtualMouse, new Vector2(), new Vector2().x((float) gameScreenWidth).y((float) gameScreenHeight));

            // Apply the same transformation as the virtual mouse to the real mouse (i.e. to work with raygui)
            //SetMouseOffset(-(GetScreenWidth() - (gameScreenWidth*scale))*0.5f, -(GetScreenHeight() - (gameScreenHeight*scale))*0.5f);
            //SetMouseScale(1/scale, 1/scale);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            // Draw everything in the render texture, note this will not be rendered on screen, yet
            BeginTextureMode(target);
                ClearBackground(RAYWHITE);  // Clear render texture background color
                for (int i = 0; i < 10; i++) {
                    DrawRectangle(0, (gameScreenHeight / 10) * i, gameScreenWidth, gameScreenHeight / 10, colors.get(i));
                }
                DrawText("If executed inside a window,\nyou can resize the window,\nand see the screen scaling!", 10, 25, 20, WHITE);
                DrawText(TextFormat(String.format("Default Mouse: [%d , %d]", (int) mouse.x(), (int) mouse.y())), 350, 25, 20, GREEN);
                DrawText(TextFormat(String.format("Virtual Mouse: [%d , %d]", (int) virtualMouse.x(), (int) virtualMouse.y())), 350, 55, 20, YELLOW);
            EndTextureMode();

            BeginDrawing();
                ClearBackground(BLACK);     // Clear screen background

                // Draw render texture to screen, properly scaled
                DrawTexturePro(
                        target.texture(),
                        new Rectangle().x(0).y(0).width((float) target.texture().width()).height((float) -target.texture().height()),
                        new Rectangle().x((GetScreenWidth() - ((float) gameScreenWidth * scale)) * 0.5f).y((GetScreenHeight() - ((float) gameScreenHeight * scale)) * 0.5f).width((float) gameScreenWidth * scale).height((float) gameScreenHeight * scale),
                        new Vector2(),
                        0.0f
                        , WHITE
                );
            EndDrawing();
            //--------------------------------------------------------------------------------------

        }
        // De-Initialization
        //--------------------------------------------------------------------------------------
        UnloadRenderTexture(target);        // Unload render texture

        CloseWindow();                      // Close window and OpenGL context
        //--------------------------------------------------------------------------------------
    }
}
