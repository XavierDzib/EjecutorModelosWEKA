package com.example.lector_model.transformer;

import weka.core.Attribute;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class MetadataTransformer {

    /**
     * Convierte la estructura de WEKA en una lista limpia de AttributeDTO.
     * Omite automáticamente el atributo clase/predictor.
     */
    public static List<AttributeDTO> transform(Instances structure) {
        List<AttributeDTO> dtoList = new ArrayList<>();
        
        int classIndex = structure.classIndex();
        int numAttributes = structure.numAttributes();

         // Recorremos todos los atributos que espera el modelo
        for (int i = 0; i < numAttributes; i++) {
            // 1. Regla para modelos supervisados (árboles): Omitir el índice de la clase objetivo
            if (classIndex != -1 && i == classIndex) {
                continue; 
            }

            Attribute attr = structure.attribute(i);
            String name = attr.name();

            // 2. Regla para modelos no supervisados (clustering, classIndex == -1):
            // Ignorar la columna si contiene el nombre de la etiqueta/especie
            if (classIndex == -1 && (name.equalsIgnoreCase("class") || name.equalsIgnoreCase("especie") || name.equalsIgnoreCase("target") || name.equalsIgnoreCase("species"))) {
                continue;
            }

            String type;

            // Identificar el tipo de dato adaptándolo a estándares web
            if (attr.isNumeric()) {
                type = "numeric";
                dtoList.add(new AttributeDTO(name, type));
                
            } else if (attr.isNominal()) {
                type = "nominal";
                // Extraer todas las opciones posibles que acepta este atributo categórico
                List<String> options = new ArrayList<>();
                for (int j = 0; j < attr.numValues(); j++) {
                    options.add(attr.value(j));
                }
                // Usamos el constructor que incluye la lista de opciones/categorías
                dtoList.add(new AttributeDTO(name, type, options));
                
            } else {
                // Manejo opcional por si el modelo incluye fechas, texto plano, etc.
                type = "text";
                dtoList.add(new AttributeDTO(name, type));
            }
        }

        return dtoList;
    }
}