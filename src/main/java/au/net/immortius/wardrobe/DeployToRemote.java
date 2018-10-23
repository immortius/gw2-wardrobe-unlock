package au.net.immortius.wardrobe;

import au.net.immortius.wardrobe.config.Config;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class DeployToRemote {
    private static Logger logger = LoggerFactory.getLogger(DeployToLocal.class);

    private static final int MAX_ATTEMPTS = 5;

    private final Config config;
    private String address;
    private String username;
    private String password;

    public DeployToRemote(String address, String username, String password) throws IOException {
        this(Config.loadConfig(), address, username, password);
    }

    public DeployToRemote(Config config, String address, String username, String password) {
        this.config = config;
        this.address = address;
        this.username = username;
        this.password = password;
    }

    public static void main(String... args) throws Exception {
        Options options = new Options();
        options.addOption("username", true, "The username for uploading to an ftp site");
        options.addOption("password", true, "The password for uploading to an ftp site");
        options.addOption("address", true, "The address for uploading to an ftp site");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        new DeployToRemote(cmd.getOptionValue("address"), cmd.getOptionValue("username"), cmd.getOptionValue("password")).run();
    }

    public void run() throws IOException {
        logger.info("Deploying image maps and content to remote site {}", address);

        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(address);
        ftpClient.login(username, password);
        ftpClient.changeWorkingDirectory("/public_html/wardrobe-unlock-analyser/img");

        FTPFile[] ftpFiles = ftpClient.listFiles();
        ftpFiles[0].getTimestamp();
        Map<String, FTPFile> existing = Arrays.stream(ftpFiles).collect(Collectors.toMap(FTPFile::getName, x -> x));
        for (Path imgPath : Files.newDirectoryStream(config.paths.getImageMapPath())) {
            FTPFile remoteFile = existing.get(imgPath.getFileName().toString());
            if (remoteFile == null || Files.getLastModifiedTime(imgPath).toInstant().compareTo(remoteFile.getTimestamp().toInstant()) > 0) {
                logger.info("Uploading {} as remote file is newer or missing", imgPath.getFileName());
                try (InputStream imgStream = Files.newInputStream(imgPath)) {
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    upload(ftpClient, imgStream, imgPath.getFileName().toString());
                }
            }
        }
        ftpClient.changeWorkingDirectory("/public_html/wardrobe-unlock-analyser/data");
        logger.info("Uploading content.json");
        try (InputStream contentStream = Files.newInputStream(config.paths.contentFile)) {
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
            upload(ftpClient, contentStream, "content.json");
        }
    }

    private boolean upload(FTPClient ftpClient, InputStream stream, String toFile) throws IOException {
        boolean uploaded = false;
        for (int i = 0; i < MAX_ATTEMPTS && !uploaded; i++) {
            uploaded = ftpClient.storeFile(toFile, stream);
        }
        if (!uploaded) {
            logger.error("Failed to upload {} after {} attempts", toFile, MAX_ATTEMPTS);
        }
        return uploaded;
    }

}
