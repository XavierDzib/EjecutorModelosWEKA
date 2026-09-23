package com.example.lector_model.weka;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.clusterers.Clusterer;
import weka.core.DenseInstance;
import weka.core.Instance;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class WekaCore {

    // Variables de estado en memoria (se sobreescriben al cargar un nuevo modelo)
    private Object model; // Permite almacenar Classifier o Clusterer
    private Instances referenceStructure;

    /**
     * Carga el archivo .model binario en memoria usando WEKA.
     * Al ser un InputStream, nos permite leerlo directamente desde la petición HTTP sin guardarlo en disco.
     */
    public void loadModel(InputStream modelStream) throws Exception {
        // Ejecuta la deserialización nativa de WEKA
        Object[] objects = SerializationHelper.readAll(modelStream);
        // Validación básica de estructura
        if (!(objects[0] instanceof Classifier) && !(objects[0] instanceof Clusterer)) {
            throw new IllegalArgumentException("El archivo .model no contiene ni un Classifier ni un Clusterer válido.");
        }
        if (!(objects[1] instanceof Instances)) {
            throw new IllegalArgumentException("El objeto no contiene Instances válidas.");
        }

        this.model = objects[0];
        this.referenceStructure = (Instances) objects[1];

        // Configuración de clase solo si el modelo es un Classifier supervisado
        if (this.model instanceof Classifier) {
            if (this.referenceStructure.classIndex() == -1) {
                this.referenceStructure.setClassIndex(this.referenceStructure.numAttributes() - 1);
            }
        } else {
            // Si es un Clusterer, no existe atributo clase predictora
            this.referenceStructure.setClassIndex(-1);
        }
    }

    /**
     * Recibe una instancia de datos ya preparada con los valores del formulario
     * y utiliza el clasificador en memoria para predecir el resultado.
     */
    public String makePrediction(Instance instance) throws Exception {
        if (!isModelLoaded()) {
            throw new IllegalStateException("No hay ningún modelo cargado en memoria.");
        }

        if (this.model instanceof Classifier) {
            Classifier classifier = (Classifier) this.model;
            double classValueIndex = classifier.classifyInstance(instance);

            if (this.referenceStructure.classAttribute() != null && this.referenceStructure.classAttribute().isNominal()) {
                return this.referenceStructure.classAttribute().value((int) classValueIndex);
            } else {
                return String.valueOf(classValueIndex);
            }
        } else if (this.model instanceof Clusterer) {
            Clusterer clusterer = (Clusterer) this.model;
            // Retorna el índice del cluster al que pertenece la instancia
            int clusterIndex = clusterer.clusterInstance(instance);
            return "Cluster asignado: " + clusterIndex;
        }

        throw new IllegalStateException("Tipo de modelo no soportado.");
    }
    
    /**
    * Recibe el mapa del controlador, construye la Instance de WEKA,
    * delega la ejecución del método makePrediction y la devuelve.
    */
    public String predict(Map<String, String> dataInput) throws Exception {
        if (!isModelLoaded()) {
            throw new IllegalStateException("No hay un modelo cargado en memoria.");
        }

        // 1. Crear la fila virtual con el tamaño exacto de columnas que espera WEKA
        Instance instance = new DenseInstance(this.referenceStructure.numAttributes());
        instance.setDataset(this.referenceStructure);

        // 2. Mapear los datos de texto que envió el usuario hacia el objeto de WEKA
        for (int i = 0; i < this.referenceStructure.numAttributes(); i++) {
            // Si es clasificación, omitimos la clase; si es clustering, leemos todos los atributos
            if (i == this.referenceStructure.classIndex()) {
                continue;
            }

            weka.core.Attribute attr = this.referenceStructure.attribute(i);
            String valueStr = dataInput.get(attr.name());

            if (valueStr == null || valueStr.trim().isEmpty()) {
                instance.setMissing(attr);
            } else {
                if (attr.isNumeric()) {
                    instance.setValue(attr, Double.parseDouble(valueStr));
                } else {
                    instance.setValue(attr, valueStr);
                }
            }
        }

        return makePrediction(instance);
    }

    /**
     * Devuelve la estructura de referencia de los datos que espera el modelo cargado.
     */
    public Instances getReferenceStructure() {
        return this.referenceStructure;
    }

    /**
     * Utilidad para verificar rápidamente si el motor tiene un modelo listo
     */
    public boolean isModelLoaded() {
        return this.model != null && this.referenceStructure != null;
    }
}