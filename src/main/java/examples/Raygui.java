package examples;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import static com.raylib.Jaylib.*;


public class Raygui {
    public static void main(String args[]) {
        // Initialization
        //---------------------------------------------------------------------------------------
        int screenWidth = 800;
        int screenHeight = 600;

        SetConfigFlags(FLAG_WINDOW_UNDECORATED);
        InitWindow(screenWidth, screenHeight, "raygui - portable window");

        // General variables
        Raylib.Vector2 mousePosition = new Jaylib.Vector2();
        Raylib.Vector2 windowPosition = new Jaylib.Vector2(300, 300);
        Raylib.Vector2 panOffset = mousePosition;
        boolean dragWindow = false;

        SetWindowPosition((int)windowPosition.x(), (int)windowPosition.y());

        boolean exitWindow = false;

        SetTargetFPS(60);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!exitWindow && !WindowShouldClose())    // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            mousePosition = GetMousePosition();

            if (IsMouseButtonPressed(Raylib.MOUSE_BUTTON_LEFT))
            {
                if (CheckCollisionPointRec(mousePosition, new Jaylib.Rectangle(0, 0, screenWidth, 20 )))
                {
                    dragWindow = true;
                    panOffset = mousePosition;
                }
            }

            if (dragWindow)
            {
                windowPosition.x( windowPosition.x() + (mousePosition.x() - panOffset.x()) );
                windowPosition.y( windowPosition.y() + (mousePosition.y() - panOffset.y()) );

                if (IsMouseButtonReleased(MOUSE_BUTTON_LEFT)) dragWindow = false;

                SetWindowPosition((int)windowPosition.x(), (int)windowPosition.y());
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            BeginDrawing();

            ClearBackground(RAYWHITE);

            exitWindow = GuiWindowBox(new Jaylib.Rectangle(0, 0, screenWidth, screenHeight ), "#198# PORTABLE WINDOW");

            DrawText(("Mouse Position: [" + mousePosition.x()+", "+ mousePosition.y()+"]"), 10, 40, 10, DARKGRAY);

            EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        CloseWindow();        // Close window and OpenGL context
        //--------------------------------------------------------------------------------------

        return;
    }


}
