package com.example;

import com.example.model.Account;
import com.example.service.AccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@SpringBootApplication
public class SpringbootJdbc01Application {

	public static void main(String[] args) {

		ConfigurableApplicationContext ctxt = SpringApplication.run(SpringbootJdbc01Application.class, args);

		AccountService service = ctxt.getBean(AccountService.class);

		Account a1 = new Account(0, "John", "john@mybank.com");
		Account a2 = new Account(1, "James", "james@mybank.com");

		System.out.println("Before Saving : ");
		System.out.println(a1);
		System.out.println(a2);


		Account savedA1 = service.createAccount(a1);
		Account savedA2 = service.createAccount(a2);

		System.out.println("After saving : ");
		System.out.println(savedA1);
		System.out.println(savedA2);

		List<Account> accounts = service.getAllAccounts();
		System.out.println("All accounts from db : ");
		for(Account a : accounts)
		{
			System.out.println(a);
		}

		ctxt.close();

	}

}
