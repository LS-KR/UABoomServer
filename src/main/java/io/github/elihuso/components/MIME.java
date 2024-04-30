package io.github.elihuso.components;

public class MIME {
    private final String name;
    private final String type;
    private final String extension;
    private final String details;

    public MIME(String name, String type, String extension, String details) {
        this.name = name;
        this.type = type;
        this.extension = extension;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public String getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }
}
