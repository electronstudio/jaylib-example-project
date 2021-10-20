import com.raylib.Jaylib.*
import com.raylib.Raylib
import kotlinx.collections.immutable.*
import java.util.*
import kotlin.concurrent.thread


typealias Vector2 = Pair<Int, Int>


private val Vector2.x: Int
    get() {
        return first
    }

private val Vector2.y: Int
    get() {
        return second
    }

const val WIDTH = 1920
const val HEIGHT = 1080

const val FRAME_TIME_NANOS: Long = 16666666

const val RBL = 10
const val NUM_OF_BOTS = 1000

val rand = Random()
//@JvmInline value class Vec(val v: Pair<Int, Int>){
//    fun getX() = v.first
//    fun getY() = v.second
//}
//


//data class Vector2(val x: Int, val y: Int) {
//    override fun toString(): String {
//        return "($x, $y)"
//    }
//}

enum class Direction(val x: Int, val y: Int) {
    UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0), CENTER(0, 0)
}

data class PlayerInput(val spawn: Boolean = false, val direction: Direction = Direction.CENTER)

class Snake(x: Int = 0, y: Int = 0,
            val segments: PersistentList<Segment> = persistentListOf(Segment(Vector2(x, y), Vector2(x, y))),
            val direction: Direction = Direction.UP,
            val color: Raylib.Color = RED
        //val position: Vector2 = Vector2(0, 0)
) {
}

class Segment(val start: Vector2, val end: Vector2) {

    override fun toString(): String {
        return "Segment ($start, $end}"
    }
}

//class Point(val x: Int, val y: Int)

class WorldState(val snakes: PersistentMap<Int, Snake> = persistentMapOf()) {
}

class InputState(val playerInputs: HashMap<Int, PlayerInput> = HashMap())

object Main {
    val game = Game()

    @JvmStatic
    fun main(args: Array<String>) {
        game.run()
    }
}

class Game {

    var zoom = 1.0

    @Volatile
    var updateTime: Long = 0

    @Volatile
    var currentWorldState = WorldState(persistentMapOf())
    var oldState = currentWorldState
    val inputStateArray = HashMap<Long, InputState>()

    init {
        val snakes = HashMap<Int, Snake>()
        for (i in 1..NUM_OF_BOTS) {
            val color = ColorFromHSV(i.toFloat() * 3, 1f, 0.1f)
            val snake = Snake(x = (rand.nextFloat()*WIDTH - WIDTH/2).toInt(), y = (rand.nextFloat()*HEIGHT - HEIGHT/2).toInt(),
                    color = color, direction = Direction.RIGHT)
            snakes.put(i, snake)
            //println("Snake $i, color ${snake.color.r()} ${snake.color.g()} ${snake.color.b()}")
        }


        currentWorldState = WorldState(snakes.toImmutableMap().toPersistentMap())
        oldState = currentWorldState
    }

    @Volatile var logicFrame: Long = 0
    @Volatile var drawFrame: Long = 0

    fun run() {


        val b = Vector2(2, 2)
        b.x



        Raylib.InitWindow(WIDTH, HEIGHT, "Demo")
        Raylib.SetTargetFPS(60)


        val updateThread = thread {
            val t = System.nanoTime()
            update()
            val d = System.nanoTime() - t
            println(d)
        }

        while (!Raylib.WindowShouldClose()) {
            if(logicFrame != drawFrame){
                println("$logicFrame $drawFrame")
            }
            val logicThread = thread {
                doLogic()
            }
            draw(currentWorldState)
            drawFrame++
        }
        Raylib.CloseWindow()
        updateThread.stop()
    }



    fun update() {

        val startTime = System.nanoTime()
        //var timer = System.nanoTime()
        while (true) {
            //doLogic()
            val timer = startTime + logicFrame * FRAME_TIME_NANOS

            //while (System.nanoTime() < timer) {
                //Thread.sleep(1)
            //}

            while (logicFrame>drawFrame) {
            }

        }
    }

