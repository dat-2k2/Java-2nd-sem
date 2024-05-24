package org.src.httpserver.bases;

public enum HttpVersion {
    HTTP_1_1(1, 1);

    final int major;
    final int minor;

    HttpVersion(int major, int minor) {
        if (major < 0 && minor < 0)
            throw new NumberFormatException("Version must be a positive integer: " + major + "," + minor);
        this.major = major;
        this.minor = minor;
    }

    public static HttpVersion get(String version) {
        for (var v : HttpVersion.values()) {
            if (v.toString().equals(version)) {
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
