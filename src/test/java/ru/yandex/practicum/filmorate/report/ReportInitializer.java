package ru.yandex.practicum.filmorate.report;

import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class ReportInitializer {

    @BeforeAll
    static void ensureReportsDir() throws IOException {
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        File html = new File(reportsDir, "shareIt.html");
        if (!html.exists()) {
            try (FileWriter writer = new FileWriter(html)) {
                writer.write("<html><body><h1>Report placeholder</h1></body></html>");
            }
        }
        File log = new File(reportsDir, "newman-cli.log");
        if (!log.exists()) {
            log.createNewFile();
        }
    }
}
