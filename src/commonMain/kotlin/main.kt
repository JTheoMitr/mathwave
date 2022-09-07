import com.soywiz.klock.*
import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.time.delay
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.tween.moveTo
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlin.random.Random
import kotlin.reflect.KClass

suspend fun main() = Korge(Korge.Config(module = ConfigModule))

object ConfigModule : Module() {
    override val bgcolor = Colors["#2b2b2b"]
    override val size = SizeInt(1024, 768)
    override val mainScene: KClass<out Scene> = Scene1::class

    override suspend fun AsyncInjector.configure() {
        mapPrototype { Scene1() }
        mapPrototype { Scene2() }
    }
}

class Scene1() : Scene() {
    override suspend fun Container.sceneInit() {
        val bg = solidRect(1024.0, 768.0, Colors["#02020bdd"]).xy(1024.0, 768.0)

        val title = text("START GAME", alignment = TextAlignment.CENTER, textSize = 30.0).xy(IPoint.invoke(bg.width / 2, bg.height / 2 ))

        title.onClick {
            sceneContainer.changeTo<Scene2>()
        }

    }

}

class Scene2() : Scene() {
    override suspend fun Container.sceneInit() {

        // Establish background field
        val rect = solidRect(1024.0, 768.0, Colors["#02020bdd"]).xy(0.0, 0.0)

        // Some Abstract Values
        val buffer = 40
        val minDegrees = (-110).degrees
        val maxDegrees = (+90).degrees
        var jellyHits = 0
        var garbagePickUps = 0
        var canSwitch = true
        var jellySwitchPurple = true
        var jellySwitchGreen = true
        var levelIsActive = false
        val surferBoundary = rect.height - 90

        // SPRITES AND IMAGES

        // Target
	    val neonTarget = image(resourcesVfs["neon_target_1.png"].readBitmap()) {
		    rotation = maxDegrees
		    anchor(.5, .5)
		    scale(.085)
		    position(rect.width / 2, rect.height - 50)
	    }

        // Red Triangle 1
        val redTriangleOne = resourcesVfs["red_tri_complete.xml"].readAtlas()
        val triangleOneAnimation = redTriangleOne.getSpriteAnimation("neon")

        // PURPLE Jellyfish
        val jellyOneSprites = resourcesVfs["jellyfish_one.xml"].readAtlas()
        val jellyOneAnimation = jellyOneSprites.getSpriteAnimation("jelly")

        val canOneSprites = resourcesVfs["oil_can_one.xml"].readAtlas()
        val canOneAnimation = canOneSprites.getSpriteAnimation("img")

        val garbageBagSprites = resourcesVfs["garbage_bag_one.xml"].readAtlas()
        val garbageBagAnimation = garbageBagSprites.getSpriteAnimation("img")

        // Establish Music

//        val music = resourcesVfs["neon_slide_one.wav"].readMusic()
//        music.play()


            // Add Components to the Stage

            // Banner
            val rect2 = solidRect(1024.0, 65.0, Colors["#3c436df7"]).xy(0.0, 0.0)

            // HEARTS
            val heartImgOne = image(resourcesVfs["pixel_heart_one.png"].readBitmap()) {
                anchor(.5, .5)
                scale(.03)
                position(rect.width - 160, 30.0)
                visible = true
            }

            val heartImgTwo = image(resourcesVfs["pixel_heart_one.png"].readBitmap()) {
                anchor(.5, .5)
                scale(.03)
                position(rect.width - 120, 30.0)
            }

            val heartImgThree = image(resourcesVfs["pixel_heart_one.png"].readBitmap()) {
                anchor(.5, .5)
                scale(.03)
                position(rect.width - 80, 30.0)
            }

            // LASER
            val laserOne = image(resourcesVfs["laser_green_one.png"].readBitmap()) {
            anchor(.5, .5)
            scale(.07)
            position(rect.width / 2, 30.0)
            rotation(Angle.fromDegrees(90))
            visible = false
            }

            // EXPLOSION STUFF

            val spriteMap = resourcesVfs["explosion.png"].readBitmap()

            val explosionAnimation = SpriteAnimation(
            spriteMap = spriteMap,
            spriteWidth = 128, // image is 1024x1024 and it's 8x8, 1024 / 8 = 128
            spriteHeight = 128,
            marginTop = 0, // default
            marginLeft = 0, // default
            columns = 8,
            rows = 8,
            offsetBetweenColumns = 0, // default
            offsetBetweenRows = 0 // default
            )

            val explosion = sprite(explosionAnimation)
            explosion.visible = false
            explosion.scale = 1.0

            // GARBAGE BAG
            val garbageBag = image(resourcesVfs["garbage_bag_one.png"].readBitmap()) {
                anchor(.5, .5)
                scale(.1)
                position(rect.width - 80, rect.height - 60)
            }

            // JELLYFISH

            val jellySchool = Array<Sprite>(1) {
                sprite(jellyOneAnimation) {
                    anchor(.5, .5)
                    scale(.4)
                    visible = false
                    this.playAnimationLooped(spriteDisplayTime = 90.milliseconds)

                }
            }

            // RED TRIANGLE

            val redTriangleGroupOne = Array<Sprite>(1) {
                sprite(triangleOneAnimation) {
                    anchor(.5, .5)
                    scale(.3)
                    visible = false
                    this.playAnimationLooped(spriteDisplayTime = 90.milliseconds)
                }
            }


            suspend fun targetMovement(clickPoint: Point) {

                if (clickPoint.y <= surferBoundary) { clickPoint.y = surferBoundary }
                neonTarget.tweenAsync(neonTarget::x[neonTarget.x, clickPoint.x], time = 1.5.seconds, easing = Easing.EASE)
                neonTarget.tweenAsync(neonTarget::y[neonTarget.y, clickPoint.y], time = 1.5.seconds, easing = Easing.EASE)

            }


            // CAN CLUSTER

            val canCluster = Array<Sprite>(1) {
                sprite(canOneAnimation) {
                    anchor(.5, .5)
                    scale(.2)
                    visible = false
                    this.playAnimationLooped(spriteDisplayTime = 90.milliseconds)

                }
            }

            // Level Functions

            fun levelComplete() {

                val levelComplete = text("Level Completed") {
                    position(centerOnStage())
                    neonTarget.removeFromParent()
                    jellySchool.forEach { it.removeFromParent() }
                    canCluster.forEach { it.removeFromParent() }
                }
            }

            fun gameOver() {

                val gameOver = text("GAME OVER") {
                    position(centerOnStage())
                    neonTarget.removeFromParent()
                    jellySchool.forEach { it.removeFromParent() }
                    canCluster.forEach { it.removeFromParent() }
                }
            }

            // track switch position for hit detection

            fun canSwitchHit() {
                if (canSwitch) {
                    garbagePickUps += 1
                    garbageBag.scale += .05
                }

                // WIN Parameters
                if (garbagePickUps >= 3) {
                    levelComplete()
                }
            }

            fun jellySwitchGreenHit() {
                if (jellySwitchGreen) {
                    jellyHits += 1
                }
                if (jellyHits == 1) {
                    heartImgThree.visible = false
                }

                if (jellyHits == 2) {
                    heartImgTwo.visible = false
                }

                if (jellyHits >= 3) {
                    heartImgOne.visible = false
                    gameOver()
                }
            }

            fun jellySwitchPurpleHit() {
                if (jellySwitchPurple) {
                    jellyHits += 1
                }
                if (jellyHits == 1) {
                    heartImgThree.visible = false
                }

                if (jellyHits == 2) {
                    heartImgTwo.visible = false
                }

                if (jellyHits >= 3) {
                    heartImgOne.visible = false
                    gameOver()
                }
            }

            suspend fun laserBoi() {
            laserOne.position(neonTarget.x, neonTarget.y)
            laserOne.visible = true
            laserOne.moveTo(laserOne.x, -25.0, 0.5.seconds, Easing.EASE)
        }

            suspend fun runJelly() {

                println("JELLYS RUNNING")
                awaitAll(async {
                    redTriangleGroupOne.forEach {
                        // if (!it.visible || it.pos.y > height) {
                        delay((Random.nextInt(1, 3)).seconds)
                        val jellyX = Random.nextInt(buffer, (width.toInt() - buffer)).toDouble()
                        jellySwitchPurple = true
                        it.visible = true
                        it.position(jellyX, -5.0)

                        it.addUpdater {
                            if (neonTarget.collidesWith(this) && jellySwitchPurple) {

                                var collisionPosX = neonTarget.x - 60
                                var collisionPosY = neonTarget.y - 70
                                explosion.xy(collisionPosX, collisionPosY)
                                println(collisionPosY)
                                jellySwitchPurpleHit()
                                jellySwitchPurple = false

                                explosion.visible = true
                                this.visible = false

                                explosion.playAnimationForDuration(2.seconds)
                                explosion.onAnimationCompleted { explosion.visible = false}

                                println("Purple Jelly hits Surfer $jellyHits")
                            }

                            else if (laserOne.collidesWith(this)) {
                                this.visible = false
                                jellySwitchPurple = false
                                explosion.xy(this.x - 50, this.y - 50)
                                explosion.visible = true
                                explosion.playAnimationForDuration(2.seconds)
                                explosion.onAnimationCompleted { explosion.visible = false}

                            }

                        }

                        it.moveTo(jellyX + 75, 400.0, 2.seconds, Easing.EASE_IN)
                        it.tween(it::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
                        it.moveTo(jellyX + 3, height - buffer, 500.milliseconds, Easing.EASE_IN)
                        it.tween(it::rotation[minDegrees], time = 500.milliseconds, easing = Easing.EASE_IN_OUT)
                        it.moveTo(jellyX + 30, height + buffer, 1.seconds, Easing.EASE_IN)

                    }
                }, async {
                    canCluster.forEach {
                        //  if (!it.visible || it.pos.y > height) {
                        delay((Random.nextInt(1, 2)).seconds)
                        canSwitch = true
                        val canX = Random.nextInt(buffer, (width.toInt() - buffer)).toDouble()
                        it.visible = true
                        it.position(canX, -5.0)

                        it.addUpdater {
                            if (neonTarget.collidesWith(this)) {
                                this.visible = false
                                canSwitchHit()
                                canSwitch = false

                                // colorDefault = AnsiEscape.Color.RED
                                println("$garbagePickUps")
                            }
                        }

                        it.moveTo(canX, height + buffer, 3.seconds, Easing.EASE_IN)

                        //  }
                    }
                })
            }

            suspend fun jellyTimer() {
                while (levelIsActive) {
                    awaitAll(
                        async { runJelly() },
                        async {
                            neonTarget.tween(neonTarget::rotation[minDegrees], time = 3.seconds, easing = Easing.EASE_IN_OUT)
                            neonTarget.tween(neonTarget::rotation[maxDegrees], time = 3.seconds, easing = Easing.EASE_IN_OUT) }
                    )
                }
            }

            rect.onClick {

            println("clicked!")

            val target = it.currentPosLocal

            // MOVE SURFER
            neonTarget.position(neonTarget.x, neonTarget.y)
            targetMovement(target)

        }

            addUpdater {

            if (views.input.keys[Key.SPACE]) {
                async { laserBoi() }
            }

        }

            garbageBag.onClick {
                levelIsActive = true
                println(levelIsActive.equals(true))
                jellyTimer()
            }
        }
}