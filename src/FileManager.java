import java.nio.file.*;
import java.io.*;

public class FileManager {
    private final Path ffmpegPath;
    private final Path outputFolderPath;

    public FileManager() {
        Path applicationPath = Paths.get(System.getProperty("user.dir"));
        ffmpegPath = initFFmpegPath(applicationPath);
        outputFolderPath = initOutputFolder(applicationPath);
    }

    private Path initFFmpegPath(Path applicationPath) {
        Path ffmpeg = applicationPath.resolve("ffmpeg").resolve("ffmpeg.exe");
        return Files.exists(ffmpeg) ? ffmpeg : Paths.get("ffmpeg");
    }

    private Path initOutputFolder(Path applicationPath) {
        Path outputPath = applicationPath.resolve("output");
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputPath;
    }

    public Path getFFmpegPath() {
        return ffmpegPath;
    }

    public Path getOutputPath() {
        return outputFolderPath;
    }

    public Path resolveOutputFile(String filename) {
        return outputFolderPath.resolve(filename);
    }
}
