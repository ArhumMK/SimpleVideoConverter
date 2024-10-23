import java.util.concurrent.TimeUnit;
import javax.swing.*;
import java.io.*;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FFmpegHandler {
    private final FileManager fileManager;

    public FFmpegHandler(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public String buildCommand(String inputFile, String outputName, String resolution,
                               String format, String aspectRatio) {
        StringBuilder command = new StringBuilder();
        command.append(fileManager.getFFmpegPath().toString())
                .append(" -i \"").append(inputFile).append("\"");

        // Add resolution parameter
        if (!resolution.equals("Keep Original")) {
            String scale = switch (resolution) {
                case "480p (854x480)" -> "854:480";
                case "720p (1280x720)" -> "1280:720";
                case "1080p (1920x1080)" -> "1920:1080";
                case "2K (2560x1440)" -> "2560:1440";
                case "4K (3840x2160)" -> "3840:2160";
                default -> null;
            };
            if (scale != null) {
                command.append(" -vf scale=").append(scale);
            }
        }

        // Add aspect ratio crop
        if (!aspectRatio.equals("Keep Original")) {
            command.append(" -vf \"crop=");
            switch (aspectRatio) {
                case "16:9 Landscape" -> command.append("iw:iw*9/16");
                case "9:16 Portrait" -> command.append("ih*9/16:ih");
                case "1:1 Square" -> command.append("min(iw,ih):min(iw,ih)");
                case "4:3 Classic" -> command.append("iw:iw*3/4");
                case "21:9 Ultrawide" -> command.append("iw:iw*9/21");
            }
            command.append("\"");
        }

        // Handle output format
        if (!format.equals("Keep Original")) {
            String extension = format.toLowerCase();
            if (!outputName.toLowerCase().endsWith("." + extension)) {
                outputName += "." + extension;
            }
        }

        Path outputPath = fileManager.resolveOutputFile(outputName);
        command.append(" -y \"").append(outputPath.toString()).append("\"");

        return command.toString();
    }

    public void executeCommand(String command, Consumer<String> logCallback,
                               Runnable onComplete) {
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                Process process = Runtime.getRuntime().exec(command);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    publish(line);
                }

                process.waitFor(30, TimeUnit.SECONDS);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(logCallback);
            }

            @Override
            protected void done() {
                onComplete.run();
            }
        }.execute();
    }
}