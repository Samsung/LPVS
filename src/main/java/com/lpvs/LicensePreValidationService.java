/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.lpvs.util.LPVSExitHandler;

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
    private static int corePoolSize = 8;

    /**
     * The exit handler for handling application exits.
     */
    private static LPVSExitHandler exitHandler;

    /**
     * Creates a new instance of {@link SpringApplication} configured to run the {@link LicensePreValidationService} class.
     *
     * @return a new instance of {@link SpringApplication}
     */
    protected SpringApplication createSpringApplication() {
        return new SpringApplication(LicensePreValidationService.class);
    }

    /**
     * The main entry point of the LPVS application.
     *
     * @param args The command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        LicensePreValidationService lpvs = new LicensePreValidationService();
        lpvs.run(args);
    }

    /**
     * Runs the {@link LicensePreValidationService} application with the specified command-line arguments.
     *
     * @param args the command-line arguments to pass to the application
     */
    public void run(String[] args) {
        SpringApplication app = createSpringApplication();
        app.addInitializers(
                applicationContext -> {
                    ConfigurableEnvironment environment = applicationContext.getEnvironment();
                    String version = environment.getProperty("lpvs.version", "Unknown");
                    log.info(getEmblem(version));
                    corePoolSize = Integer.parseInt(environment.getProperty("lpvs.cores", "8"));
                });
        try {
            ApplicationContext applicationContext = app.run(args);
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
    protected static String getEmblem(String version) {
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
                + "  :: License Pre-Validation Service ::                                    (v "
                + version
                + ")\n";
    }
}
