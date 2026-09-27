package dev.mikkkkkkka.kube.window.controllers

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil

class PixelWindowController(
    private val imageWidth: Int,
    private val imageHeight: Int,
) {
    private var window: Long = 0L

    private var vao = 0
    private var vbo = 0
    private var texture = 0
    private var shaderProgram = 0

    fun run() {
        println("Hello LWJGL ${Version.getVersion()}!")

        initWindow()
        initOpenGL()

        loop()

        cleanup()
    }

    private fun initWindow() {
        GLFWErrorCallback.createPrint(System.err).set()

        check(glfwInit()) { "Unable to initialize GLFW" }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        window = glfwCreateWindow(
            /* width = */ imageWidth,
            /* height = */ imageHeight,
            /* title = */ "Pixel Renderer",
            /* monitor = */ 0L,
            /* share = */ 0L,
        )
        check(window != 0L) { "Failed to create the GLFW window" }

        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(window, true)
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)

        glfwShowWindow(window)
    }

    private fun initOpenGL() {
        GL.createCapabilities()

        glfwSetWindowSizeCallback(window) { _, width, height -> glViewport(0, 0, width, height) }

        // aboba
        createQuad()
        createTexture()
        createShaders()

        glClearColor(0f, 0f, 0f, 1f)
    }

    private fun createQuad() {
        // x, y,    u, v
        val vertices = floatArrayOf(
            -1f, 1f,    0f, 0f,
            1f, 1f,     1f, 0f,
            1f, -1f,    1f, 1f,

            -1f, 1f,    0f, 0f,
            1f, -1f,    1f, 1f,
            -1f, -1f,   0f, 1f
        )

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        vbo = glGenBuffers()
        glBindBuffer(
            /* target = */ GL_ARRAY_BUFFER,
            /* buffer = */ vbo,
        )

        glBufferData(
            /* target = */ GL_ARRAY_BUFFER,
            /* data = */ vertices,
            /* usage = */ GL_STATIC_DRAW,
        )

        val stride = 4 * Float.SIZE_BYTES

        glVertexAttribPointer(
            /* index = */ 0,
            /* size = */ 2,
            /* type = */ GL_FLOAT,
            /* normalized = */ false,
            /* stride = */ stride,
            /* pointer = */ 0L,
        )

        glEnableVertexAttribArray(0)

        glVertexAttribPointer(
            /* index = */ 1,
            /* size = */ 2,
            /* type = */ GL_FLOAT,
            /* normalized = */ false,
            /* stride = */ stride,
            /* pointer = */ (2 * Float.SIZE_BYTES).toLong(),
        )

        glEnableVertexAttribArray(1)
    }

    private fun createTexture() {
        texture = glGenTextures()

        glBindTexture(GL_TEXTURE_2D, texture)

        glTexParameteri(
            /* target = */ GL_TEXTURE_2D,
            /* pname = */ GL_TEXTURE_MIN_FILTER,
            /* param = */ GL_NEAREST,
        )

        glTexParameteri(
            /* target = */ GL_TEXTURE_2D,
            /* pname = */ GL_TEXTURE_MAG_FILTER,
            /* param = */ GL_NEAREST,
        )

        glTexImage2D(
            /* target = */ GL_TEXTURE_2D,
            /* level = */ 0,
            /* internalformat = */ GL_RGBA8,
            /* width = */ imageWidth,
            /* height = */ imageHeight,
            /* border = */ 0,
            /* format = */ GL_RGBA,
            /* type = */ GL_UNSIGNED_BYTE,
            /* pixels = */ 0L,
        )
    }

    private fun createShaders() {
        val vertexShaderSource = """
            #version 330 core
    
            layout (location = 0) in vec2 position;
            layout (location = 1) in vec2 texturePosition;
            
            out vec2 uv;
    
            void main() {
                gl_Position = vec4(position, 0.0, 1.0);
                
                uv = texturePosition;
            }
        """.trimIndent()

        val fragmentShaderSource = """
            #version 330 core

            in vec2 uv;
            
            out vec4 color;
            
            uniform sampler2D image;
    
            void main() {
                color = texture(image, uv);
            }
        """.trimIndent()

        val vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource)

        shaderProgram = glCreateProgram()

        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)

        glLinkProgram(shaderProgram)

        check(glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_TRUE) {
            glGetProgramInfoLog(shaderProgram)
        }

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        val imageLocation = glGetUniformLocation(shaderProgram, "image")

        glUniform1i(imageLocation, 0)
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = glCreateShader(type)

        glShaderSource(shader, source)
        glCompileShader(shader)

        check(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_TRUE) {
            glGetShaderInfoLog(shader)
        }

        return shader
    }

    private fun loop() {
        val pixelBuffer = MemoryUtil.memAlloc(
            imageWidth * imageHeight * 4
        )

        try {
            while (!glfwWindowShouldClose(window)) {
                // Pseudo-reading the image
                for (y in 0 until imageHeight) {
                    for (x in 0 until imageWidth) {

                        val r = x * 255 / imageWidth
                        val g = y * 255 / imageHeight
                        val b = 128
                        val a = 255

                        pixelBuffer.put(r.toByte())
                        pixelBuffer.put(g.toByte())
                        pixelBuffer.put(b.toByte())
                        pixelBuffer.put(a.toByte())
                    }
                }
                pixelBuffer.flip()

                glBindTexture(GL_TEXTURE_2D, texture)

                glTexSubImage2D(
                    /* target = */ GL_TEXTURE_2D,
                    /* level = */ 0,
                    /* xoffset = */ 0,
                    /* yoffset = */ 0,
                    /* width = */ imageWidth,
                    /* height = */ imageHeight,
                    /* format = */ GL_RGBA,
                    /* type = */ GL_UNSIGNED_BYTE,
                    /* pixels = */ pixelBuffer,
                )

                pixelBuffer.clear()

                /*
                 * Render
                 */

                glClear(GL_COLOR_BUFFER_BIT)

                glUseProgram(shaderProgram)

                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, texture)

                glBindVertexArray(vao)

                glDrawArrays(
                    /* mode = */ GL_TRIANGLES,
                    /* first = */ 0,
                    /* count = */ 6,
                )

                glfwSwapBuffers(window)
                glfwPollEvents()
            }
        } finally {
            MemoryUtil.memFree(pixelBuffer)
        }
    }

    private fun cleanup() {
        glDeleteTextures(texture)
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
        glDeleteProgram(shaderProgram)

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}