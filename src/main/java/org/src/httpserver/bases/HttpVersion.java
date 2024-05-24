package org.src.httpserver.bases;

public enum HttpVersion {
    HTTP11(1, 1);

    int major;
    int minor;

    HttpVersion(int major, int minor) {
        if (major < 0 && minor < 0)
            throw new NumberFormatException("Version must be a positive integer: " + major + "," + minor);
        this.major = major;
        this.minor = minor;
    }

    public static HttpVersion get(String version){
        for (var v : HttpVersion.values()){
            if (v.toString().equals(version)){
                return v;
            }
        }
        return null;
    }
    @Override
    public String toString() {
        return "HTTP/" + major + "." + minor;
    }
}
