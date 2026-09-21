package dev.mikkkkkkka.kube

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack

class BasicWindowController {
    var window = 0L

    fun run() {
        println("Hello LWJGL ${Version.getVersion()}!")

        init()
        loop()

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun init() {
        GLFWErrorCallback.createPrint(System.err).set() // Direct errors to System.err

        if (!glfwInit()) error("Unable to initialize GLFW")

        glfwDefaultWindowHints() // Hints = window parameters
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(300, 300, "Hello World!", 0L, 0L)
        if (window == 0L) error("Failed to create the GLFW window")

        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true)
        }

        // Put window in the center of the screen
        MemoryStack.stackPush().use { stack -> // MemoryStack = Kostyl for C APIs
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            glfwGetWindowSize(window, pWidth, pHeight)

            val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            )
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1) // V-sync

        glfwShowWindow(window)
    }

    private fun loop() {
        GL.createCapabilities()

        glClearColor(.0f, .0f, .0f, .0f)

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            glfwSwapBuffers(window)

            glfwPollEvents()
        }
    }
}