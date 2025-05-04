package andrehsvictor.camly;

import org.springframework.boot.SpringApplication;

public class TestCamlyApplication {

	public static void main(String[] args) {
		SpringApplication.from(CamlyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
