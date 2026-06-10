package com.registerfoot.ui.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.function.Function;

/** Utilidad para crear columnas de TableView a partir de DTOs tipo record. */
final class UiTables {

    private UiTables() {}

    static <S> TableColumn<S, String> col(String titulo, Function<S, String> ext) {
        TableColumn<S, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(cd -> new ReadOnlyStringWrapper(ext.apply(cd.getValue())));
        return c;
    }
}
