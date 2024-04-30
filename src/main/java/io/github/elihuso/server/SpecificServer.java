package io.github.elihuso.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.elihuso.Main;
import io.github.elihuso.data.Boom;
import io.github.elihuso.logic.MathEx;
import io.github.elihuso.module.Logger;
import io.github.elihuso.style.LoggerLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpecificServer implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Headers headers = httpExchange.getRequestHeaders();
        String userAgent = headers.getFirst("User-Agent");

        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, authentication");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        if (!userAgent.contains(Main.agent)) {
            httpExchange.getResponseHeaders().add("Content-Encoding", "zstd");
            Boom boom = new Boom();
            httpExchange.sendResponseHeaders(200, boom.getLength());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(boom.getBoomByte());
            outputStream.close();
            Logger.Log(LoggerLevel.POSITIVE, "Boom!");
            return;
        }

        byte[] response = Files.readAllBytes(Path.of(Main.specificFile));
        httpExchange.sendResponseHeaders(200, MathEx.getByteLength(response));
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response);
        outputStream.close();
        Logger.Log(LoggerLevel.NOTIFICATION, "Successfully response");
    }
}
