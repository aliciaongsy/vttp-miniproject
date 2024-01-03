package sg.edu.nus.iss.miniproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import sg.edu.nus.iss.miniproject.repo.MovieRepo;

@SpringBootApplication
public class MiniProjectApplication implements CommandLineRunner {

	@Autowired
	MovieRepo movieRepo;
	public static void main(String[] args) {
		SpringApplication.run(MiniProjectApplication.class, args);
	}

	@Override
	public void run(String... args){
		movieRepo.getUserDB();
	}
}
