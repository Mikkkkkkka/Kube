package dev.mikkkkkkka.kube.window.controllers

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer

class ExperimentalWindowController {
    var window: Long = 0L

    fun run() {
        println("Running LWJGL version - ${Version.getVersion()}!")

        init()
        loop()

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun init() {
        GLFWErrorCallback.createPrint(System.err).set()

        if (!glfwInit()) error("Unable to initialize GLFW")

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(800, 800, "Basic Window", 0L, 0L)
        if (window == 0L) error("Failed to create the GLFW window")

        glfwSetKeyCallback(window) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(window, true)
        }

        glfwSetWindowSizeCallback(window) { window: Long, width: Int, height: Int ->
            draw()
            putWindowInCenter(window, width, height)
            glViewport(0, 0, width, height)
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth: IntBuffer = stack.mallocInt(1)
            val pHeight: IntBuffer = stack.mallocInt(1)
            glfwGetWindowSize(window, pWidth, pHeight)
            putWindowInCenter(window, pWidth.get(0), pHeight.get(0))
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)

        glfwShowWindow(window)
    }

    private fun loop() {
        GL.createCapabilities()

        glClearColor(0f, 0f, 0f, 1f)

        while (!glfwWindowShouldClose(window)) draw()
    }

    private fun draw() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        drawSquare(a = 1f)

        glfwSwapBuffers(window)
        glfwPollEvents()
    }

    private fun drawSquare(a: Float) {
        glColor3f(1f, 0f, 0f)

        glBegin(GL_QUADS)

        val squareSize: Float = a / 2

        glVertex2f(-squareSize, squareSize)
        glVertex2f(squareSize, squareSize)
        glVertex2f(squareSize, -squareSize)
        glVertex2f(-squareSize, -squareSize)

        glEnd()
    }

    private fun putWindowInCenter(window: Long, width: Int, height: Int) {
        val vidMode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!
        glfwSetWindowPos(
            window,
            (vidMode.width() - width) / 2,
            (vidMode.height() - height) / 2,
        )
    }
}