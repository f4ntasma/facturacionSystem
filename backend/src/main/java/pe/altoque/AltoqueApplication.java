package pe.altoque;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
@ConfigurationPropertiesScan
public class AltoqueApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        SpringApplication.run(AltoqueApplication.class, args);
    }

    @PostConstruct
    void zonaHoraria() {
        // Las fechas de emisión son de Perú, corra donde corra el servidor.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
    }
}
