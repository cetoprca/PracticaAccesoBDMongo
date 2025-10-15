package app.objects;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Specialty {
    public SimpleIntegerProperty id;
    public SimpleStringProperty especialidad;

    public Specialty(int id, String especialidad){
        this.id = new SimpleIntegerProperty(id);
        this.especialidad = new SimpleStringProperty(especialidad);
    }

    public Specialty(){}

    public void setId(int id) {
        this.id = new SimpleIntegerProperty(id);
    }

    public void setEspecialidad(String especialidad){
        this.especialidad = new SimpleStringProperty(especialidad);
    }
}
