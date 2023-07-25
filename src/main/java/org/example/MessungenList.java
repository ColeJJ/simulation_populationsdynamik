package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MessungenList {

    private ObservableList<Messung> messungen;

    MessungenList() {
        this.messungen = FXCollections.observableArrayList();
    }

    public ObservableList<Messung> getMessungen(){
        return this.messungen;
    }

    public void add(Messung messung) {
        this.messungen.add(messung);
    }

    public void clear() {
        this.messungen.clear();
    }
}