    private fun doLogic() {
        val frameStartTime = System.nanoTime()
        val inputDirection = when {
            IsKeyDown(KEY_UP) -> Direction.UP
            IsKeyDown(KEY_DOWN) -> Direction.DOWN
            IsKeyDown(KEY_LEFT) -> Direction.LEFT
            IsKeyDown(KEY_RIGHT) -> Direction.RIGHT
            else -> Direction.CENTER
        }
        //   if (inputDirection != null) {
        val inputState = InputState()

        //var inputs = persistentMapOf<Int, Direction>()
        //

        val spawn = IsKeyDown(KEY_SPACE)
        val playerInput = PlayerInput(spawn, inputDirection)
        inputState.playerInputs.put(0, playerInput)
        //}
        inputStateArray.put(logicFrame - RBL + 1, inputState)
        //   }


        //            for ((id, snake) in currentWorldState.snakes) {
        //                if (rand.nextFloat() < 0.01) {
        //                    inputState.playerInputs.put(id, Direction.values().random())
        //                    inputStateArray.put(frame, inputState)
        //                }
        //            }

        // if(frame>100) {
        oldState = nextState(inputStateArray.getOrDefault(logicFrame - RBL + 1, InputState()), oldState)
        var tempState = oldState
        for (i in logicFrame - RBL..logicFrame) {
            //println(i)
            tempState = nextState(inputStateArray.getOrDefault(i, InputState()), tempState)
        }

        currentWorldState = tempState //nextState(inputArray.getOrDefault(frame, Input()), state)

        // }

        val frameTimeTaken = System.nanoTime() - frameStartTime
        updateTime = frameTimeTaken / 1000000

        logicFrame++
    }


    private fun nextState(inputState: InputState, worldState: WorldState): WorldState {
        var newSnakes = worldState.snakes

//        if(newSnakes.size==0){
//            newSnakes = newSnakes.put(0, Snake())
//        }

        for ((id, playerInput) in inputState.playerInputs) {
            if (playerInput.spawn) {
                newSnakes = newSnakes.put(id, Snake())
            }

            val snake = newSnakes.get(id) ?: Snake() // fixme
            var segments = snake.segments

            if (playerInput.direction != Direction.CENTER && snake.direction != playerInput.direction) {
                val position = segments.last().end
                segments = segments.add(Segment(position, position))
                newSnakes = newSnakes.put(id, Snake(segments = segments, direction = playerInput.direction))
            }


        }

        for ((id, snake) in newSnakes) {
            var segments = snake.segments

            //val playerInput = inputState.playerInputs.get(id)


            val direction = snake.direction //playerInput?.direction ?: snake.direction
            val oldStart = segments.last().start
            val newPosition = Vector2(segments.last().end.x + direction.x, segments.last().end.y + direction.y)
            val newLastSegment = Segment(oldStart, newPosition)
            segments = segments.remove(segments.last()).add(newLastSegment)


            val newSnake = Snake(0, 0, segments, direction, snake.color)
            newSnakes = newSnakes.put(id, newSnake)
        }
        return WorldState(newSnakes)

    }

    val zoomX = 1
    val zoomY = -1

//    fun convertX(x: Int): Int{
//        return (x / zoomX) + WIDTH/2
//    }
//
//    fun convertY(y: Int): Int{
//        return (y / zoomY) + HEIGHT/2
//    }

//    fun convertRL(p: Vector2): Raylib.Vector2 {
//
//        return Raylib.Vector2().x((p.x / zoomX) + WIDTH / 2).y((p.y / zoomY) + HEIGHT / 2)
//    }

    fun convertRL(p: Vector2, rlv: Raylib.Vector2): Raylib.Vector2 {

        return rlv.x(((p.x / zoomX) + WIDTH / 2).toFloat()).y(((p.y / zoomY) + HEIGHT / 2).toFloat())


    }


    private fun draw(worldState: WorldState) {
        BeginDrawing()
        ClearBackground(BLACK)

        for (snake in worldState.snakes.values) {
            //println("${snake.color.r()} ${snake.color.g()} ${snake.color.b()}")
            for (segment in snake.segments) {
                var rlv = Raylib.Vector2()
                var rlv2 = Raylib.Vector2()
                //rlv.deallocate(false)
                //rlv2.deallocate(false)

                DrawLineEx(convertRL(segment.start, rlv), convertRL(segment.end, rlv2), 22f, WHITE)
                DrawLineEx(convertRL(segment.start, rlv), convertRL(segment.end, rlv2), 20f, snake.color)
                DrawCircle(convertRL(segment.start, rlv).x().toInt(), convertRL(segment.start, rlv2).y().toInt(), 13f, WHITE)


                DrawCircle(convertRL(segment.start, rlv).x().toInt(), convertRL(segment.start, rlv2).y().toInt(), 12f, snake.color)
                DrawCircle(convertRL(segment.end, rlv).x().toInt(), convertRL(segment.end, rlv2).y().toInt(), 13f, WHITE)
                DrawCircle(convertRL(segment.end, rlv).x().toInt(), convertRL(segment.end, rlv2).y().toInt(), 12f, snake.color)


                rlv.deallocate()
                rlv2.deallocate()
            }
        }



        Raylib.DrawText(updateTime.toString(), 100, 20, 50, WHITE)
        DrawFPS(20, 20)
        EndDrawing()
    }
}