package org.clusterer.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Spring boot runner
 */
@SpringBootApplication
@Configuration
@ComponentScan("org.clusterer.services")
public class Runner
{
	public static void main(final String[] args)
	{
		SpringApplication.run(Runner.class, args);
	}
}
