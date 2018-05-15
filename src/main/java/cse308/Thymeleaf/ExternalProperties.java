package cse308.Thymeleaf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ExternalProperties {
	private	String path = System.getProperty("user.dir") + "/src/main/resources/static/externalProperty/Property.txt";
	private int		maxMoves;
	private int		maxNonImprovedSteps;
	public ExternalProperties(){
		File file = new File(path);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String st;
			int i=0;
			while ((st = br.readLine()) != null){
				System.out.println(st);
				int index=st.indexOf('=');
				if(i==0){
					maxMoves=Integer.parseInt(st.substring(index+1, st.length()));
				}
				else{
					maxNonImprovedSteps=Integer.parseInt(st.substring(index+1, st.length()));
				}
				i++;
			}
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getMaxMoves(){
		return maxMoves;
	}
	
	public int getNonImprovedSteps(){
		return maxNonImprovedSteps;
	}
	
}
