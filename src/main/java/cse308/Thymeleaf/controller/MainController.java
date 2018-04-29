package cse308.Thymeleaf.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import cse308.Thymeleaf.form.LoginForm;

import cse308.Thymeleaf.form.RegisterForm;
import cse308.Thymeleaf.model.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

	private static String login = "no";
	private static User user = null;

	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String index(Model model) {
		model.addAttribute("login", login);
		return "index";
	}

	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	public String showLoginPage(Model model) {

		LoginForm loginForm = new LoginForm();
		model.addAttribute("loginForm", loginForm);

		return "login";
	}

	@RequestMapping(value = { "/login" }, method = RequestMethod.POST)
	public String login(Model model, //
			@ModelAttribute("loginForm") LoginForm loginForm) {

		String email = loginForm.getEmail();
		String pwd = loginForm.getPwd();

		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		user = entitymanager.find(User.class, email);
		if (user == null || !user.getPwd().equals((Encrypt.encrypt(pwd)))) {
			entitymanager.close();
			emfactory.close();
			return "loginFail";
		}
		entitymanager.close();
		emfactory.close();
		login = "yes";
		model.addAttribute("user", user);
		return "UserCenter";
	}

	@RequestMapping(value = { "/about" }, method = RequestMethod.GET)
	public String showAboutPage(Model model) {
		model.addAttribute("user", user);
		model.addAttribute("login", login);
		return "about";
	}

	@RequestMapping(value = { "/logout" }, method = RequestMethod.GET)
	public String logout(Model model) {
		login = "no";
		user = null;
		model.addAttribute("login", login);
		return "index";
	}

	@RequestMapping(value = { "/profile" }, method = RequestMethod.GET)
	public String showProfilePage(Model model) {
		model.addAttribute("user", user);
		return "UserCenter";
	}

	@RequestMapping(value = { "/register" }, method = RequestMethod.GET)
	public String showRegisterPage(Model model) {

		RegisterForm registerForm = new RegisterForm();
		model.addAttribute("registerForm", registerForm);

		return "register";
	}

	@RequestMapping(value = { "/register" }, method = RequestMethod.POST)
	public String register(Model model, //
			@ModelAttribute("registerForm") RegisterForm registerForm) {

		String email = registerForm.getEmail();
		String pwd = registerForm.getPwd();
		String uname = registerForm.getUname();
		String phone = registerForm.getPhone();
		String address = registerForm.getAddress();

		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		user = new User();

		user.setUname(uname);
		user.setEmail(email);
		System.out.println(pwd);
		System.out.println(email);
		System.out.println(uname);
		System.out.println(address);
		user.setPwd(Encrypt.encrypt(pwd));
		user.setPhone(phone);
		user.setAddress(address);

		entitymanager.getTransaction().begin();
		entitymanager.persist(user);
		entitymanager.getTransaction().commit();

		entitymanager.close();
		emfactory.close();
		model.addAttribute("user", user);
		return "UserCenter";
	}

}
