package gwtjava.net;

public class URI {
    private String uri;
    public URI(String uri) throws URISyntaxException {
        if (!uri.startsWith("file:/")) {
            throw new URISyntaxException(uri);
        }
        this.uri = uri;
    }

    public URI(String scheme, String ssp, String fragment) throws URISyntaxException {
        throw new UnsupportedOperationException();
    }

    public boolean isAbsolute() {
        throw new UnsupportedOperationException();
    }

    public URI normalize() {
        return this;
    }

    public String getPath() {
        throw new UnsupportedOperationException();
    }

    public URL toURL() throws MalformedURLException {
        return new URL(uri);
    }

    public String getSchemeSpecificPart() {
        return uri.substring(5);
    }

}
