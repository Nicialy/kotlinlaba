package ru.ac.uniyar.endpoints // ktlint-disable filename

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.path
import ru.ac.uniyar.utils.BadRequestException
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class PhotoGet() : HttpHandler {
    override fun invoke(request: Request): Response {
        val fileName = request.path("name") ?: throw BadRequestException("Нужен путь файла")
        if (Files.notExists(Paths.get("./photo/$fileName"))) {
            throw BadRequestException("Файла не существует")
        }
        val image = File("./photo/$fileName")
        return Response(OK)
            .body(ByteArrayInputStream(image.readBytes())) // ByteArrayInputStream(image))
            .header("Content-Type", "image/png")
            .header("Content-Length", image.length().toString()) // bytes.size.toString())
    }
}
