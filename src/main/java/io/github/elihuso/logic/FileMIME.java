package io.github.elihuso.logic;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.github.elihuso.components.MIME;
import io.github.elihuso.data.JavaResources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileMIME {

    private final List<MIME> mimes = new ArrayList<>();

    public FileMIME() {
        String rawMime = Streaming.ReadInputStream(JavaResources.getResource("mime.json"));
        JsonArray jsonArray = new Gson().fromJson(rawMime, JsonArray.class);
        for (int i = 0; i < jsonArray.size(); ++i) {
            String name = jsonArray.get(i).getAsJsonObject().get("name").getAsString();
            String type = jsonArray.get(i).getAsJsonObject().get("type").getAsString();
            String extension = jsonArray.get(i).getAsJsonObject().get("extension").getAsString();
            String details = jsonArray.get(i).getAsJsonObject().get("details").getAsString();
            mimes.add(new MIME(name, type, extension, details));
        }
    }

    public List<MIME> getMimes() {
        return mimes;
    }

    public String getType(String extension) {
        for (var v : mimes) {
            if (v.getExtension().equalsIgnoreCase(extension))
                return v.getType();
        }
        return "";
    }
}
