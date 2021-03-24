/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

module Appmanager {
    requires javafx.fxml;
    requires javafx.controls;
    opens org.openjfx to javafx.fxml;
    exports org.openjfx;
}
