package dev.applytrack.backend;

import dev.applytrack.backend.config.RequiredProfileListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackendApplication.class);
        app.addListeners(new RequiredProfileListener());
        app.run(args);
    }

}
