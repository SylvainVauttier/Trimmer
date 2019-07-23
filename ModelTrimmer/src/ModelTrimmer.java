import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class ModelTrimmer {
	
	static final String cheatcode = "C:\\Users\\VAUTTIER\\Documents\\Recherche\\Broadleaf\\broadleaf1.6.0";
	
	static final String cheat2 = "uml2.txt";
	
	static final String cheat3 = "trimmed.puml";
	
	String workingdirectory;
	
	String pumlInput;
	
	String pumlOuput;
	
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	Set<String> beanclasses = new HashSet<String>();
	Set<String> supertypes = new HashSet<String>();

	List<String> filteredRelations = new ArrayList<String>();
	
	public static void main(String[] args) {
		
		
		// TODO Auto-generated method stub
		ModelTrimmer mt = new ModelTrimmer();
		mt.setWorkingDirectory(args);
		mt.extractBeanList();
		mt.extractClassHierarchy(args);
		mt.genTrimmedPuml(args);
	}

	private void genTrimmedPuml(String[] args) {
		// TODO Auto-generated method stub
		setOutputPumlFiles(args);
		
		BufferedWriter output=null;
		
		try {
			output = new BufferedWriter(new FileWriter(new File (pumlOuput)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		try {
			output.write("@startuml\n\n");
			
			for(String b : beanclasses)
				//output.write("class "+b+" { }\n\n");
				output.write("class "+b+"\n\n");

			
			for(String t : supertypes)
				//output.write("class "+t+" { }\n\n");
				output.write("class "+t+"\n\n");
			
			for(String r : filteredRelations)
				output.write(r+"\n");
			
			output.write("@enduml");
			
			output.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}

	private void setOutputPumlFiles(String[] args) {
		// TODO Auto-generated method stub
		pumlOuput=workingdirectory+"\\";
		if (args==null)
			pumlOuput+=cheat3;
		else
			if (args.length<2)
				pumlOuput+=cheat3;
			else 
				pumlOuput+=args[2];
	}

	private void extractClassHierarchy(String[] args){
		// TODO Auto-generated method stub
		
		setInputPumlFiles(args);
		
		BufferedReader input=null;
		
		try {
			input = new BufferedReader(new FileReader(new File (pumlInput)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		// on extrait toutes les relations du fichier puml
		List<String> relations = new ArrayList<String>();
		
		boolean done=false;
		
		String line=null;
		
		// on laisse de côté les définitions des classes et interfaces
		while (!done)
		{
			try {
				line = input.readLine();
				if (line==null) throw new IOException();
				if (line.contains("<|--")||line.contains("<|..")||line.contains("-->"))
					done=true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//done=true;
				System.exit(0);
			}
		}
		
		//puis on parcourt toutes les définitions de relations
		while(line!=null)
		{
			relations.add(line);
			try {
				line=input.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		// on enlève la ligne de fin de fichier puml
		relations.remove(relations.size()-1);
		
		System.out.println(relations.size());
		
		List<String> remainingRelations = new ArrayList<String>();
		
		// initialement les types recherchés sont les classes de beans
		Set<String> filteredClasses = beanclasses;
		
		//tant que l'on a des types qui n'ont pas été recherchées comme source d'une relation
		while (filteredClasses.size()>0)
		{
			// types à rechercher lors de la prochaine itération
			Set<String> unfilteredClasses = new HashSet<String>();
			
			for (String r : relations)
			{
				String[] relements = r.split(" ");
				List<String> relClassElements = new ArrayList<String>();
				
				for (String e : relements)
					if (e.contains(".")&&!e.equals("<|..")) relClassElements.add(e);
				
				String target = relClassElements.get(0);
				String source = relClassElements.get(1);
				
				// si la relation est une relation d'implémentation ou de spécialisation
				if (r.contains("<|--")||r.contains("<|.."))
					// si l'une des classes recherchées est à la source
					if (filteredClasses.contains(source))
							{
							// on sélectionne la relation
							filteredRelations.add(r);
							// si la classe cible n'est pas déjà une classe de beans
							// ou une classe déjà atteinte comme un supertype
							// c'est une classe à rechercher lors de la prochaine itération
							if (!beanclasses.contains(target)&&!supertypes.contains(target))
							unfilteredClasses.add(target);
							}
					// si la relation ne nous intéresse pas immédiatement, elle doit être conservée
					// pour les prochains filtrages
					else remainingRelations.add(r);
				else {
					// il s'agit d'une relation d'association
					// si elle relie des classes de beans ou leurs supertypes
					// on sélectionne la relation
					if ((beanclasses.contains(source)||supertypes.contains(source))
					    &&
						(beanclasses.contains(target)||supertypes.contains(target)))
						filteredRelations.add(r);
					else remainingRelations.add(r);
				}
					
			}
			supertypes.addAll(unfilteredClasses);
			filteredClasses = unfilteredClasses;
			relations=remainingRelations;
			remainingRelations= new ArrayList<String>();
		}
		
		System.out.println(beanclasses.size());
		System.out.println(supertypes.size());
		System.out.println(filteredRelations.size());
		
	}

	private void setInputPumlFiles(String[] args) {
		// TODO Auto-generated method stub
		pumlInput=workingdirectory+"\\";
		if (args==null)
			pumlInput+=cheat2;
		else
			if (args.length<1)
				pumlInput+=cheat2;
			else 
				pumlInput+=args[1];
	}

	private void extractBeanList() {
		// TODO Auto-generated method stub
		File sdsldir = new File(workingdirectory+"\\sdsl");
		
		File[] sdslfiles = sdsldir.listFiles();
		
		for (File f : sdslfiles)
		{
			extractBeans(f);
		}
	}

	private void extractBeans(File f) {
		// TODO Auto-generated method stub
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document document= builder.parse(f);
			
			Element root = document.getDocumentElement();
			//System.out.println(root.getNodeName());
			
			NodeList beanList = root.getElementsByTagName("bean");
			//System.out.println("bean tags :"+beanList.getLength());
			for (int i=0;i<beanList.getLength();i++)
			{
				Element beanElement = ((Element)beanList.item(i));
				beanclasses.add(beanElement.getAttribute("class"));
				//System.out.println(((Element)beanList.item(i)).getAttribute("class"));
//				if (!beanElement.getParentNode().getNodeName().equals("beans"))
//					System.out.println("Component !");
			}
			
			//System.out.println(f.getName());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setWorkingDirectory(String[] args) {
		// TODO Auto-generated method stub
		if (args!=null&&args.length>0) workingdirectory = args[0];
		else workingdirectory = cheatcode;
	}

}
