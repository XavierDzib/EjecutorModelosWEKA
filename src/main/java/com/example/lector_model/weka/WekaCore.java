package com.example.lector_model.weka;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class WekaCore {

    // Variables de estado en memoria (se sobreescriben al cargar un nuevo modelo)
    private Classifier model;
    private Instances referenceStructure;

    /**
     * Carga el archivo .model binario en memoria usando WEKA.
     * Al ser un InputStream, nos permite leerlo directamente desde la petición HTTP sin guardarlo en disco.
     */
    public void loadModel(InputStream modelStream) throws Exception {
        // Ejecuta la deserialización nativa de WEKA
        Object[] objects = SerializationHelper.readAll(modelStream);
        // Validación básica de estructura
        if (objects == null || objects.length < 2) {
            throw new IllegalArgumentException(
            "El archivo .model no contiene la estructura requerida [Classifier, Instances]."
            );
        }
        // Validación de tipos
        if (!(objects[0] instanceof Classifier)) {
            throw new IllegalArgumentException(
                "El primer objeto del .model no es un Classifier válido."
            );
        }

        if (!(objects[1] instanceof Instances)) {
            throw new IllegalArgumentException(
                "El segundo objeto del .model no contiene Instances válidas."
            );
        }
        
        // Asignamos y sobreescribimos las variables globales en memoria
        this.model = (Classifier) objects[0];
        this.referenceStructure = (Instances) objects[1];

        // Establecemos por seguridad que el último atributo es la clase predictora (si no viene predefinida)
        if (this.referenceStructure.classIndex() == -1) {
            this.referenceStructure.setClassIndex(this.referenceStructure.numAttributes() - 1);
        }
    }

    // Getters para que los otros módulos consulten el estado
    
    public Classifier getModel() {
        return this.model;
    }

    public Instances getReferenceStructure() {
        return this.referenceStructure;
    }

    /**
     * Utilidad para verificar rápidamente si el motor tiene un modelo listo
     */
    public boolean isModelLoaded() {
        return this.model != null && this.referenceStructure != null;
    }

    /**
     * Recibe una instancia de datos ya preparada con los valores del formulario
     * y utiliza el clasificador en memoria para predecir el resultado.
     */
    public String makePrediction(weka.core.Instance instance) throws Exception {
        if (!isModelLoaded()) {
            throw new IllegalStateException("No hay ningún modelo cargado en memoria para realizar predicciones.");
        }

        // El método clasifyInstance de WEKA devuelve el índice flotante del resultado
        double classValueIndex = this.model.classifyInstance(instance);

        // Si la clase predictora es categórica/nominal, convertimos el índice al texto legible (ej: "Sí", "No")
        if (this.referenceStructure.classAttribute().isNominal()) {
            return this.referenceStructure.classAttribute().value((int) classValueIndex);
        } else {
            // Si la clase es numérica, devolvemos el número directo convertido a texto
            return String.valueOf(classValueIndex);
        }
    }
    
    /**
    * Recibe el mapa del controlador, construye la Instance de WEKA,
    * delega la ejecución del método makePrediction y la devuelve.
    */
    public String predict(Map<String, String> dataInput) throws Exception {
        if (this.referenceStructure == null || this.model == null) {
            throw new IllegalStateException("No hay un modelo cargado en memoria para realizar predicciones.");
        }

        // 1. Crear la fila virtual con el tamaño exacto de columnas que espera WEKA
        weka.core.Instance instance = new weka.core.DenseInstance(this.referenceStructure.numAttributes());
        instance.setDataset(this.referenceStructure);

        // 2. Mapear los datos de texto que envió el usuario hacia el objeto de WEKA
        for (int i = 0; i < this.referenceStructure.numAttributes(); i++) {
            weka.core.Attribute attr = this.referenceStructure.attribute(i);

            // Saltamos el atributo clase, que es el que vamos a predecir
            if (i == this.referenceStructure.classIndex()) {
                continue;
            }

            String valueStr = dataInput.get(attr.name());

            if (valueStr == null || valueStr.trim().isEmpty()) {
                instance.setMissing(attr);
            } else {
                if (attr.isNumeric()) {
                    instance.setValue(attr, Double.parseDouble(valueStr));
                } else if (attr.isNominal()) {
                    instance.setValue(attr, valueStr);
                } else {
                    instance.setValue(attr, valueStr);
                }
            }
        }
        // 3. Utilizar el método pasándole la instancia ya estructurada
        return makePrediction(instance);
    }
}