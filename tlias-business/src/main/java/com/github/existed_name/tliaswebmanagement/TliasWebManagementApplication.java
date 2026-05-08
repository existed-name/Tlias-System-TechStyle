package com.github.existed_name.tliaswebmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan //开启对Servlet组件的支持
//@SpringBootApplication
@SpringBootApplication(scanBasePackages = "com.github.existed_name")
public class TliasWebManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(TliasWebManagementApplication.class, args);
	}

}
