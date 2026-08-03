package com.example.lector_model.weka;

import org.springframework.stereotype.Service;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class GeneradorModelo {

    /**
     * Recibe los flujos de datos del clasificador y la estructura ARFF,
     * y genera el archivo .model empaquetado completamente en memoria.
     */
    public byte[] generarModeloCompleto(InputStream classifierStream, InputStream structureStream) throws Exception {
        // 1. Deserializar el clasificador desde el stream
        Classifier classifier = (Classifier) SerializationHelper.read(classifierStream);

        // 2. Cargar la estructura (.arff) usando ArffLoader desde el stream
        ArffLoader loader = new ArffLoader();
        loader.setSource(structureStream);
        Instances data = loader.getDataSet();

        // 3. Definir atributo clase si no está asignado
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        // 4. Empaquetar ambos objetos en un arreglo Object[]
        Object[] modelData = new Object[] { classifier, data };

        // 5. Escribir el arreglo a un ByteArrayOutputStream en memoria
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationHelper.writeAll(baos, modelData);

        // Devolver el arreglo de bytes resultante
        return baos.toByteArray();
    }
}