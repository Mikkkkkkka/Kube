package dev.mikkkkkkka.kube

import dev.mikkkkkkka.kube.window.controllers.PixelWindowController

const val IMAGE_WIDTH = 300
const val IMAGE_HEIGHT = 300

fun main() {
    PixelWindowController(
        IMAGE_WIDTH,
        IMAGE_HEIGHT
    ).run()
}