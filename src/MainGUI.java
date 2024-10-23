import javax.swing.*;
import java.awt.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainGUI extends JFrame {
    private final FileManager fileManager;
    private final FFmpegHandler ffmpegHandler;

    private JTextField inputFileField;
    private JTextField outputFileField;
    private JComboBox<String> resolutionBox;
    private JComboBox<String> formatBox;
    private JComboBox<String> aspectRatioBox;
    private JProgressBar progressBar;
    private JTextArea logArea;

    private static final String[] RESOLUTIONS = {
            "Keep Original",
            "480p (854x480)",
            "720p (1280x720)",
            "1080p (1920x1080)",
            "2K (2560x1440)",
            "4K (3840x2160)"
    };

    private static final String[] FORMATS = {
            "Keep Original",
            "MP4",
            "AVI",
            "MKV",
            "MOV",
            "WebM"
    };

    private static final String[] ASPECT_RATIOS = {
            "Keep Original",
            "16:9 Landscape",
            "9:16 Portrait",
            "1:1 Square",
            "4:3 Classic",
            "21:9 Ultrawide"
    };

    public MainGUI() {
        fileManager = new FileManager();
        ffmpegHandler = new FFmpegHandler(fileManager);
        setupGUI();
    }

    private void setupGUI() {
        setTitle("Simple FFmpeg Video Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        addFileSelectionPanel(mainPanel, gbc);
        addVideoOptionsPanel(mainPanel, gbc);
        addProgressAndLogPanel(mainPanel, gbc);

        JButton processButton = new JButton("Process Video");
        processButton.addActionListener(e -> processVideo());

        add(mainPanel, BorderLayout.CENTER);
        add(processButton, BorderLayout.SOUTH);

        pack();
        setSize(500, 600);
        setLocationRelativeTo(null);
    }

    private void addFileSelectionPanel(JPanel mainPanel, GridBagConstraints gbc) {
        JPanel panel = new JPanel(new GridLayout(2, 3, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("File Selection"));

        inputFileField = new JTextField();
        outputFileField = new JTextField();

        JButton inputBrowseButton = new JButton("Browse");
        inputBrowseButton.addActionListener(e -> browseFile());

        JButton outputBrowseButton = new JButton("Browse");
        outputBrowseButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter output filename:");
            if (name != null && !name.trim().isEmpty()) {
                outputFileField.setText(name);
            }
        });

        panel.add(new JLabel("Input File:"));
        panel.add(inputFileField);
        panel.add(inputBrowseButton);
        panel.add(new JLabel("Output Name:"));
        panel.add(outputFileField);
        panel.add(outputBrowseButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(panel, gbc);
    }

    private void addVideoOptionsPanel(JPanel mainPanel, GridBagConstraints gbc) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Video Options"));

        resolutionBox = new JComboBox<>(RESOLUTIONS);
        formatBox = new JComboBox<>(FORMATS);
        aspectRatioBox = new JComboBox<>(ASPECT_RATIOS);

        panel.add(new JLabel("Resolution:"));
        panel.add(resolutionBox);
        panel.add(new JLabel("Format:"));
        panel.add(formatBox);
        panel.add(new JLabel("Aspect Ratio:"));
        panel.add(aspectRatioBox);

        gbc.gridy = 1;
        mainPanel.add(panel, gbc);
    }

    private void addProgressAndLogPanel(JPanel mainPanel, GridBagConstraints gbc) {
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        gbc.gridy = 2;
        mainPanel.add(progressBar, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, gbc);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Video Files", "mp4", "avi", "mkv", "mov", "webm");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            inputFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void processVideo() {
        if (inputFileField.getText().isEmpty() || outputFileField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select input file and specify output name.");
            return;
        }

        progressBar.setValue(0);
        String command = ffmpegHandler.buildCommand(
                inputFileField.getText(),
                outputFileField.getText(),
                (String) resolutionBox.getSelectedItem(),
                (String) formatBox.getSelectedItem(),
                (String) aspectRatioBox.getSelectedItem()
        );

        logArea.setText("Executing command: " + command + "\n");

        ffmpegHandler.executeCommand(
                command,
                line -> logArea.append(line + "\n"),
                () -> {
                    progressBar.setValue(100);
                    JOptionPane.showMessageDialog(this,
                            "Processing complete!\nOutput saved to: " +
                                    fileManager.getOutputPath().toString());
                    progressBar.setValue(0);
                }
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainGUI().setVisible(true);
        });
    }
}