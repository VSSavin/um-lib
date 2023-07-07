package com.github.vssavin.umlib.config;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by vssavin on 16.05.2022.
 */
@Component
public class SqlScriptsConfig {
    public static final String SCRIPTS_DEFAULT_DIRECTORY = "sqlScripts";
    private static final Logger log = LoggerFactory.getLogger(SqlScriptsConfig.class);

    @Autowired
    public SqlScriptsConfig(@Qualifier("umDataSource") DataSource umDataSource) {
        ArrayList<String> sqlFiles = new ArrayList<>();
        sqlFiles.add("/init.sql");
        executeSqlScripts(umDataSource, SCRIPTS_DEFAULT_DIRECTORY, sqlFiles);
    }

    public void executeSqlScripts(DataSource dataSource, String scriptsDirectory, List<String> sourceFiles) {
        Map<String, InputStream> fileStreams = new HashMap<>();

        if (!scriptsDirectory.isEmpty()) {
            Path path = null;
            try {
                path = Paths
                        .get(Objects.requireNonNull(getClass().getClassLoader().getResource(scriptsDirectory)).toURI());
            } catch (Exception e) {
                log.warn("Directory " + scriptsDirectory + " not found!");
            }

            if (path != null) {
                try (Stream<Path> paths = Files.walk(path)) {
                    paths
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(".sql"))
                            .forEach(p -> {
                                try {
                                    fileStreams.put(p.getFileName().toString(),
                                            new FileInputStream(new File(p.toUri())));
                                } catch (FileNotFoundException e) {
                                    log.error("File not found: file = " + p.getFileName());
                                }
                            });
                } catch (Exception e) {
                    log.error("Searching sql files error: ", e);
                }
            }
        }

        for (String sourceFile : sourceFiles) {
            InputStream resourceStream;
            if (sourceFile.endsWith(".sql")) {
                try {
                    resourceStream = getClass().getResourceAsStream(sourceFile);
                    if (resourceStream == null) {
                        log.warn("Resource is null! File: " + sourceFile);
                    } else {
                        fileStreams.put(sourceFile, resourceStream);
                    }

                } catch (Exception e) {
                    log.error("Getting resource stream error: file = " + sourceFile, e);
                }
            } else {
                log.warn("Resource: " + sourceFile + " is not sql file!");
            }

        }

        fileStreams.forEach((file, inputStream) -> {
            log.debug("Processing sql file: " + file);
            executeSqlScript(new InputStreamReader(inputStream), dataSource);
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Close input stream error! File = " + file, e);
            }
        });
    }

    private void executeSqlScript(Reader reader, DataSource dataSource) {
        StringWriter logWriter = new StringWriter();
        try (Reader innerReader = reader) {
            Connection connection = dataSource.getConnection();
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            StringWriter errorWriter = new StringWriter();
            scriptRunner.setLogWriter(new PrintWriter(logWriter));
            scriptRunner.setErrorLogWriter(new PrintWriter(errorWriter));
            scriptRunner.runScript(innerReader);
            if (!logWriter.toString().isEmpty()) {
                log.debug(logWriter.toString());
            }
            if (!errorWriter.toString().isEmpty()) {
                throw new RuntimeException("Executing script error: " + errorWriter);
            }

        } catch (Exception e) {
            if (!logWriter.toString().isEmpty()) {
                log.debug(logWriter.toString());
            }
            log.error("Executing init script error: ", e);
        }
    }
}
