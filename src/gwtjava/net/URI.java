package gwtjava.net;

public class URI {
    private java.net.URI uri;
    public URI(String uri) throws URISyntaxException {
        try {
            this.uri = new java.net.URI(uri);
        } catch (java.net.URISyntaxException e) {
            throw new URISyntaxException(e.getMessage());
        }
    }

    public URI(String scheme, String ssp, String fragment) throws URISyntaxException {
        try {
            this.uri = new java.net.URI(scheme, ssp, fragment);
        } catch (java.net.URISyntaxException e) {
            throw new URISyntaxException(e.getMessage());
        }
    }

    private URI(java.net.URI uri) {
        this.uri = uri;
    }

    public boolean isAbsolute() {
        return uri.isAbsolute();
    }

    public URI normalize() {
        return new URI(uri.normalize());
    }

    public String getPath() {
        return uri.getPath();
    }

    public URL toURL() throws MalformedURLException {
        try {
            return new URL(uri.toURL());
        } catch (java.net.MalformedURLException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    public String getSchemeSpecificPart() {
        return uri.getSchemeSpecificPart();
    }

}
