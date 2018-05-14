package cse308.Thymeleaf.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class User {

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private	String 	email;
	private String 	uname;
	private String 	address;
	private String 	password;
	private String 	phone;
	private String token;

	public User( String uname, String email, String passwd, String phone, String address, String token) {
		this.password 	=	passwd;
		this.uname 		= 	uname;
		this.email 		= 	email;
		this.phone 		= 	phone;
		this.address 	= 	address;
		this.token 		= 	token;
	}	
	
	public User() {
		super();
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPwd() {
		return password;
	}

	public void setPwd(String pwd) {
		this.password = pwd;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String e) {
		this.email = e;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String p) {
		this.phone = p;
	}
	public String getAddress() {
		return address;
	}

	public void setAddress(String a) {
		this.address = a;
	}
	//
	@Override
	public String toString() {
		return "User [email=" + email + ", uname=" + uname + "]";
	}
}

