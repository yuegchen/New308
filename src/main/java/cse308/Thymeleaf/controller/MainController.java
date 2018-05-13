package cse308.Thymeleaf.controller;

import java.io.*;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import cse308.Thymeleaf.form.LoginForm;
import cse308.Thymeleaf.form.PropertyForm;
import cse308.Thymeleaf.form.RedistrictingForm;
import cse308.Thymeleaf.form.RegisterForm;
import cse308.Thymeleaf.form.SelectUserForm;
import cse308.Thymeleaf.model.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainController {

	private static String login = "no";
	private static User user = null;
	private static Admin admin=null;
	private static List<User> userList=null;
	private static List<Admin> adminList=null;

	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String index(Model model) {
		RedistrictingForm redistrictingForm = new RedistrictingForm();
		model.addAttribute("redistrictingForm", redistrictingForm);
		model.addAttribute("login", login);
		return "index";
	}

	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	public String showLoginPage(Model model) {

		LoginForm loginForm = new LoginForm();
		model.addAttribute("loginForm", loginForm);

		return "login";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/login" }, method = RequestMethod.POST)
	public String login(Model model, 
			@ModelAttribute("loginForm") LoginForm loginForm) {

		String email = loginForm.getEmail();
		String pwd = loginForm.getPwd();
		user=null;
		admin=null;
		
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		admin = entitymanager.find(Admin.class, email);
		if (admin == null || !admin.getPwd().equals((Encrypt.encrypt(pwd)))) {
			admin=null;
			user = entitymanager.find(User.class, email);
			if (user == null || !user.getPwd().equals((Encrypt.encrypt(pwd)))) {
				user=null;
				entitymanager.close();
				emfactory.close();
				return "loginFail";
			}
		}
		
		
		if(admin==null){
			login = "yes";
			model.addAttribute("user", user);
			entitymanager.close();
			emfactory.close();
			return "UserCenter";
		}
		else{
			login = "no";
			userList =(List<User>) entitymanager.createQuery(
					"SELECT u FROM User u").getResultList();
			adminList =(List<Admin>) entitymanager.createQuery(
					"SELECT a FROM Admin a").getResultList();

			model.addAttribute("adminList", adminList);
			SelectUserForm selectUserForm = new SelectUserForm();
			model.addAttribute("selectUserForm", selectUserForm);
			PropertyForm propertyForm = new PropertyForm();
			model.addAttribute("propertyForm", propertyForm);
			model.addAttribute("userList", userList);
			model.addAttribute("admin", admin);
			entitymanager.close();
			emfactory.close();
			return "AdminCenter";
		}
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
		admin =null;
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
	public String register(Model model, 
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
		user.setPwd(Encrypt.encrypt(pwd));
		user.setPhone(phone);
		user.setAddress(address);

		entitymanager.getTransaction().begin();
		entitymanager.persist(user);
		entitymanager.getTransaction().commit();

		entitymanager.close();
		emfactory.close();
		login="yes";
		model.addAttribute("user", user);
		return "UserCenter";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/deleteUser" }, method = RequestMethod.POST)
	public String deleteUser(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
		User u = entitymanager.find(User.class, email);

		entitymanager.getTransaction().begin();
		entitymanager.remove(u);
		entitymanager.getTransaction().commit();
		  
		userList =(List<User>) entitymanager.createQuery(
				"SELECT u FROM User u").getResultList();
		adminList =(List<Admin>) entitymanager.createQuery(
				"SELECT a FROM Admin a").getResultList();
		PropertyForm propertyForm = new PropertyForm();
		model.addAttribute("propertyForm", propertyForm);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);
		model.addAttribute("adminList", adminList);
		model.addAttribute("userList", userList);
		model.addAttribute("admin", admin);
		entitymanager.close();
		emfactory.close();
		return "adminCenter";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/deleteAdmin" }, method = RequestMethod.POST)
	public String deleteAdmin(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
		Admin u = entitymanager.find(Admin.class, email);

		entitymanager.getTransaction().begin();
		entitymanager.remove(u);
		entitymanager.getTransaction().commit();
		  
		userList =(List<User>) entitymanager.createQuery(
				"SELECT u FROM User u").getResultList();
		adminList =(List<Admin>) entitymanager.createQuery(
				"SELECT a FROM Admin a").getResultList();
		PropertyForm propertyForm = new PropertyForm();
		model.addAttribute("propertyForm", propertyForm);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);
		model.addAttribute("adminList", adminList);
		model.addAttribute("userList", userList);
		model.addAttribute("admin", admin);
		entitymanager.close();
		emfactory.close();
		return "adminCenter";
	}
	
	@RequestMapping(value = { "/editUser" }, method = RequestMethod.POST)
	public String editUser(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
		User selectedUser = entitymanager.find(User.class, email);

		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);

		entitymanager.close();
		emfactory.close();
		return "Edit";
	}
	@RequestMapping(value = { "/editAdmin" }, method = RequestMethod.POST)
	public String editAdmin(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
		Admin selectedUser = entitymanager.find(Admin.class, email);

		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);

		entitymanager.close();
		emfactory.close();
		return "EditAdmin";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/updateUser" }, method = RequestMethod.POST)
	public String updateUser(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
		
		entitymanager.getTransaction().begin(); 
		User selectedUser = entitymanager.find(User.class, email);
		selectedUser.setPhone(selectUserForm.getPhone()); 
		selectedUser.setAddress(selectUserForm.getAddress());
		selectedUser.setUname(selectUserForm.getUname());
//		selectedUser.setPwd(Encrypt.encrypt(selectUserForm.getPwd()));
		entitymanager.getTransaction().commit();
		userList =(List<User>) entitymanager.createQuery(
				"SELECT u FROM User u").getResultList();
		adminList =(List<Admin>) entitymanager.createQuery(
				"SELECT a FROM Admin a").getResultList();

		model.addAttribute("adminList", adminList);
		model.addAttribute("userList", userList);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);
		PropertyForm propertyForm = new PropertyForm();
		model.addAttribute("propertyForm", propertyForm);
		entitymanager.close();
		emfactory.close();
		return "admincenter";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/updateAdmin" }, method = RequestMethod.POST)
	public String updateAdmin(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
		
		entitymanager.getTransaction().begin(); 
		Admin selectedUser = entitymanager.find(Admin.class, email);
		selectedUser.setPhone(selectUserForm.getPhone()); 
		selectedUser.setAddress(selectUserForm.getAddress());
		selectedUser.setUname(selectUserForm.getUname());
//		selectedUser.setPwd(Encrypt.encrypt(selectUserForm.getPwd()));
		entitymanager.getTransaction().commit();
		userList =(List<User>) entitymanager.createQuery(
				"SELECT u FROM User u").getResultList();
		adminList =(List<Admin>) entitymanager.createQuery(
				"SELECT a FROM Admin a").getResultList();

		model.addAttribute("adminList", adminList);
		model.addAttribute("userList", userList);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);
		PropertyForm propertyForm = new PropertyForm();
		model.addAttribute("propertyForm", propertyForm);
		entitymanager.close();
		emfactory.close();
		return "admincenter";
	}
	
	@RequestMapping(value = { "/addUser" }, method = RequestMethod.GET)
	public String showAddUser(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
//		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
//		User selectedUser = entitymanager.find(User.class, email);

//		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);

		entitymanager.close();
		emfactory.close();
		return "AddUser";
	}
	
	@RequestMapping(value = { "/addAdmin" }, method = RequestMethod.GET)
	public String showAddAdmin(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
//		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
//		User selectedUser = entitymanager.find(User.class, email);

//		model.addAttribute("selectedUser", selectedUser);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);

		entitymanager.close();
		emfactory.close();
		return "AddAdmin";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/addUser" }, method = RequestMethod.POST)
	public String addUser(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
//		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
//		User selectedUser = entitymanager.find(User.class, email);

//		model.addAttribute("selectedUser", selectedUser);
		
		String email = selectUserForm.getEmail();
		String pwd = selectUserForm.getPwd();
		String uname = selectUserForm.getUname();
		String phone = selectUserForm.getPhone();
		String address = selectUserForm.getAddress();

		User user = new User();

		user.setUname(uname);
		user.setEmail(email);
		user.setPwd(Encrypt.encrypt(pwd));
		user.setPhone(phone);
		user.setAddress(address);

		entitymanager.getTransaction().begin();
		entitymanager.persist(user);
		entitymanager.getTransaction().commit();
		
		userList =(List<User>) entitymanager.createQuery(
				"SELECT u FROM User u").getResultList();
		adminList =(List<Admin>) entitymanager.createQuery(
				"SELECT a FROM Admin a").getResultList();

		model.addAttribute("adminList", adminList);
		model.addAttribute("userList", userList);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);
		PropertyForm propertyForm = new PropertyForm();
		model.addAttribute("propertyForm", propertyForm);
		entitymanager.close();
		emfactory.close();
		return "admincenter";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/addAdmin" }, method = RequestMethod.POST)
	public String addAdmin(Model model,
			@ModelAttribute("selectUserForm") SelectUserForm selectUserForm) {
//		String email = selectUserForm.getEmail();
//		String adminE = selectUserForm.getCurrentAdmin();
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("Eclipselink_JPA");
		EntityManager entitymanager = emfactory.createEntityManager();
		
//		admin = entitymanager.find(Admin.class, adminE);
//		User selectedUser = entitymanager.find(User.class, email);

//		model.addAttribute("selectedUser", selectedUser);
		
		String email = selectUserForm.getEmail();
		String pwd = selectUserForm.getPwd();
		String uname = selectUserForm.getUname();
		String phone = selectUserForm.getPhone();
		String address = selectUserForm.getAddress();

		Admin user = new Admin();

		user.setUname(uname);
		user.setEmail(email);
		user.setPwd(Encrypt.encrypt(pwd));
		user.setPhone(phone);
		user.setAddress(address);

		entitymanager.getTransaction().begin();
		entitymanager.persist(user);
		entitymanager.getTransaction().commit();
		
		userList =(List<User>) entitymanager.createQuery(
				"SELECT u FROM User u").getResultList();
		adminList =(List<Admin>) entitymanager.createQuery(
				"SELECT a FROM Admin a").getResultList();

		model.addAttribute("adminList", adminList);
		model.addAttribute("userList", userList);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);
		PropertyForm propertyForm = new PropertyForm();
		model.addAttribute("propertyForm", propertyForm);
		entitymanager.close();
		emfactory.close();
		return "admincenter";
	}
	@RequestMapping(value = { "/editProperty" }, method = RequestMethod.POST)
	public String editProperty(Model model,
			@ModelAttribute("propertyForm") PropertyForm propertyForm) {
		int max1=propertyForm.getMax1();
		int max2=propertyForm.getMax2();
		String str = "MAX_MOVES="+max1+"\r\n"+
"MAX_NON_IMPROVED_STEPS="+max2;
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("C:\\eclipse\\Projects\\New308\\src\\main\\resources\\static\\externalProperty\\Property.txt"));
			writer.write(str);
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		model.addAttribute("adminList", adminList);
		model.addAttribute("userList", userList);
		model.addAttribute("admin", admin);
		SelectUserForm selectUserForm2 = new SelectUserForm();
		model.addAttribute("selectUserForm", selectUserForm2);
		PropertyForm propertyForm2 = new PropertyForm();
		model.addAttribute("propertyForm", propertyForm2);
		return "admincenter";
	}
	
}
