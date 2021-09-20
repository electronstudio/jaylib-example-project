package examples;


import static com.raylib.Jaylib.RED;
import static com.raylib.Jaylib.VIOLET;
import static com.raylib.Jaylib.RAYWHITE;
import static com.raylib.Raylib.*;


public class HeightMap {
    public static void main(String args[]) {
        InitWindow(800, 450, "Raylib static texture test");
        SetTargetFPS(60);
        Camera3D camera = new Camera3D()
                ._position(new Vector3().x(18).y(16).z(18))
                .target(new Vector3())
                .up(new Vector3().x(0).y(1).z(0))
                .fovy(45).projection(CAMERA_PERSPECTIVE);

        Image image = LoadImage("resources/heightmap.png");
        Texture texture = LoadTextureFromImage(image);
        Mesh mesh = GenMeshHeightmap(image, new Vector3().x(16).y(8).z(16));
        Model model = LoadModelFromMesh(mesh);
        model.materials().maps().position(0).texture(texture);


        UnloadImage(image);
        SetCameraMode(camera, CAMERA_ORBITAL);

        while(!WindowShouldClose()){
            UpdateCamera(camera);
            BeginDrawing();
            ClearBackground(RAYWHITE);
            BeginMode3D(camera);
            DrawModel(model, new Vector3().x(-8).y(0).z(-8), 1, RED);
            DrawGrid(20, 1.0f);
            EndMode3D();
            DrawText("This mesh should be textured", 190, 200, 20, VIOLET);
            DrawFPS(20, 20);
            EndDrawing();
        }
        CloseWindow();
    }
}
