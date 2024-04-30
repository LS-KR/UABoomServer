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
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, authentication");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        Headers headers = httpExchange.getRequestHeaders();
        Logger.Log(LoggerLevel.NOTIFICATION, headers.getFirst("User-Agent"));

        if ((headers.isEmpty()) || (!headers.containsKey("User-Agent"))) {
            Logger.Log(LoggerLevel.WARNING, "No User Agent Found");
            responseBoom(httpExchange);
            return;
        }

        String userAgent = headers.getFirst("User-Agent");

        if (!userAgent.contains(Main.agent)) {
            Logger.Log(LoggerLevel.NOTIFICATION, "User-Agent not matched");
            responseBoom(httpExchange);
            return;
        }

        byte[] response = Files.readAllBytes(Path.of(Main.specificFile));
        httpExchange.sendResponseHeaders(200, MathEx.getByteLength(response));
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response);
        outputStream.close();
        Logger.Log(LoggerLevel.NOTIFICATION, "Successfully response");
    }

    private void responseBoom(HttpExchange httpExchange) throws IOException {
        if (!httpExchange.getRequestHeaders().containsKey("Accept-Encoding")) {
            httpExchange.getResponseHeaders().add("Content-Encoding", "gzip");
            Boom boom = new Boom(Main.loader);
            httpExchange.sendResponseHeaders(200, boom.getGzipLength());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(boom.getBoomGzipByte());
            outputStream.close();
        }
        else if ((!httpExchange.getRequestHeaders().getFirst("Accept-Encoding").contains("zstd")) && (!httpExchange.getRequestHeaders().getFirst("Accept-Encoding").contains("*"))) {
            httpExchange.getResponseHeaders().add("Content-Encoding", "gzip");
            Boom boom = new Boom(Main.loader);
            httpExchange.sendResponseHeaders(200, boom.getGzipLength());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(boom.getBoomGzipByte());
            outputStream.close();
        }
        else {
            httpExchange.getResponseHeaders().add("Content-Encoding", "zstd");
            Boom boom = new Boom(Main.loader);
            httpExchange.sendResponseHeaders(200, boom.getZstdLength());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(boom.getBoomZstdByte());
            outputStream.close();
        }
        Logger.Log(LoggerLevel.NEGATIVE, "Boom!");
    }
}
