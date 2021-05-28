package com.marathon.alephone.plugins;

public class Plugin {
    public final String path;
    public final String name;
    public final String description;
    public final String version;

    public String minVersion;
    public String hudLua;
    public String soloLua;
    public String themeDir;

    public Plugin(String path, String name, String description, String version) {
        this.path = path;
        this.name = name;
        this.description = description;
        this.version = version;
    }
}
