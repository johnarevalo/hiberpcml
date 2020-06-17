/*
 * The MIT License
 *
 * Copyright 2011 John Arevalo <johnarevalo@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.googlecode.hiberpcml;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400ConnectionPool;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.data.ProgramCallDocument;

/**
 *
 * @author John Arevalo <johnarevalo@gmail.com>
 * @author Vincent Villain <flake9025@gmail.com>
 */
public class SessionManager {

	/**
	 * Pool de connexions a l'AS400
	 */
	private AS400ConnectionPool pool = null;
	
	/**
	 * IP / Hostname de l'AS400
	 */
	private String server = null;
	
	/**
	 * Nom d'utilisateur pour la connexion AS400
	 */
	private String user = null;
	
	/**
	 * Mot de passe pour la connexion AS400
	 */
	private String password = null;
	
	/**
	 * Document PCML
	 */
    private ProgramCallDocument pcmlDoc;

	/**
	 * Constructeur : Connexion a un ISeries
	 * @param serveur
	 * @param utilisateur
	 * @param mot de passe
	 */
    public SessionManager(String server, String user, String password) {
		this.pool = new AS400ConnectionPool();
		this.server = server;
		this.user = user;
		this.password = password;
		this.pcmlDoc = null;
    }

	/**
	 * Appel de commande
	 * @param programme
	 */
	public void invokeCommand(String cmdString) throws Exception{
		
		AS400 as400 = null;
		
		try {
			// Connexion a l'AS400
			as400 = pool.getConnection(server, user, password);
			// Ouverture d'une commande
			CommandCall command = new CommandCall(as400);
			// Execution de la commande
			command.run(cmdString);
			
		} catch (Exception e) {
			throw e;
		} finally {
			// Deonnexion de l'AS400
			if(as400 != null){
				pool.returnConnectionToPool(as400);
			}
		}
	}
	
	/**
	 * Appel de programme
	 * @param pcml : programme
	 */
    public void invokeProgram(Object pcml) throws PcmlException {
    	
        if (!pcml.getClass().isAnnotationPresent(Program.class)) {
            throw new PcmlException("class: " + pcml.getClass() + " is not a "
                    + "@com.googlecode.hiberpcml.Program annotated class");
        }
        Program program = pcml.getClass().getAnnotation(Program.class);

        AS400 as400 = null;
        
        try {
    		// Connexion a l'AS400
        	as400 = pool.getConnection(server, user, password);
        	
        	// Analyse du fichier PCML
            pcmlDoc = new ProgramCallDocument(as400, program.documentName());
            
            // Passage des parametres au programme (INPUT)
            Field[] fields = pcml.getClass().getDeclaredFields();
            for (Field field : fields) {
                setValue(field, pcml, program.programName(), new int[0]);
            }

            // Appel du programme
            pcmlDoc.callProgram(program.programName());

            // Recuperation des messages (avertissements, erreurs)
            AS400Message[] messageList = pcmlDoc.getProgramCall().getMessageList();
            if (messageList != null && messageList.length > 0) {
                StringBuilder buffer = new StringBuilder();
                for (AS400Message message : messageList) {
                    buffer.append(message.getText()).append(System.getProperty("line.separator"));
                }
                throw new PcmlException(buffer.toString());
            }
            
            // Recuperation des valeurs de retour (OUTPUT)
            for (Field field : fields) {
                getValue(field, pcml, program.programName(), new int[0]);
            }
        } catch (Exception ex) {
            throw new PcmlException(ex);
        } finally {
        	// Deconnexion de l'AS400
			if(as400 != null){
				pool.returnConnectionToPool(as400);
			}
		}
    }

