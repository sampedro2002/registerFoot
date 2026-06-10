package com.registerfoot.ui.view;

import com.registerfoot.domain.enums.Rol;
import javafx.scene.Node;

import java.util.Set;

/**
 * Contrato de un modulo del menu lateral. Cada modulo declara su nombre,
 * los roles autorizados y construye su nodo de contenido bajo demanda.
 */
public interface ModuleView {

    String nombre();

    String icono(); // emoji simple como icono

    Set<Rol> rolesPermitidos();

    Node crear();

    default int orden() { return 100; }

    default boolean visiblePara(Rol rol) {
        return rolesPermitidos().contains(rol);
    }
}
