package examples;


import examples.RLights.Light;
import org.bytedeco.javacpp.FloatPointer;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.Vector3;
import static com.raylib.Raylib.*;
import static examples.RLights.Light.MAX_LIGHTS;


public class ShadersBasicLighting {

    public static void main(String[] args) {

        final int screenWidth = 800;
        final int screenHeight = 450;

        SetConfigFlags(FLAG_MSAA_4X_HINT);  // Enable Multi Sampling Anti Aliasing 4x (if available)
        InitWindow(screenWidth, screenHeight, "raylib [shaders] example - basic lighting");

        // Define the camera to look into our 3d world
        Camera3D camera = new Camera3D()
                ._position((new Vector3().x(2.0f).y(4.0f).z(6.0f)))
                .target(new Vector3().x(0.0f).y(0.5f).z(0.0f))
                .up(new Vector3().x(0.0f).y(1.0f).z(0.0f))
                .fovy(45.0f)
                .projection(CAMERA_PERSPECTIVE);


        // Load plane model from a generated mesh
        Model model = LoadModelFromMesh(GenMeshPlane(10.0f, 10.0f, 3, 3));
        Model cube = LoadModelFromMesh(GenMeshCube(2.0f, 4.0f, 2.0f));
        // Load basic lighting shader
        Shader shader = LoadShader("resources/lighting.vs", "resources/lighting.fs");
        // Get some required shader locations
        shader.locs().put(SHADER_LOC_VECTOR_VIEW, GetShaderLocation(shader, "viewPos"));
        // NOTE: "matModel" location name is automatically assigned on shader loading,
        // no need to get the location again if using that uniform name
        //shader.locs[SHADER_LOC_MATRIX_MODEL] = GetShaderLocation(shader, "matModel");

        // Ambient light level (some basic lighting)
        int ambientLoc = GetShaderLocation(shader, "ambient");

        FloatPointer ambient = new FloatPointer(0.1f, 0.1f, 0.1f, 1.0f);

        SetShaderValue(shader, ambientLoc, ambient, SHADER_UNIFORM_VEC4);

        // Assign out lighting shader to model
        model.materials().shader().put(shader);
        cube.materials().shader().put(shader);

        // Create lights
        Light[] lights = new Light[MAX_LIGHTS];
        lights[0] = new Light(Light.LIGHT_POINT, new Vector3().x(-2).y(1).z(-2), Vector3Zero(), YELLOW, shader);
        lights[1] = new Light(Light.LIGHT_POINT, new Vector3().x(2).y(1).z(2), Vector3Zero(), RED, shader);
        lights[2] = new Light(Light.LIGHT_POINT, new Vector3().x(-2).y(1).z(2), Vector3Zero(), GREEN, shader);
        lights[3] = new Light(Light.LIGHT_POINT, new Vector3().x(2).y(1).z(-2), Vector3Zero(), BLUE, shader);


        SetTargetFPS(60);                   // Set our game to run at 60 frames-per-second
        //--------------------------------------------------------------------------------------

        FloatPointer cameraPos = new FloatPointer(camera._position().x(), camera._position().y(), camera._position().z());

        // Main game loop
        while (!WindowShouldClose())        // Detect window close button or ESC key
        {
            // Update
            //----------------------------------------------------------------------------------
            UpdateCamera(camera, CAMERA_ORBITAL);

            // Update the shader with the camera view vector (points towards { 0.0f, 0.0f, 0.0f })

            cameraPos.put(0, camera._position().x());
            cameraPos.put(1, camera._position().y());
            cameraPos.put(2, camera._position().z());
            SetShaderValue(shader, shader.locs().get(SHADER_LOC_VECTOR_VIEW), cameraPos, SHADER_UNIFORM_VEC3);

            // Check key inputs to enable/disable lights
            if (IsKeyPressed(KEY_Y)) {
                lights[0].setEnabled(!lights[0].getEnabled());
            }
            if (IsKeyPressed(KEY_R)) {
                lights[1].setEnabled(!lights[1].getEnabled());
            }
            if (IsKeyPressed(KEY_G)) {
                lights[2].setEnabled(!lights[2].getEnabled());
            }
            if (IsKeyPressed(KEY_B)) {
                lights[3].setEnabled(!lights[3].getEnabled());
            }

            // Update light values (actually, only enable/disable them)
            for (int i = 0; i < MAX_LIGHTS; i++) lights[i].UpdateLightValues(shader);
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            BeginDrawing();

            ClearBackground(RAYWHITE);

            BeginMode3D(camera);

            //  BeginShaderMode(shader);

            DrawModel(model, Vector3Zero(), 1.0f, WHITE);
            DrawModel(cube, Vector3Zero(), 1.0f, WHITE);

//            DrawPlane(Vector3Zero(), new Vector2().x(10.0f).y(10.0f), WHITE);
//            DrawCube(Vector3Zero(), 2.0f, 4.0f, 2.0f, WHITE);

            //     EndShaderMode();

            // Draw spheres to show where the lights are
            for (int i = 0; i < MAX_LIGHTS; i++) {
                if (lights[i].getEnabled()) DrawSphereEx(lights[i].getPosition(), 0.2f, 8, 8, lights[i].getColor());
                else DrawSphereWires(lights[i].getPosition(), 0.2f, 8, 8, ColorAlpha(lights[i].getColor(), 0.3f));
            }

            DrawGrid(10, 1.0f);

            EndMode3D();

            DrawFPS(10, 10);

            DrawText("Use keys [Y][R][G][B] to toggle lights", 10, 40, 20, DARKGRAY);

            EndDrawing();
            //----------------------------------------------------------------------------------
        }

        // De-Initialization
        //--------------------------------------------------------------------------------------
        UnloadShader(shader);   // Unload shader

        CloseWindow();          // Close window and OpenGL context
        //--------------------------------------------------------------------------------------


    }
}