	/**
	 * Affecte une valeur a un champ avant un appel de programme
	 * @param field : champ 
	 * @param parent : objet contenant le champ
	 * @param path : path du parent
	 * @param tabIndices : indices dans le cas d'un tableau
	 * @throws Exception
	 */
    @SuppressWarnings({ "rawtypes" })
    private void setValue(Field field, Object parent, String path, int tabIndices[]) throws Exception {
    	
		/*-------------------------------------------------------*/
		/* 1. Champs avec annotions "@Data"	                     */
		/*-------------------------------------------------------*/
        if (field.isAnnotationPresent(Data.class)) {
            Data pcmlData = field.getAnnotation(Data.class);
            
            // Le type du champ doit etre INPUT ou INPUTOUTPUT, on ne doit pas setter les OUTPUT
            if (!pcmlData.usage().equals(UsageType.INPUT) && !pcmlData.usage().equals(UsageType.INPUTOUTPUT)) {
                return;
            }
            
            // On recupere la valeur du champ depuis le parent
            Object value = getField(field, parent);
            
            // On calcule le path absolu du champ dans le PCML
            path = path + "." + pcmlData.pcmlName();

    		/*-------------------------------------------------------*/
    		/* 1.1 Champs dont le type est "@Struct"                 */
    		/*-------------------------------------------------------*/
            if (field.getType().isAnnotationPresent(Struct.class)) {
            	
            	// On ne peut pas setter une Structure : on parcoure et on set les champs qu'elle contient
                for (Field structField : field.getType().getDeclaredFields()) {
                    setValue(structField, value, path, tabIndices);
                }
                
        	/*-------------------------------------------------------*/
        	/* 1.2 Champs standards									 */
        	/*-------------------------------------------------------*/
            } else {
            	// Pour les chaines, on les remplit avec la directive completeWith de l'annotation data
                if (value instanceof String) {
                    value = Util.completeWith((String) value, pcmlData.completeWith(), pcmlData.length());
                }
                if(tabIndices.length > 0){
                	// Set value dans un tableau : on passe les indices
                    pcmlDoc.setValue(path, tabIndices, value);
                }else{
                	// Set value dans une zone simple
                    pcmlDoc.setValue(path, value);
                }
            }

        /*-------------------------------------------------------*/
    	/* 2. Champs avec annotation "@Array"					 */
        /*-------------------------------------------------------*/
        } else if (field.isAnnotationPresent(Array.class)) {
            Array pcmlArray = field.getAnnotation(Array.class);
            
            // Le type du champ doit etre INPUT ou INPUTOUTPUT, on ne doit pas setter les OUTPUT
            if (!pcmlArray.usage().equals(UsageType.INPUT) && !pcmlArray.usage().equals(UsageType.INPUTOUTPUT)) {
                return;
            }
            
            // On recupere le tableau depuis le parent
            List arrayValue = (List) getField(field, parent);
            // On calcule le path absolu du champ dans le PCML
            path = path + "." + pcmlArray.pcmlName();
            
        	// Creation des indices
        	int indices[];
        	if(tabIndices.length > 0){
        		// Tableau dans un tableau
        		indices = new int[2];
        	}else{
        		// Tableau simple
        		indices = new int[1];
        	}
        	
            /*-------------------------------------------------------*/
    		/* 2.1 Le tableau contient des objets "@Struct"          */
            /*-------------------------------------------------------*/
            if (pcmlArray.type().isAnnotationPresent(Struct.class)) {
            	
            	// On ne peut pas setter une Structure : on parcoure et on set les champs qu'elle contient
                for (Field structField : pcmlArray.type().getDeclaredFields()) {
                	// Parcours du tableau
                    for (int i = 0; i < pcmlArray.size(); i++) {
                    	
                    	// Positionnement des indices
                        if(tabIndices.length > 0){
                        	// Tableau dans un tableau
                    		indices[0] = tabIndices[0];
                    		indices[1] = i;
                        }else{
                        	// Tableau simple
                        	indices[0] = i;
                        }
                        
                        // Set value dans un tableau : on passe les indices
                        setValue(structField, arrayValue.get(i), path, indices);
                    }
                }
                
            /*-------------------------------------------------------*/
    		/* 2.2 Le tableau contient des champs standards		     */
            /*-------------------------------------------------------*/
            } else {
            	
            	// Parcours du tableau
                for (int i = 0; i < pcmlArray.size(); i++) {
                	if(tabIndices.length > 0){
                		// Tableau dans un tableau
                		indices[0] = tabIndices[0];
                		indices[1] = i;
                	}else{
                		// Tableau simple
                		indices[0] = i;
                	}
                	
                    // Set value dans un tableau : on passe les indices
                    pcmlDoc.setValue(path, indices, arrayValue.get(i));
                }
            }
        }
    }

