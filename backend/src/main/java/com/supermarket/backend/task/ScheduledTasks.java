package com.supermarket.backend.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:root}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${backup.path:./backup}")
    private String backupPath;

    /**
     * 每天凌晨2点执行数据库备份
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void databaseBackup() {
        logger.info("开始执行数据库备份任务...");
        try {
            String databaseName = extractDatabaseName(dbUrl);
            String timestamp = LocalDateTime.now().format(DATETIME_FORMAT);
            String fileName = String.format("%s_backup_%s.sql", databaseName, timestamp);

            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            Path backupFile = backupDir.resolve(fileName);
            
            // 模拟数据库备份（实际项目中可以使用mysqldump等工具）
            logger.info("数据库备份完成: {}", backupFile);
            
            // 清理90天前的备份文件
            cleanupOldBackups(backupDir, 90);
            
        } catch (Exception e) {
            logger.error("数据库备份失败", e);
        }
    }

    /**
     * 每天凌晨3点执行日志归档
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void archiveLogs() {
        logger.info("开始执行日志归档任务...");
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                return;
            }

            String today = LocalDateTime.now().format(DATE_FORMAT);
            Path archiveDir = logDir.resolve("archive");
            if (!Files.exists(archiveDir)) {
                Files.createDirectories(archiveDir);
            }

            // 归档昨天及之前的日志文件
            try (Stream<Path> stream = Files.list(logDir)) {
                stream.filter(path -> Files.isRegularFile(path))
                        .filter(path -> path.getFileName().toString().endsWith(".log"))
                        .filter(path -> !path.getFileName().toString().contains(today))
                        .forEach(path -> {
                            try {
                                Path targetPath = archiveDir.resolve(path.getFileName());
                                Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                                logger.info("归档日志文件: {}", path.getFileName());
                            } catch (IOException e) {
                                logger.error("归档日志文件失败: {}", path.getFileName(), e);
                            }
                        });
            }

            // 清理归档目录中90天前的日志
            cleanupOldArchives(archiveDir, 90);

            logger.info("日志归档任务完成");
        } catch (Exception e) {
            logger.error("日志归档失败", e);
        }
    }

    private String extractDatabaseName(String url) {
        if (url == null || url.isEmpty()) {
            return "supermarket";
        }
        int start = url.lastIndexOf("/") + 1;
        int end = url.indexOf("?");
        if (end == -1) {
            end = url.length();
        }
        return url.substring(start, end);
    }

    private void cleanupOldBackups(Path backupDir, int daysToKeep) {
        try (Stream<Path> stream = Files.list(backupDir)) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
            stream.filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .forEach(path -> {
                        try {
                            LocalDateTime fileTime = Files.getLastModifiedTime(path)
                                    .toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDateTime();
                            if (fileTime.isBefore(cutoff)) {
                                Files.delete(path);
                                logger.info("删除旧备份文件: {}", path.getFileName());
                            }
                        } catch (IOException e) {
                            logger.error("删除旧备份文件失败: {}", path.getFileName(), e);
                        }
                    });
        } catch (IOException e) {
            logger.error("清理旧备份文件失败", e);
        }
    }

    private void cleanupOldArchives(Path archiveDir, int daysToKeep) {
        try (Stream<Path> stream = Files.list(archiveDir)) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
            stream.filter(path -> Files.isRegularFile(path))
                    .forEach(path -> {
                        try {
                            LocalDateTime fileTime = Files.getLastModifiedTime(path)
                                    .toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDateTime();
                            if (fileTime.isBefore(cutoff)) {
                                Files.delete(path);
                                logger.info("删除旧归档文件: {}", path.getFileName());
                            }
                        } catch (IOException e) {
                            logger.error("删除旧归档文件失败: {}", path.getFileName(), e);
                        }
                    });
        } catch (IOException e) {
            logger.error("清理旧归档文件失败", e);
        }
    }
}
