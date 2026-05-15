package org.testcharm.io;

import org.testcharm.util.Sneaky;
import org.testcharm.util.function.TriFunction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.testcharm.util.Sneaky.sneakyRun;

public class TempDirectory {
    private final Path root;

    public TempDirectory(Path root) {
        this.root = root;
        if (!Files.exists(root))
            Sneaky.run(() -> Files.createDirectory(root));
    }

    public TempDirectory() {
        this(Sneaky.get(() -> Files.createTempDirectory("fm-")));
    }

    public Path root() {
        return root;
    }

    public TempDirectory clean() {
        cleanSubs(root);
        return this;
    }

    private Path cleanSubs(Path dir) {
        Sneaky.run(() -> {
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.skip(1).forEach(sneakyRun(path -> {
                    if (Files.isRegularFile(path))
                        Files.delete(path);
                    else if (Files.isDirectory(path))
                        Files.delete(cleanSubs(path));
                }));
            }
        });
        return dir;
    }

    public Path write(VirtualFile file) {
        return write(file.getName(), file.binary());
    }

    public Path write(String name, byte[] binary) {
        Path path = resolve(name);
        Sneaky.run(() -> Files.write(path, binary));
        return path;
    }

    public Path write(String name, String text) {
        Path path = resolve(name);
        Sneaky.run(() -> Files.write(path, text.getBytes(StandardCharsets.UTF_8)));
        return path;
    }

    public TempDirectory mkdir(String sub) {
        return new TempDirectory(root.resolve(sub));
    }

    public Path resolve(String name) {
        return root.resolve(name);
    }

    public String readAllText(String name) {
        Path path = resolve(name);
        return Sneaky.get(() -> new String(Files.readAllBytes(path)));
    }

    public boolean exist(String path) {
        return Files.exists(resolve(path));
    }

    public static class Shared extends TempDirectory {
        private final Path remote;

        public Shared(Path root, Path remote) {
            super(root);
            this.remote = remote;
        }

        public Shared(Path root) {
            this(root, root);
        }

        public Shared() {
            super();
            remote = root();
        }

        public <A> Path remoteOf(BiFunction<TempDirectory, A, Path> pathFunction, A arg) {
            Path local = pathFunction.apply(this, arg);
            return remoteOf(local);
        }

        public <A1, A2> Path remoteOf(TriFunction<TempDirectory, A1, A2, Path> pathFunction, A1 arg1, A2 arg2) {
            Path local = pathFunction.apply(this, arg1, arg2);
            return remoteOf(local);
        }

        @Override
        public TempDirectory.Shared mkdir(String sub) {
            return new Shared(super.mkdir(sub).root(), remote.resolve(sub));
        }

        public Path remoteOf(Path local) {
            return remote.resolve(root().relativize(local));
        }

        public Path localOf(Path remote) {
            return root().resolve(this.remote.relativize(remote));
        }

        @Override
        public Shared clean() {
            return (Shared) super.clean();
        }

        public Path remote() {
            return remote;
        }
    }
}
