package io.github.vssavin.umlib.config;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by vssavin on 16.05.2022.
 */
@Component
public class SqlScriptsConfig {
    private final Logger log = LoggerFactory.getLogger(SqlScriptsConfig.class);

    @Autowired
    public SqlScriptsConfig(DataSource dataSource) {
        ArrayList<String> sqlFiles = new ArrayList<>();
        sqlFiles.add("classpath:init.sql");
        executeSqlScripts(dataSource, "", sqlFiles);
    }

    private void executeSqlScripts(DataSource dataSource, String scriptsDirectory, List<String> sourceFiles) {
        List<Path> sqlFiles = new ArrayList<>();

        if (!scriptsDirectory.isEmpty()) {
            try (Stream<Path> paths = Files.walk(Paths.get(scriptsDirectory))) {
                paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".sql"))
                        .forEach(sqlFiles::add);
            } catch (IOException e) {
                log.error("Searching sql files error: ", e);
            }
        }

        if(sqlFiles.isEmpty()) {
            for (String sourceFile : sourceFiles) {
                sqlFiles.add(Paths.get(sourceFile));
            }
        }

        sqlFiles.forEach(path -> {
            log.debug("Processing sql file: " + path);
            Reader reader = null;
            try {
                if (path.toString().startsWith("classpath")) {
                    String[] splitted = path.toString().split(":");
                    if (splitted.length > 1) {
                        ClassPathResource resource = new ClassPathResource(splitted[1]);
                        reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                    }

                } else {
                    reader = new BufferedReader(new FileReader(path.toString()));
                }
            } catch (IOException e) {
                log.error("Executing init script error: ", e);
            }
            if(reader != null) {
                executeSqlScript(reader, dataSource);
            }
        });
    }

    private void executeSqlScript(Reader reader, DataSource dataSource) {
        StringWriter logWriter = new StringWriter();
        try(Reader innerReader = reader) {
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
