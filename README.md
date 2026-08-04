# Lector y Ejecutor de Modelos de Minería de Datos (WEKA)

Este proyecto permite cargar modelos binarios de Weka (`.model`) y realizar predicciones a través de una API REST.  
Está diseñado para ser **genérico**, adaptable a cualquier modelo de clasificación o regresión entrenado en WEKA.

---

## 📦 ¿Qué hace?

- 📥 Carga un archivo `.model` desde el frontend.
- 🔍 Extrae automáticamente los metadatos del modelo (atributos, tipos, opciones categóricas).
- 📋 Genera dinámicamente un formulario con los campos requeridos.
- 🧠 Recibe los datos ingresados por el usuario y ejecuta la predicción.
- 📤 Devuelve el resultado de la predicción en formato texto.

---

## ⚙️ Requisitos para ejecutar

- **Java JDK 11 o superior**
- **Maven**
- **WEKA**

---

## 🚀 ¿Cómo ejecutarlo?

```bash
# Clonar o descargar el proyecto
# Compilar con Maven
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
