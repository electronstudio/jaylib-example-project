package examples;


import static com.raylib.Colors.RAYWHITE;
import static com.raylib.Helpers.newRectangle;
import static com.raylib.Raylib.*;

public class Raygui {
    public static void main(String[] args) {
        int screenWidth = 800;
        int screenHeight = 600;
        InitWindow(screenWidth, screenHeight, "raygui");
        while (!WindowShouldClose()) {
            BeginDrawing();
            ClearBackground(RAYWHITE);
            GuiLabel(new Rectangle().x(120f).y(120f).width(100f).height(32f), "Hello world");

            EndDrawing();
        }
        CloseWindow();
    }
}
