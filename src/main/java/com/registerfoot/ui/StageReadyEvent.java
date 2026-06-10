package com.registerfoot.ui;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

/** Evento publicado cuando JavaFX entrega el Stage primario a Spring. */
public class StageReadyEvent extends ApplicationEvent {
    public StageReadyEvent(Stage stage) { super(stage); }
    public Stage getStage() { return (Stage) getSource(); }
}
