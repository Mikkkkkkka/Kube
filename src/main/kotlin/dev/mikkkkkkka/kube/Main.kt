package dev.mikkkkkkka.kube

import org.lwjgl.glfw.GLFW.*

fun main() {
    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    val width = 800
    val height = 600
    val window = glfwCreateWindow(width, height, "Hello LWJGL!", 0, 0)
    if (window == 0L) {
        throw IllegalStateException("Failed to create the GLFW window")
    }

    glfwMakeContextCurrent(window)

    while (!glfwWindowShouldClose(window)) {
        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
}