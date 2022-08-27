import com.soywiz.klock.*
import com.soywiz.korau.format.AudioDecodingProps
import com.soywiz.korau.sound.*
import com.soywiz.korau.sound.DummySoundProps.pitch
import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.animate.playAndWaitEvent
import com.soywiz.korge.animate.waitEvent
import com.soywiz.korge.animate.waitStop
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.stat.Stats
import com.soywiz.korge.time.delay
import com.soywiz.korge.time.timers
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.Circle
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
import kotlinx.coroutines.cancel
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

        val title = text("START GAME", alignment = TextAlignment.CENTER).xy(IPoint.invoke(bg.width / 2, bg.height / 2 ))

        title.onClick {
            sceneContainer.changeTo<Scene2>()
        }

    }

}

class Scene2() : Scene() {
    override suspend fun Container.sceneInit() {


        val rect = solidRect(1024.0, 768.0, Colors["#02020bdd"]).xy(0.0, 0.0)


        // Some Abstract Values
        val buffer = 40
        val minDegrees = (-16).degrees
        val maxDegrees = (+16).degrees
        var jellyHits = 0
        var garbagePickUps = 0
        var canSwitch = true
        var jellySwitchPurple = true
        var jellySwitchGreen = true
        var levelIsActive = false
        val surferBoundary = rect.height - 90

        // Sprite and Animation Control

        // Background and Wave
        val bgndSprites = resourcesVfs["bgnd_space_one.xml"].readAtlas()
        val bgndAnimation = bgndSprites.getSpriteAnimation("bgnd")

        val waveSprites = resourcesVfs["wave_break_demo.xml"].readAtlas()
        val breakAnimation = waveSprites.getSpriteAnimation("wave")

        // Surfer
        val surferSprites = resourcesVfs["surfer_boi.xml"].readAtlas()
        val idleAnimation = surferSprites.getSpriteAnimation("surfer")

        // PURPLE Jellyfish
        val jellyOneSprites = resourcesVfs["jellyfish_one.xml"].readAtlas()
        val jellyOneAnimation = jellyOneSprites.getSpriteAnimation("jelly")

        // GREEN Jellyfish
        val jellyTwoSprites = resourcesVfs["jellyfish_two.xml"].readAtlas()
        val jellyTwoAnimation = jellyTwoSprites.getSpriteAnimation("jelly")

        // GREEN JELLY 2
        val jellyThreeAnimation = jellyTwoSprites.getSpriteAnimation("jelly")

        // PURPLE JELLY 2
        val jellyFourAnimation = jellyOneSprites.getSpriteAnimation("jelly")

        val canOneSprites = resourcesVfs["oil_can_one.xml"].readAtlas()
        val canOneAnimation = canOneSprites.getSpriteAnimation("img")

        val garbageBagSprites = resourcesVfs["garbage_bag_one.xml"].readAtlas()
        val garbageBagAnimation = garbageBagSprites.getSpriteAnimation("img")

        // Establish Vaporwave Atmosphere

        val music = resourcesVfs["neon_slide_one.wav"].readMusic()
        music.play()

        val motionBgnd = sprite(bgndAnimation) {
            position(rect.width / 2, rect.height / 2)
            anchor(0.5, 0.5)
            visible = true
            scaledHeight = 768.0
            scaledWidth = 1024.0
            alpha(0.6)
        }

        val rect2 = solidRect(1024.0, 65.0, Colors["#02020bdd"]).xy(0.0, 0.0)

            // Establish WaveBreak for Level Background
            val waveBreak = sprite(breakAnimation) {
                scaledHeight = 2010.0
                scaledWidth = 290.0
                alpha = 1.0

                rotation(Angle.fromDegrees(272)) // adjust to even out
                position(rect.width / 2, rect.height + 15)
                anchor(0.0, .5)
                visible = true
            }

        waveBreak.playAnimationLooped(spriteDisplayTime = 200.milliseconds)

        motionBgnd.playAnimationLooped(spriteDisplayTime = 90.milliseconds)




            // Add Components to the Stage

            // SURFER
            val surfer = sprite(idleAnimation) {
                anchor(.5, .5)
                scale(.9)
                position(rect.width / 2, rect.height - 60)
            }
            surfer.playAnimationLooped(spriteDisplayTime = 200.milliseconds)

            // HEARTS
            val heartImgOne = image(resourcesVfs["pixel_heart_one.png"].readBitmap()) {
                anchor(.5, .5)
                scale(.03)
                position(rect.width - 160, 30.0)
                visible = true
            }

        // LASER
        val laserOne = image(resourcesVfs["laser_green_one.png"].readBitmap()) {
            anchor(.5, .5)
            scale(.07)
            position(rect.width / 2, 30.0)
            rotation(Angle.fromDegrees(90))
            visible = false
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

            val greenJellySchool = Array<Sprite>(1) {
                sprite(jellyTwoAnimation) {
                    anchor(.5, .5)
                    scale(.4)
                    visible = false
                    this.playAnimationLooped(spriteDisplayTime = 90.milliseconds)

                }
            }

            val greenJellyThreeSchool = Array<Sprite>(1) {
            sprite(jellyThreeAnimation) {
                anchor(.5, .5)
                scale(.4)
                visible = false
                this.playAnimationLooped(spriteDisplayTime = 90.milliseconds)

            }
        }

            val jellySchoolFour = Array<Sprite>(1) {
            sprite(jellyFourAnimation) {
                anchor(.5, .5)
                scale(.4)
                visible = false
                this.playAnimationLooped(spriteDisplayTime = 90.milliseconds)

            }
        }

            suspend fun surferMovement(clickPoint: Point) {

                if (clickPoint.y <= surferBoundary) { clickPoint.y = surferBoundary }
                surfer.tweenAsync(surfer::x[surfer.x, clickPoint.x], time = 1.5.seconds, easing = Easing.EASE)
                surfer.tweenAsync(surfer::y[surfer.y, clickPoint.y], time = 1.5.seconds, easing = Easing.EASE)

                surfer.tween(surfer::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
                surfer.tween(surfer::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
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
                    surfer.removeFromParent()
                    jellySchool.forEach { it.removeFromParent() }
                    greenJellySchool.forEach { it.removeFromParent() }
                    canCluster.forEach { it.removeFromParent() }
                }
            }

            fun gameOver() {

                val gameOver = text("GAME OVER") {
                    position(centerOnStage())
                    surfer.removeFromParent()
                    jellySchool.forEach { it.removeFromParent() }
                    greenJellySchool.forEach { it.removeFromParent() }
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

            rect.onClick {
                // sceneContainer.changeTo<Scene1>()
                println("clicked!")

                val target = it.currentPosLocal
                // waypoint.visible = true
                // waypoint.pos = target

                // MOVE SURFER
                surfer.position(surfer.x, surfer.y)
                surferMovement(target)


            }

        suspend fun laserBoi() {
            laserOne.position(surfer.x, surfer.y)
            laserOne.visible = true
            laserOne.moveTo(laserOne.x, -15.0, 0.5.seconds, Easing.EASE)
        }


            suspend fun runJelly() {

                println("JELLYS RUNNING")
                awaitAll(async {
                    jellySchool.forEach {
                        // if (!it.visible || it.pos.y > height) {
                        delay((Random.nextInt(1, 3)).seconds)
                        val jellyX = Random.nextInt(buffer, (width.toInt() - buffer)).toDouble()
                        jellySwitchPurple = true
                        it.visible = true
                        it.position(jellyX, -5.0)

                        it.addUpdater {
                            if (surfer.collidesWith(this)) {
                                jellySwitchPurpleHit()
                                jellySwitchPurple = false
                                this.visible = false
                                println("Purple Jelly hits Surfer $jellyHits")
                            }
                        }

                        it.moveTo(jellyX + 75, 400.0, 1.seconds, Easing.EASE_IN)
                        it.moveTo(jellyX + 3, height - buffer, 1.seconds, Easing.EASE_IN)
                        it.moveTo(jellyX + 30, height + buffer, 1.seconds, Easing.EASE_IN)

                        //  }
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
                            if (surfer.collidesWith(this)) {
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
                }, async {
                    greenJellySchool.forEach {
                        //  if (!it.visible || it.pos.y > height) {
                        delay((Random.nextInt(1, 2)).seconds)
                        val jellyX = Random.nextInt(buffer, (width.toInt() - buffer)).toDouble()
                        jellySwitchGreen = true
                        it.visible = true
                        it.position(jellyX, -5.0)

                        it.addUpdater {
                            if (surfer.collidesWith(this)) {
                                jellySwitchGreenHit()
                                jellySwitchGreen = false
                                this.visible = false
                                println("Green Jelly hits Surfer $jellyHits")
                            }
                        }

                        it.moveTo(jellyX - 50, 400.0, 2.seconds, Easing.EASE_IN)
                        it.moveTo(jellyX + 15, height - buffer, 1.seconds, Easing.EASE_IN)
                        it.moveTo(jellyX + 30, height + buffer, 1.seconds, Easing.EASE_IN)


                        //  }
                    }
                }, async {
                    greenJellyThreeSchool.forEach {
                        //  if (!it.visible || it.pos.y > height) {
                        delay((Random.nextInt(1, 2)).seconds)
                        val jellyX = Random.nextInt(buffer, (width.toInt() - buffer)).toDouble()
                        jellySwitchGreen = true
                        it.visible = true
                        it.position(jellyX, -5.0)

                        it.addUpdater {
                            if (surfer.collidesWith(this)) {
                                jellySwitchGreenHit()
                                jellySwitchGreen = false
                                this.visible = false
                                println("Green Jelly hits Surfer $jellyHits")
                            }
                        }

                        it.moveTo(jellyX - 50, 400.0, 2.seconds, Easing.EASE_IN)
                        it.moveTo(jellyX + 15, height - buffer, 1.seconds, Easing.EASE_IN)
                        it.moveTo(jellyX + 30, height + buffer, 1.seconds, Easing.EASE_IN)


                        //  }
                    }
                }, async {
                    jellySchoolFour.forEach {
                        //  if (!it.visible || it.pos.y > height) {
                        delay((Random.nextInt(1, 2)).seconds)
                        val jellyX = Random.nextInt(buffer, (width.toInt() - buffer)).toDouble()
                        jellySwitchPurple = true
                        it.visible = true
                        it.position(jellyX, -5.0)

                        it.addUpdater {
                            if (surfer.collidesWith(this)) {
                                jellySwitchPurpleHit()
                                jellySwitchPurple = false
                                this.visible = false
                                println("Purple Jelly hits Surfer $jellyHits")
                            }
                        }

                        it.moveTo(jellyX - 50, 400.0, 2.seconds, Easing.EASE_IN)
                        it.moveTo(jellyX + 15, height - buffer, 1.seconds, Easing.EASE_IN)
                        it.moveTo(jellyX + 30, height + buffer, 1.seconds, Easing.EASE_IN)


                        //  }
                    }
                })
            }

            suspend fun jellyTimer() {
                while (levelIsActive) {
                    runJelly()
                }
            }

        suspend fun laserTimer() {
            while (levelIsActive) {
                laserBoi()
            }
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