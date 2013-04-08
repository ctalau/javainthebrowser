package gwtjava.net;

public class URI {

    private String uri;
    public URI(String uri) throws URISyntaxException {
        this.uri = uri;
    }

    public URI(String object, String string, String object2) throws URISyntaxException {
        throw new UnsupportedOperationException();
    }

    public boolean isAbsolute() {
        return uri.startsWith("file:/");
    }

    public URI normalize() {
        throw new UnsupportedOperationException();
    }

    public String getPath() {
        throw new UnsupportedOperationException();
    }

    public URL toURL() throws MalformedURLException {
        return new URL(uri);
    }

    public String getSchemeSpecificPart() {
        throw new UnsupportedOperationException();
    }

}
