package co.onlinestore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
    public static final Logger LOGGER = LoggerFactory.getLogger( Application.class);

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("starting...");

    }
    public static void main(String[]args){
        SpringApplication.run( Application.class,args);

    }
}
