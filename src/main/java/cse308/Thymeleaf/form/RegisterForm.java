package cse308.Thymeleaf.form;

public class RegisterForm {
	   	private String email;
	    private String pwd;
	    private String address;
	    private String phone;
	    private String uname;
	    
	    public String getEmail() {
	        return email;
	    }
	 
	    public void setEmail(String email) {
	        this.email= email;
	    }
	 
	    public String getPwd() {
	        return pwd;
	    }
	 
	    public void setPwd(String pwd) {
	        this.pwd = pwd;
	    }
	     
	    public String getAddress() {
	        return address;
	    }
	 
	    public void setAddress(String address) {
	        this.address= address;
	    }
	    
	    public String getUname() {
	        return uname;
	    }
	 
	    public void setUname(String uname) {
	        this.uname= uname;
	    }
	    
	    public String getPhone() {
	        return phone;
	    }
	 
	    public void setPhone(String phone) {
	        this.phone= phone;
	    }
	}
