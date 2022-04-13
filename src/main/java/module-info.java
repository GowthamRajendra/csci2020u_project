module com.project.csci2020u_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.project.csci2020u_project to javafx.fxml;
    exports com.project.csci2020u_project;
}