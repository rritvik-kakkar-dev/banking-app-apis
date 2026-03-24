package com.banking.banking_app_apis;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Banking App",
                description = "Backend Rest APIs for Banking App",
                version = "v1.0",
                contact = @Contact(
                        name = "Rritvik Kakkar",
                        email = "rritvik98kakkar@gmail.com",
                        url = "https://github.com/rritvik-kakkar-dev/banking-app-apis"
                ),
                license = @License(
                        name = "Banking App",
                        url = "https://github.com/rritvik-kakkar-dev/banking-app-apis"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Banking App Documentation",
                url = "https://github.com/rritvik-kakkar-dev/banking-app-apis"
        )
)
public class BankingAppApisApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingAppApisApplication.class, args);
	}

}
