import kotlin.jvm.JvmStatic
import com.raylib.Raylib
import com.raylib.Jaylib
import com.raylib.Jaylib.*
import com.raylib.Raylib.*
import com.raylib.Raylib.Vector2
import org.kotlinmath.Complex
import org.kotlinmath.complex
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.math.abs

val MAX_ITER = 80

val numThreads = Math.max(1, Runtime.getRuntime().availableProcessors())
private val es = Executors.newFixedThreadPool(numThreads)

val WIDTH = 1920
val HEIGHT = 1080

class Fractal {

    val RE_START = -2.0
    val RE_END = 1.0
    val IM_START = -1.0
    val IM_END = 1.0
    val RE_WIDTH = (RE_END - RE_START)
    val IM_HEIGHT = (IM_END - IM_START)

    val zoomLevels = arrayListOf<ZoomLevel>()


    val cellWidth = WIDTH / 4
    val cellHeight = HEIGHT / 4

    fun mandelbrot(c: Complex): Int {
        var z = complex(0, 0)
        var n = 0
        while (z.mod <= 2 && n < MAX_ITER) {
            z = z * z + c
            n += 1
        }
        return n
    }

    fun plot_image(
        width: Int,
        height: Int,
        image: Image,
        re_start: Double,
        re_width: Double,
        im_start: Double,
        im_height: Double
    ) {
        for (x in 0..width) {
            for (y in 0..height) {
                val c = complex(
                    (re_start + (x / (width.toDouble())) * re_width),
                    (im_start + (y / (height.toDouble())) * im_height)
                )

                val m = mandelbrot(c)
                val hue = (255f * m.toFloat() / MAX_ITER.toFloat())
                val saturation = 255f
                val value = if (m < MAX_ITER) 255f else 0f
                val color = ColorFromHSV(hue.toFloat(), saturation, value)
                //println("$x $y $c")
                ImageDrawPixel(image, x, y, color)
            }
        }
    }

    fun addLevel() {
        val level = ZoomLevel(zoomLevels.size)
        zoomLevels.add(level)
        val reWidth = RE_WIDTH / (level.level + 1)
        val imHeight = IM_HEIGHT / (level.level + 1)
        for (row in 0 until level.cells.size) {
            for (column in 0 until level.cells[row].size) {
                es.submit() {
                    println("making image level ${level.level} $row $column width $cellWidth height $cellHeight")
                    val image = GenImageColor(cellWidth, cellHeight, BLUE)
                    plot_image(
                        cellWidth,
                        cellHeight,
                        image,
                        RE_START + reWidth * column,
                        reWidth,
                        IM_START + imHeight * row,
                        imHeight
                    )
                    level.cells[row][column] = image
                    level.completed.incrementAndGet()
                }
            }
        }
    }

}

class ZoomLevel(val level: Int) {
    val rows = Math.pow(2.0, level.toDouble()).toInt()
    val columns = rows
    val cells = Array(rows) { Array<Image?>(columns) { null } }
    val completed = AtomicInteger(0)


}


fun update() {
//    if (IsKeyPressed(KEY_SPACE)) {
//        zoom *= 1.2
//        plot_image()
//    } else  if (IsKeyPressed(KEY_ENTER)) {
//        zoom *= 0.8
//        plot_image()
//    }
}

//    elif keyboard . up :
//    IM_START -= 0.2
//    plot_image()
//    elif keyboard . down :
//    IM_START += 0.2
//    plot_image()
//    elif keyboard . left :
//    RE_START -= 0.2
//    plot_image()
//    elif keyboard . right :
//    RE_START += 0.2
//    plot_image()


object Main {


    //val SCALE = 1


    var zoom = 1.0


    lateinit var texture: Texture
    lateinit var screenImage: Image

    val fractal = Fractal()

    @JvmStatic
    fun main(args: Array<String>) {
        Raylib.InitWindow(WIDTH, HEIGHT, "Demo")
        Raylib.SetTargetFPS(60)

        //fractal.addLevel()


        screenImage = GenImageColor(WIDTH, HEIGHT, YELLOW)
        texture = LoadTextureFromImage(screenImage)



        SetTextureFilter(texture, TEXTURE_FILTER_BILINEAR)

        fractal.addLevel()


        //renderImage()

//            val t = WIDTH / fractal.cellWidth
//            val l = Math.sqrt(t.toDouble())
//            print("we need $t x $t cells for max res, so level $l")

        // thread(start = true) {
        repeat(2) {
            fractal.addLevel()

            //    }

        }
//        thread (start=true ){
//            while (true) {
//                renderImage()
//                Thread.sleep(100)
//            }
//        }

        while (!Raylib.WindowShouldClose()) {
            update()
            draw(fractal)
        }
        Raylib.CloseWindow()
    }


    val r1 = Raylib.Rectangle().width(fractal.cellWidth.toFloat()).height(fractal.cellHeight.toFloat())
    val r2 = Raylib.Rectangle().width(fractal.cellWidth.toFloat()).height(fractal.cellHeight.toFloat())

    private fun renderImage():Boolean {
        var allDone = true
        for (l in 0 until fractal.zoomLevels.size) {
            println("rendering $l of ${fractal.zoomLevels.size}")

            //val l = 2
            val level = fractal.zoomLevels[l]
            if(level.completed.get() != level.rows*level.columns){
                allDone = false
                break
            }
            val width = WIDTH / (level.level + 1)
            val height = HEIGHT / (level.level + 1)
            for (row in 0 until level.cells.size) {
                for (column in 0 until level.cells[row].size) {
                    val image = level.cells[row][column]
                    if (image==null){
                        println("not all done")
                        allDone = false
                    }else{
                        r2.x((width * column).toFloat()).y((height * row).toFloat()).width(width.toFloat())
                            .height(height.toFloat())
                        ImageDraw(screenImage, image, r1, r2, WHITE)
                    }
//                    image?.let {
//                        //println("drawing level $l row $row column $column")
//                        r2.x((width * column).toFloat()).y((height * row).toFloat()).width(width.toFloat())
//                            .height(height.toFloat())
//                        ImageDraw(screenImage, image, r1, r2, WHITE)
//                        //UpdateTexture(texture, it.data())
//                        //val t = LoadTextureFromImage(image)
//                        //DrawTextureEx(t, Vector2().x((width*column).toFloat()).y((height*row).toFloat()), 0f,
//                        //    (WIDTH/(l+1)).toFloat(), WHITE)
//                    }
                }
            }
        }
        println(allDone)
        return allDone
    }

    var needRender = true

    private fun draw(fractal: Fractal) {
        BeginDrawing()

        if(needRender){
            needRender = !renderImage()
        }

        UpdateTexture(texture, screenImage.data())
        DrawTexture(texture, 0, 0, WHITE)


        DrawText("Hello world", 190, 200, 20, VIOLET)
        DrawFPS(20, 20)
        EndDrawing()
    }
}