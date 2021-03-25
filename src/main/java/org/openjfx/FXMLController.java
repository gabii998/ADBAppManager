/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openjfx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Ryzen 5 2600
 */
public class FXMLController {
    @FXML
    private Label label;
    @FXML
    private Label deviceInfo;
    @FXML
    private ChoiceBox dispositivos;
    @FXML
    private ListView apps;
    
    public void initialize() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        getAdb();

        apps.setOnMouseClicked(event -> {
            String message="¿Seguro que desea deshabilitar este paquete?\n";
            message+="Package Name: "+apps.getSelectionModel().getSelectedItem();
            //message+="\nApplication Name:";

            Alert alert=new Alert(Alert.AlertType.NONE,message, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                disableApplication((String) dispositivos.getSelectionModel().getSelectedItem(),(String)apps.getSelectionModel().getSelectedItem());
                getApplications((String) dispositivos.getSelectionModel().getSelectedItem());
            }
            System.out.println(apps.getSelectionModel().getSelectedItem());
        });
        
        dispositivos.setOnAction((event) -> {
            //System.out.println("Pase por aca");
            int selectedIndex=dispositivos.getSelectionModel().getSelectedIndex();
            //System.out.println("selectedIndex"+selectedIndex);
            Object selectedItem = dispositivos.getSelectionModel().getSelectedItem();
            //System.out.println("selectedItem"+selectedItem);
            getDeviceInfo((String)selectedItem);
            Thread t1=new Thread(){
                @Override
                public void run() {
                    getApplications((String)selectedItem);
                }
                
            };
            t1.start();
            
        });
    }  
    
    private void getAdb(){
        try {
            final String commands[] = {"adb", "devices"};
            ProcessBuilder builder= new ProcessBuilder(commands);
            Process pr = builder.start();
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
          
            while ((line=buf.readLine())!=null) {
                if(!line.equals("List of devices attached"))
                dispositivos.getItems().add(line.replace("device","").replaceAll("\\s", ""));
            }  
        } catch (IOException | InterruptedException ex) {
            label.setText(ex.getLocalizedMessage());         
        }
    }
    
    private void getDeviceInfo(String deviceId){
        try {  
            final String commands[] = {"adb", "-s",deviceId.replaceAll("\\s", ""),"shell","getprop","ro.product.model"};
            ProcessBuilder builder= new ProcessBuilder(commands);
            Process pr = builder.start();
           
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
           
            
            while ((line=buf.readLine())!=null) {
                deviceInfo.setText(line);
            }  
        } catch (IOException ex) {
            label.setText(ex.getLocalizedMessage()); 
            System.out.println(ex.getLocalizedMessage());
        } 
    }
    
    private void getApplications(String deviceId){
        /*
        Options:
        -f: see their associated file
        -d: filter to only show disabled packages
        -e: filter to only show enabled packages
        -s: filter to only show system packages
        -3: filter to only show third party packages
        -i: see the installer for the packages
        -l: ignored (used for compatibility with older releases)
        -U: also show the package UID
        -u: also include uninstalled packages
        --uid UID: filter to only show packages with the given UID
        --user USER_ID: only list packages belonging to the given user
        */
        try {
            String[] forbiddenApps=new String[]{"android","com.android.settings","com.android.systemui","com.samsung.android.kgclient"};
            List<String> forbiddenList=Arrays.asList(forbiddenApps);
            
            List<String> appsList = new ArrayList();
            final String commands[] = {"adb", "-s",deviceId.replaceAll("\\s", ""),"shell","cmd","package","list","packages","-s","-e","|","sort"};
            ProcessBuilder builder= new ProcessBuilder(commands);
            Process pr = builder.start();
           
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            
            while ((line=buf.readLine())!=null) {
                String temp=line.replaceAll("package:","");
                temp=temp.replaceAll(" ", "");
                
                if(!forbiddenList.contains(temp) ){
                    appsList.add(temp);
                }
                
            }  
            ObservableList<String> it=FXCollections.observableArrayList(appsList);
            apps.setItems(it);
        } catch (IOException ex) {
            label.setText(ex.getLocalizedMessage()); 
            System.out.println(ex.getLocalizedMessage());
        } 
    }
    
    private void disableApplication(String deviceId,String packageName){
        try {
            final String commands[] = {"adb", "-s",deviceId.replaceAll("\\s", ""),"shell","pm","disable-user","--user","0",packageName};
            ProcessBuilder builder= new ProcessBuilder(commands);
            Process pr = builder.start();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            
            while ((line=buf.readLine())!=null) {
                
            } 
            getApplications(deviceId);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
