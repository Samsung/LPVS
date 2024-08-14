/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.lpvs.util.LPVSExitHandler;

import java.io.FileReader;

/**
 * The main class for the License Pre-Validation Service (LPVS) application.
 * This class configures and launches the LPVS Spring Boot application.
 */
@SpringBootApplication(scanBasePackages = {"com.lpvs"})
@EnableAutoConfiguration
@EnableAsync
@Slf4j
public class LicensePreValidationService {

    /**
     * The core pool size for the asynchronous task executor.
     */
    private final int corePoolSize;

    /**
     * The exit handler for handling application exits.
     */
    private static LPVSExitHandler exitHandler;

    /**
     * Constructs a new LicensePreValidationService with the specified core pool size.
     *
     * @param corePoolSize The core pool size for the asynchronous task executor.
     */
    public LicensePreValidationService(@Value("${lpvs.cores:8}") int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * The main entry point of the LPVS application.
     *
     * @param args The command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        try {
            log.info(getEmblem());
            ApplicationContext applicationContext =
                    SpringApplication.run(LicensePreValidationService.class, args);
            exitHandler = applicationContext.getBean(LPVSExitHandler.class);
        } catch (IllegalArgumentException e) {
            log.error("An IllegalArgumentException occurred: " + e.getMessage());
            try {
                exitHandler.exit(-1);
            } catch (Exception exitException) {
                log.error(
                        "An exception occurred during exit handling: "
                                + exitException.getMessage());
                System.exit(-1);
            }
        } catch (Exception e) {
            log.info("LPVS application is being closed.");
        }
    }

    /**
     * Retrieves the version of the LicensePreValidationService using the provided MavenXpp3Reader object.
     *
     * @param reader the MavenXpp3Reader object used to read the pom.xml file
     * @return the version of the LicensePreValidationService, or "latest" if an error occurs
     */
    public static String getVersion(MavenXpp3Reader reader) {
        try (FileReader fileReader = new FileReader("pom.xml")) {
            Model model = reader.read(fileReader);
            return model.getVersion();
        } catch (Exception e) {
            return "latest";
        }
    }

    /**
     * Configures and retrieves an asynchronous task executor bean.
     *
     * @return An asynchronous task executor bean.
     */
    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setThreadNamePrefix("LPVS::");
        return executor;
    }

    /**
     * Returns the emblem for the License Pre-Validation Service.
     *
     * @return the emblem as a String
     */
    protected static String getEmblem() {
        return "\n"
                + "   .----------------.   .----------------.   .----------------.   .----------------. \n"
                + "  | .--------------. | | .--------------. | | .--------------. | | .--------------. |\n"
                + "  | |   _____      | | | |   ______     | | | | ____   ____  | | | |    _______   | |\n"
                + "  | |  |_   _|     | | | |  |_   __ \\   | | | ||_  _| |_  _| | | | |   /  ___  |  | |\n"
                + "  | |    | |       | | | |    | |__) |  | | | |  \\ \\   / /   | | | |  |  (__ \\_|  | |\n"
                + "  | |    | |   _   | | | |    |  ___/   | | | |   \\ \\ / /    | | | |   '.___`-.   | |\n"
                + "  | |   _| |__/ |  | | | |   _| |_      | | | |    \\ ' /     | | | |  |`\\____) |  | |\n"
                + "  | |  |________|  | | | |  |_____|     | | | |     \\_/      | | | |  |_______.'  | |\n"
                + "  | |              | | | |              | | | |              | | | |              | |\n"
                + "  | '--------------' | | '--------------' | | '--------------' | | '--------------' |\n"
                + "   '----------------'   '----------------'   '----------------'   '----------------' \n"
                + "  :: License Pre-Validation Service ::                                    ("
                + getVersion(new MavenXpp3Reader())
                + ")\n";
    }
}
