package gwtjava.util.jar;

public class Manifest {

    java.util.jar.Manifest manifest;
    public Manifest(java.util.jar.Manifest manifest) {
        this.manifest = manifest;
    }

    public Attributes getMainAttributes() {
        return new Attributes(manifest.getMainAttributes());
    }

}
