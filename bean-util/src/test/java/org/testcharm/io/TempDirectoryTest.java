package org.testcharm.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.Sneaky;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testcharm.dal.Assertions.expect;

class TempDirectoryTest {
    TempDirectory tempDirectory = new TempDirectory();

    @BeforeEach
    void clean() {
        tempDirectory.clean();
    }

    @Test
    void create_binary_file() {
        Path file = tempDirectory.write(new VirtualFile() {
            @Override
            public String getName() {
                return "hello";
            }

            @Override
            public byte[] binary() {
                return new byte[]{1};
            }
        });
        expect(tempDirectory.root()).should("hello.binary: [1y]");
        expect(file).should("binary: [1y]");
    }

    @Test
    void clean_file() {
        tempDirectory.write(new VirtualFile() {
            @Override
            public String getName() {
                return "hello";
            }

            @Override
            public byte[] binary() {
                return new byte[]{1};
            }
        });

        tempDirectory.clean();
        expect(tempDirectory.root()).should(": []");
    }

    @Test
    void create_write_clean_sub_dir() {
        TempDirectory sub = tempDirectory.mkdir("sub");

        expect(tempDirectory.root()).should("sub: {...}");

        sub.write("file", new byte[]{2});

        expect(tempDirectory.root()).should("sub.file.binary: [2]");

        tempDirectory.clean();
        expect(tempDirectory.root()).should("[]");
    }

    @Nested
    class SharedDir {
        TempDirectory tempRoot = new TempDirectory(Sneaky.get(() -> Files.createTempDirectory("tm-")));
        TempDirectory.Shared shared = new TempDirectory.Shared(tempRoot.root(), Paths.get("/remote"));

        @AfterEach
        public void clean() {
            tempRoot.clean();
            tempRoot.root().toFile().delete();
        }

        @Test
        void create_file_with_shared() {
            Path remote = shared.remoteOf(TempDirectory::write, "file", "any");

            expect(remote.toString()).should(": '/remote/file'");
        }

        @Test
        void local_path() {
            Path local = shared.localOf(shared.remoteOf(TempDirectory::write, "file", "any"));

            expect(local).isEqualTo(tempRoot.root().resolve("file"));
        }

        @Test
        void sub_shared() {
            TempDirectory.Shared subShared = shared.mkdir("sub");

            Path remote = subShared.remoteOf(TempDirectory::write, "file", "any");

            expect(remote.toString()).should(": '/remote/sub/file'");

            expect(subShared.localOf(remote)).isEqualTo(subShared.root().resolve("file"));
        }
    }
}