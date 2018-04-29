package cse308.Thymeleaf.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Feedback {
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private String title;
	private String email;
	private String detail;

	public Feedback(String title, String email, String detail) {
		this.title	= title;
		this.email 	= email;
		this.detail = detail;
	}
	public Feedback() {
		super();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String e) {
		this.email = e;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String t) {
		this.title = t;
	}
	public String getDetail() {
		return detail;
	}

	public void setDetail(String d) {
		this.detail = d;
	}
	//
	@Override
	public String toString() {
		return "User [email=" + email + ", title=" + title + ", detail=" + detail + "]";
	}
}
