package com.example.lector_model.api;

import com.example.lector_model.model.ModelManager;
import com.example.lector_model.transformer.AttributeDTO;
import com.example.lector_model.weka.WekaCore;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/modelo")
@CrossOrigin(origins = "*") // Permite conexión sin problemas de CORS
public class ApiController {

    private final ModelManager modelManager;
    private final WekaCore wekaCore;

    // Inyección de los componentes requeridos
    public ApiController(ModelManager modelManager, WekaCore wekaCore) {
        this.modelManager = modelManager;
        this.wekaCore = wekaCore;
    }

    /**
     * Endpoint para cargar un nuevo archivo .model
     * Recibe el archivo binario enviado desde el Frontend y retorna la estructura del formulario.
     */
    @PostMapping("/cargar")
    public ResponseEntity<?> cargarModelo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío o no es válido.");
        }

        try {
            // Extraemos el flujo de bytes (InputStream) abstrayendo la capa de red del manager
            List<AttributeDTO> metadatos = modelManager.loadNewModel(file.getInputStream());
            return ResponseEntity.ok(metadatos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el archivo .model: " + e.getMessage());
        }
    }

    /**
     * Endpoint para realizar la predicción analítica.
     * Recibe un JSON en forma de Mapa (Clave: Nombre Atributo, Valor: Lo que llenó el usuario).
     */
    @PostMapping("/predecir")
    public ResponseEntity<?> predecir(@RequestBody Map<String, String> datosFormulario) {
        try {
            // Validamos que exista un modelo en la memoria RAM antes de intentar predecir
            if (wekaCore.getReferenceStructure() == null) {
                return ResponseEntity.badRequest().body("No hay ningún modelo activo en memoria. Carga uno primero.");
            }

            // Delegamos los datos al motor analítico para computar el resultado conceptual
            String resultadoPrediccion = wekaCore.predict(datosFormulario);
            return ResponseEntity.ok(resultadoPrediccion);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al calcular la predicción: " + e.getMessage());
        }
    }
}