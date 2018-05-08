package cse308.Thymeleaf;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class ThreadingConfig {
    @Bean
    @Primary
    public AsyncTaskExecutor configTaskExecutor(){
    	SimpleAsyncTaskExecutor sate = new SimpleAsyncTaskExecutor();
    	sate.setConcurrencyLimit(1);
    	return sate;
    }
}