import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.Circle
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {

// 1) Basic Shapes and Images:

        //  Circle:

        //	val circle = Circle(radius = 20.0, fill = Colors.GREEN).xy(100, 100)
        //	addChild(circle)
	    // ^^ code above does the same as:
        //	circle(radius = 20.0, fill = Colors.GREEN).xy(100, 100)  // can use this syntax with shapes and views that can be added to screen

        //  Rectangle:

        //    solidRect(width = 100.0, height = 100.0, Colors.GOLD).xy(110, 110)  // the later a view is added to it's parent, the higher it is in the drawing stack

        //  Display Image:

        //    val bitmap = resourcesVfs["korge.png"].readBitmap()
        //    image(bitmap).scale(.3).apply {
                // rotation = (+50).degrees
                //  alpha = 0.5
        //    }

// 2) Sprites and SpriteAnimations:

//    val spriteMap = resourcesVfs["explosion.png"].readBitmap()

    // image(spriteMap)

//    val explosionAnimation = SpriteAnimation(
//        spriteMap = spriteMap,
//        spriteWidth = 128, // image is 1024x1024 and it's 8x8, 1024 / 8 = 128
//        spriteHeight = 128,
//        marginTop = 0, // default
//        marginLeft = 0, // default
//        columns = 8,
//        rows = 8,
//        offsetBetweenColumns = 0, // default
//        offsetBetweenRows = 0 // default
//    )
//
//
//    val explosion = sprite(explosionAnimation)

    // diff playAnimation options and parameters:

    // explosion.playAnimation(times = 2)
    // explosion.playAnimationLooped(spriteDisplayTime = 500.milliseconds)
    // explosion.playAnimationLooped(reversed = true, startFrame = 6)
    // explosion.playAnimationForDuration(1.seconds)
    // explosion.playAnimationLooped()


    // lifecycle of animation:

    // explosion.onAnimationCompleted
    // explosion.onAnimationStarted
    // explosion.onAnimationStopped



// 3) Simple Collision Detection:

//    val rect1 = solidRect(100.0, 100.0, Colors.GREEN).xy(200,200)
//    val rect2 = solidRect(100.0, 100.0, Colors.BLUE).xy(300,200)
//
//    val rectList = listOf(rect1, rect2)
//
//
//    val circle = circle(20.0, Colors.RED)
//
//    circle.addUpdater {
//        circle.fill = Colors.RED
//        x = mouseX - circle.radius
//        y = mouseY - circle.radius
//
//        if (collidesWith(rect1)) {
//            circle.fill = Colors.VIOLET
//        }
//        if (collidesWith(rect2)) {
//            circle.fill = Colors.DARKKHAKI
//        }
//    }

    // adding the parameter {it == rect} here will filter all view collisions EXCEPT rect, calling the method on only collisions with rect
//    circle.onCollision( { it == rect } ) {
//        circle.fill = Colors.BLUE
//    }


// 4) Resolution Handling and Coordinates System

}





// Hello World Sample Code (was inside main function):

//	val minDegrees = (-16).degrees
//	val maxDegrees = (+16).degrees
//
//	val image = image(resourcesVfs["korge.png"].readBitmap()) {
//		rotation = maxDegrees
//		anchor(.5, .5)
//		scale(.8)
//		position(256, 256)
//	}
//
//	while (true) {
//		image.tween(image::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
//		image.tween(image::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
//	}