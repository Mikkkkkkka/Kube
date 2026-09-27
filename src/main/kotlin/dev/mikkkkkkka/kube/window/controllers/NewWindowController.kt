package dev.mikkkkkkka.kube.window.controllers

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack

class NewWindowController {
    var window: Long = 0L
    var aspectRatio: Float = 1F

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
        GLFWErrorCallback.createPrint(System.err).set()

        if (!glfwInit()) error("Unable to initialize GLFW")

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(300, 300, "Hello World!", 0L, 0L)
        if (window == 0L) error("Failed to create the GLFW window")

        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true)
        }

        glfwSetFramebufferSizeCallback(window) { _, width, height ->
            if (glfwGetCurrentContext() == 0L) return@glfwSetFramebufferSizeCallback
            glViewport(0, 0, width, height)

            aspectRatio = width.toFloat() / height.toFloat()
        }

        MemoryStack.stackPush().use { stack ->
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
        glfwSwapInterval(1)

        glfwShowWindow(window)
    }

    private fun loop() {
        GL.createCapabilities()

        glClearColor(0f, 0f, 0f, 1f)

        // Координаты двух треугольников
        val vertices = floatArrayOf(
            -0.5f,  0.5f,
            0.5f,  0.5f,
            0.5f, -0.5f,

            -0.5f,  0.5f,
            0.5f, -0.5f,
            -0.5f, -0.5f
        )

        val vao = glGenVertexArrays()
        glBindVertexArray(vao)

        val vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        val vertexShaderSource = """
        #version 330 core

        layout (location = 0) in vec2 position;
        
        uniform float aspect;

        void main() {
            vec2 correctedPosition = position;
            correctedPosition.x /= aspect;
            
            gl_Position = vec4(correctedPosition, 0.0, 1.0);
        }
    """.trimIndent()

        val fragmentShaderSource = """
        #version 330 core

        out vec4 color;

        void main() {
            float t = gl_FragCoord.x / 300.0;
        
            color = vec4(0.0, t, 1.0-t, 1.0);
        }
    """.trimIndent()

        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, vertexShaderSource)
        glCompileShader(vertexShader)

        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fragmentShaderSource)
        glCompileShader(fragmentShader)

        val shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glLinkProgram(shaderProgram)

        val aspectLocation = glGetUniformLocation(shaderProgram, "aspect")

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        glVertexAttribPointer(
            0,
            2,
            GL_FLOAT,
            false,
            2 * Float.SIZE_BYTES,
            0L
        )
        glEnableVertexAttribArray(0)

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT)

            glUseProgram(shaderProgram)
            glUniform1f(aspectLocation, aspectRatio)
            println(aspectRatio)

            glBindVertexArray(vao)

            glDrawArrays(GL_TRIANGLES, 0, 6)

            glfwSwapBuffers(window)
            glfwPollEvents()
        }

        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
        glDeleteProgram(shaderProgram)
    }
}