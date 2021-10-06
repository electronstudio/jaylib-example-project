# Example Gradle project for Jaylib

You can import this project into IntelliJ or Eclipse.

## Use it to run the included examples in IntelliJ

Right-click on the example and select `run`.

## Use it to run the included examples from the command line

    ./gradlew run -Pmain=examples.HeightMap
    ./gradlew run -Pmain=examples.CubicMap

## Use it as the basis of your own game.

Edit `Main.java` with your own code.  (You can delete the examples.)  Then

To run:

    ./gradlew run

To build a zip file you can distribute:

    ./gradlew distZip
