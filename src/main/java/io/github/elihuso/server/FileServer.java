package io.github.elihuso.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.elihuso.Main;
import io.github.elihuso.data.Boom;
import io.github.elihuso.data.JavaResources;
import io.github.elihuso.logic.FileMIME;
import io.github.elihuso.logic.MathEx;
import io.github.elihuso.logic.Streaming;
import io.github.elihuso.module.Logger;
import io.github.elihuso.style.LoggerLevel;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class FileServer implements HttpHandler {

    private final File file;

    public FileServer(File file) {
        this.file = file;
    }

    public FileServer(String path) {
        this.file = new File(path);
    }

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

        if (!file.exists()) {
            response404(httpExchange);
            return;
        }

        if (file.isDirectory()) {
            if (file.listFiles() == null) {
                response404(httpExchange);
                return;
            }
            if (file.listFiles().length == 0) {
                response404(httpExchange);
                return;
            }
            for (var v : file.listFiles()) {
                if (v.isDirectory()) continue;
                if (v.getName().equalsIgnoreCase("index")) {
                    responseFile(httpExchange, v);
                    Logger.Log(LoggerLevel.NOTIFICATION, "Redirect " + file.getPath() + " to " + v.getPath());
                    return;
                }
                if (v.getName().equalsIgnoreCase("index.html")) {
                    responseFile(httpExchange, v);
                    Logger.Log(LoggerLevel.NOTIFICATION, "Redirect " + file.getPath() + " to " + v.getPath());
                    return;
                }
                if (v.getName().equalsIgnoreCase("index.htm")) {
                    responseFile(httpExchange, v);
                    Logger.Log(LoggerLevel.NOTIFICATION, "Redirect " + file.getPath() + " to " + v.getPath());
                    return;
                }
            }
            response404(httpExchange);
            return;
        }

        responseFile(httpExchange, file);
    }

    private void response404(HttpExchange httpExchange) throws IOException {
        String response = Streaming.ReadInputStream(JavaResources.getResource("404.html"));
        FileMIME fileMIME = new FileMIME();
        httpExchange.getResponseHeaders().add("Content-Type", fileMIME.getType(FilenameUtils.getExtension("404.html")));
        httpExchange.sendResponseHeaders(404, response.length());
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
        Logger.Log(LoggerLevel.WARNING, "Response 404");
    }

    private void responseFile(HttpExchange httpExchange, File v) throws IOException {
        byte[] response = Files.readAllBytes(v.toPath());;
        FileMIME fileMIME = new FileMIME();
        httpExchange.getResponseHeaders().add("Content-Type", fileMIME.getType("." + FilenameUtils.getExtension(v.getName())));
        httpExchange.sendResponseHeaders(200, MathEx.getByteLength(response));
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response);
        outputStream.close();
        Logger.Log(LoggerLevel.POSITIVE, "Response " + file.getPath());
    }

    private void responseBoom(HttpExchange httpExchange) throws IOException {
        Logger.Log(LoggerLevel.NOTIFICATION, "Preparing Boom...");
        httpExchange.getResponseHeaders().add("Content-Type", "text/html");
        Logger.Log(LoggerLevel.NOTIFICATION, "Added Content Type");
        Boom boom = new Boom(Main.loader);
        Logger.Log(LoggerLevel.NOTIFICATION, "Loading Boom...");
        if (!httpExchange.getRequestHeaders().containsKey("Accept-Encoding")) {
            Logger.Log(LoggerLevel.NOTIFICATION, "Use Gzip...");
            httpExchange.getResponseHeaders().add("Content-Encoding", "gzip");
            httpExchange.sendResponseHeaders(200, boom.getGzipLength());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(boom.getBoomGzipByte());
            outputStream.close();
        }
        else if ((!httpExchange.getRequestHeaders().getFirst("Accept-Encoding").contains("zstd")) && (!httpExchange.getRequestHeaders().getFirst("Accept-Encoding").contains("*"))) {
            Logger.Log(LoggerLevel.NOTIFICATION, "Use Gzip...");
            httpExchange.getResponseHeaders().add("Content-Encoding", "gzip");
            httpExchange.sendResponseHeaders(200, boom.getGzipLength());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(boom.getBoomGzipByte());
            outputStream.close();
        }
        else {
            Logger.Log(LoggerLevel.NOTIFICATION, "Use Zstd...");
            httpExchange.getResponseHeaders().add("Content-Encoding", "zstd");
            httpExchange.sendResponseHeaders(200, boom.getZstdLength());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(boom.getBoomZstdByte());
            outputStream.close();
        }
        Logger.Log(LoggerLevel.NEGATIVE, "Boom!");
    }
}
