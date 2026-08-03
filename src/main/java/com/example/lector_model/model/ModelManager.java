package com.example.lector_model.model;

import com.example.lector_model.transformer.AttributeDTO;
import com.example.lector_model.transformer.MetadataTransformer;
import com.example.lector_model.weka.WekaCore;

import org.springframework.stereotype.Service;

import weka.core.Instances;

import java.io.InputStream;
import java.util.List;

@Service
public class ModelManager {

    private final WekaCore wekaCore;

    // Inyección de dependencias por constructor
    public ModelManager(WekaCore wekaCore) {
        this.wekaCore = wekaCore;
    }

    /**
     * Estrategia de carga rápida "Sobrescribir en Memoria".
     * Recibe el InputStream del nuevo archivo enviado por el usuario,
     * reemplaza el modelo anterior en memoria RAM y devuelve los metadatos.
     */
    public List<AttributeDTO> loadNewModel(InputStream modelStream) throws Exception {
        // Delegar al motor core la deserialización y actualización en memoria RAM
        wekaCore.loadModel(modelStream);

        // Recuperar la nueva estructura de referencia que se acaba de guardar
        Instances estructura = wekaCore.getReferenceStructure();

        // Traducir la estructura nativa de WEKA al listado DTO para el Frontend
        return MetadataTransformer.transform(estructura);
    }

    /**
     * Permite consultar la estructura del modelo que se encuentra activo en memoria.
     */
    public List<AttributeDTO> getCurrentModelMetadata() {
        Instances estructura = wekaCore.getReferenceStructure();
        if (estructura == null) {
            return List.of(); // Devuelve lista vacía si aún no se ha cargado ningún modelo
        }
        return MetadataTransformer.transform(estructura);
    }
}