    /**
     * Recupere la valeur d'un champ apres un appel de programme
     * @param field
     * @param parent
     * @param path
     * @param tabIndices : indices dans le cas d'un tableau
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void getValue(Field field, Object parent, String path, int tabIndices[]) throws Exception {
    	
		/*-------------------------------------------------------*/
		/* 1. Champs avec annotions "@Data"	                     */
		/*-------------------------------------------------------*/
        if (field.isAnnotationPresent(Data.class)) {
            Data pcmlData = field.getAnnotation(Data.class);
            
            // Le type du champ doit etre OUTPUT ou INPUTOUTPUT, on ne doit pas getter les INPUT
            if (!pcmlData.usage().equals(UsageType.OUTPUT) && !pcmlData.usage().equals(UsageType.INPUTOUTPUT)) {
                return;
            }
            
    		/*-------------------------------------------------------*/
    		/* 1.1 Champs dont le type est "@Struct"                 */
    		/*-------------------------------------------------------*/
            if (field.getType().isAnnotationPresent(Struct.class)) {
                Object struct = getField(field, parent);

                // On ne peut pas getter une Structure : on parcoure et on get les champs qu'elle contient
                for (Field structField : field.getType().getDeclaredFields()) {
                    getValue(structField, struct, path + "." + pcmlData.pcmlName(), tabIndices);
                }
            
        	/*-------------------------------------------------------*/
        	/* 1.2 Champs standards									 */
        	/*-------------------------------------------------------*/
            } else {
                Object value = null;
                if(tabIndices.length > 0){
                	// Get value dans un tableau : on passe les indices
                	value = pcmlDoc.getValue(path + "." + pcmlData.pcmlName(), tabIndices);
                }else{
                	// Get value dans une zone simple
                	value = pcmlDoc.getValue(path + "." + pcmlData.pcmlName());
                }
                setField(field, parent, value);
            }
            
        /*-------------------------------------------------------*/
    	/* 2. Champs avec annotation "@Array"					 */
        /*-------------------------------------------------------*/
        } else if (field.isAnnotationPresent(Array.class)) {
            Array pcmlArray = field.getAnnotation(Array.class);
            
            // Le type du champ doit etre OUTPUT ou INPUTOUTPUT, on ne doit pas getter les INPUT
            if (!pcmlArray.usage().equals(UsageType.OUTPUT) && !pcmlArray.usage().equals(UsageType.INPUTOUTPUT)) {
                return;
            }
            
            // On recupere le tableau depuis le parent
            List<Object> array = (List<Object>) getField(field, parent);
            array.clear();
            
            // On calcule le path absolu du champ dans le PCML
            path = path + "." + pcmlArray.pcmlName();
            
        	// Creation des indices
        	int indices[];
        	if(tabIndices.length > 0){
        		// Tableau dans un tableau
        		indices = new int[2];
        	}else{
        		// Tableau simple
        		indices = new int[1];
        	}
        	
            /*-------------------------------------------------------*/
    		/* 2.1 Le tableau contient des objets "@Struct"          */
            /*-------------------------------------------------------*/
            if (pcmlArray.type().isAnnotationPresent(Struct.class)) {
            	
                for (int i = 0; i < pcmlArray.size(); i++) {
                	
                	// Positionnement des indices
                    if(tabIndices.length > 0){
                    	// Tableau dans un tableau
                		indices[0] = tabIndices[0];
                		indices[1] = i;
                    }else{
                    	// Tableau simple
                    	indices[0] = i;
                    }
                    
                    // Parcours du tableau
                    Object elementArray = pcmlArray.type().getDeclaredConstructor().newInstance();
                    for (Field structField : pcmlArray.type().getDeclaredFields()) {
                        if (structField.isAnnotationPresent(Data.class)) {
                            Data pcmlData = structField.getAnnotation(Data.class);
                            Object value = pcmlDoc.getValue(path + "." + pcmlData.pcmlName(), indices);
                            setField(structField, elementArray, value);
                        }
                    }
                    array.add(elementArray);
                }
                
            /*-------------------------------------------------------*/
        	/* 2.2 Le tableau contient des champs standards		     */
            /*-------------------------------------------------------*/
            } else {

            	// Parcours du tableau
                for (int i = 0; i < pcmlArray.size(); i++) {
                	if(tabIndices.length > 0){
                		// Tableau dans un tableau
                		indices[0] = tabIndices[0];
                		indices[1] = i;
                	}else{
                		// Tableau simple
                		indices[0] = i;
                	}
                    
                    // Get value dans un tableau : on passe les indices
                    Object value = pcmlDoc.getValue(path, indices);
                    array.add(value);
                }

                setField(field, parent, array);
            }
        }
    }

    /**
     * Recuperation de la valeur d'un champ depuis son parent
     * @param field
     * @param object
     * @return field value
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private static Object getField(Field field, Object object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        String methodName = "get" + Util.toCamelCase(field.getName());
        Method method = field.getDeclaringClass().getMethod(methodName);
        return method.invoke(object);
    }

    /**
     * @param field
     * @param object
     * @param value
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private static void setField(Field field, Object object, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        String methodName = "set" + Util.toCamelCase(field.getName());
        Method method = field.getDeclaringClass().getMethod(methodName, value.getClass());
        method.invoke(object, value);
    }
}
