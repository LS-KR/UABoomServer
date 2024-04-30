package io.github.elihuso;

import com.sun.net.httpserver.HttpServer;
import io.github.elihuso.logic.Streaming;
import io.github.elihuso.module.Logger;
import io.github.elihuso.server.FileServer;
import io.github.elihuso.server.SpecificServer;
import io.github.elihuso.style.LoggerLevel;
import org.ini4j.Ini;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    @Option(name = "-c", usage = "Config Path")
    public static String configPath = "./config.ini";

    @Argument
    private List<String> arguments = new ArrayList<String>();

    public static String agent = "";
    public static String mode = "specific";
    public static String specificFile = "text.file";
    public static int port = 10808;

    public static List<String> serverFileList = new ArrayList<>();
    public static HttpServer server;
    public static boolean running = true;
    public static ClassLoader loader;

    public static void main(String[] args) {
        loader = Thread.currentThread().getContextClassLoader();
        new Main().doMain(args);
    }

    public void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        }
        catch (Exception ex) {
            Logger.Log(LoggerLevel.NEGATIVE, "Error in parse arguments");
            Logger.Log(LoggerLevel.WARNING, "usage: UABoomServer [-c config]");
            return;
        }
        try {
            initialConfig();
        }
        catch (IOException ex) {
            Logger.Log(LoggerLevel.NEGATIVE, "Error in load config: " + ex.getLocalizedMessage());
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        }
        catch (IOException ex) {
            Logger.Log(LoggerLevel.NEGATIVE, "Error on creating server: " + ex.getLocalizedMessage());
            return;
        }
        if (mode.equalsIgnoreCase("specific")) {
            Logger.Log(LoggerLevel.POSITIVE, "Use Specific File: " + specificFile);
            server.createContext("/", new SpecificServer());
        }
        if (mode.equalsIgnoreCase("server")) {
            for (var v : serverFileList) {
                server.createContext(v.replaceFirst("server/", "/"), new FileServer(v));
                Logger.Log(LoggerLevel.POSITIVE, v.replaceFirst("server/", "/") + " -> " + v);
            }
        }
        server.getExecutor();
        server.start();
        Logger.Log(LoggerLevel.NOTIFICATION, "Server Started.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            Logger.Log(LoggerLevel.WARNING, "Closing Server...");
            Main.server.stop(0);
            Logger.Log(LoggerLevel.NOTIFICATION, "Goodbye!");
            Main.running = false;
        }));
        while (running);
    }

    public static void initialConfig() throws IOException {
        File config = new File(configPath);
        if (!config.exists()) {
            config.createNewFile();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream is = loader.getResourceAsStream("config.ini");
            Files.write(config.toPath(), Streaming.ByteInputStream(is));
            is = loader.getResourceAsStream("text.file");
            Path textPath = Path.of("./text.file");
            Files.createFile(textPath);
            Files.write(textPath, Streaming.ByteInputStream(is));
        }
        Ini ini = new Ini(config);
        if (ini.get("DEFAULT").containsKey("agent")) {
            agent = ini.get("DEFAULT", "agent");
            Logger.Log(LoggerLevel.NOTIFICATION, "Agent: " + agent);
        }
        if (ini.get("DEFAULT").containsKey("mode")) {
            mode = ini.get("DEFAULT", "mode");
            Logger.Log(LoggerLevel.NOTIFICATION, "Mode: " + mode);
        }
        if (ini.get("DEFAULT").containsKey("port")) {
            port = Integer.parseInt(ini.get("DEFAULT", "port"));
            Logger.Log(LoggerLevel.NOTIFICATION, "Port: " + port);
        }
        if (mode.equalsIgnoreCase("specific")) {
            if (ini.get("SPECIFIC").containsKey("file")) {
                specificFile = ini.get("SPECIFIC", "file");
                Logger.Log(LoggerLevel.NOTIFICATION, "Specific File: " + specificFile);
            }
        }
        else if (mode.equalsIgnoreCase("server")) {
            if (Files.isDirectory(Path.of("./server"))) {
                File serverPath = new File("./server");
                serverFileList.add(new File(".").toURI().relativize(new File(serverPath.getAbsolutePath()).toURI()).getPath());
                addFiles(serverPath.listFiles());
            }
            else {
                throw new InvalidObjectException("directory ./server not found");
            }
        }
        else {
            throw new InvalidObjectException("Invalid mode");
        }
    }

    public static void addFiles(File[] files) {
        for (var v : files) {
            serverFileList.add(new File(".").toURI().relativize(new File(v.getAbsolutePath()).toURI()).getPath());
            if (v.isDirectory()) {
                if (v.listFiles() == null) continue;
                if (v.listFiles().length == 0) continue;
                addFiles(v.listFiles());
            }
        }
    }
}