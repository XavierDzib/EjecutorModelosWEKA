package com.example.lector_model.transformer;

import java.util.List;

public class AttributeDTO {
    private String name;
    private String type; // Puede ser "numeric", "nominal", etc.
    private List<String> options; // Se llenará solo si es nominal/categórico

    // Constructor para atributos numéricos
    public AttributeDTO(String name, String type) {
        this.name = name;
        this.type = type;
        this.options = null;
    }

    // Constructor para atributos categóricos (nominales)
    public AttributeDTO(String name, String type, List<String> options) {
        this.name = name;
        this.type = type;
        this.options = options;
    }

    // Getters y Setters (Obligatorios para que Gson/Spring los conviertan a JSON)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}