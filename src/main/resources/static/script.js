// Configuración de la URL base del Backend en Spring Boot
const API_BASE_URL = '/api/modelo';

// Referencias a los elementos del DOM de index.html
const formCargarModelo = document.getElementById('form-cargar-modelo');
const archivoModelInput = document.getElementById('archivo-model');
const mensajeEstado = document.getElementById('mensaje-estado');

const seccionFormulario = document.getElementById('seccion-formulario');
const formPrediccion = document.getElementById('form-prediccion');
const contenedorCampos = document.getElementById('contenedor-campos');

const seccionResultado = document.getElementById('seccion-resultado');
const textoResultado = document.getElementById('texto-resultado');

/**
 * EVENTO 1: Envío del archivo .model al Backend (/cargar)
 */
formCargarModelo.addEventListener('submit', async (e) => {
    e.preventDefault(); // Evita que la página se recargue

    const file = archivoModelInput.files[0];
    if (!file) return;

    // Preparar el archivo en un contenedor binario multiparte de red
    const formData = new FormData();
    formData.append('file', file);

    mensajeEstado.textContent = "Procesando archivo .model y extrayendo metadatos...";
    mensajeEstado.style.color = "orange";

    // Ocultar secciones previas en caso de que ya existiera otro modelo en memoria
    seccionFormulario.style.display = 'none';
    seccionResultado.style.display = 'none';

    try {
        const response = await fetch(`${API_BASE_URL}/cargar`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error(`Error en el servidor: ${response.statusText}`);
        }

        // Recibir la lista de AttributeDTO (JSON) devuelta por MetadataTransformer
        const metadatosAtributos = await response.json();
        
        mensajeEstado.textContent = "¡Modelo cargado exitosamente en la memoria RAM!";
        mensajeEstado.style.color = "green";

        // Invocar la función encargada de construir el formulario con los metadatos recibidos
        renderizarFormularioDinamico(metadatosAtributos);

    } catch (error) {
        console.error(error);
        mensajeEstado.textContent = `Error al cargar el modelo: ${error.message}`;
        mensajeEstado.style.color = "red";
    }
});

/**
 * FUNCIÓN: Renderiza dinámicamente los campos basados en la lista de AttributeDTO
 * @param {Array} atributos 
 */
function renderizarFormularioDinamico(atributos) {
    // Limpiar campos de cualquier modelo previo que haya estado cargado en el contenedor
    contenedorCampos.innerHTML = '';

    if (atributos.length === 0) {
        contenedorCampos.innerHTML = '<p>El modelo no requiere variables de entrada.</p>';
        return;
    }

    // Recorrer iterativamente cada atributo devuelto por el Backend (omitiendo la clase predictora)
    atributos.forEach(attr => {
        // Crear un contenedor de grupo para cada campo individual
        const grupoDiv = document.createElement('div');
        grupoDiv.style.marginBottom = '15px';

        // Crear etiqueta <label> con el nombre original del atributo en WEKA
        const label = document.createElement('label');
        label.textContent = `${attr.name}: `;
        label.setAttribute('for', attr.name);
        grupoDiv.appendChild(label);

        // Bifurcar la creación del elemento HTML según el tipo de dato web estandarizado
        if (attr.type === 'numeric') {
            // Requerimiento Numérico: Generar un <input type="number"> con validación nativa
            const inputNumeric = document.createElement('input');
            inputNumeric.type = 'number';
            inputNumeric.step = 'any'; // Permite decimales continuos flotantes
            inputNumeric.id = attr.name;
            inputNumeric.name = attr.name;
            inputNumeric.required = true; // Validación nativa para evitar nulos
            grupoDiv.appendChild(inputNumeric);

        } else if (attr.type === 'nominal') {
            // Requerimiento Categórico/Nominal: Generar un elemento de selección <select>
            const selectNominal = document.createElement('select');
            selectNominal.id = attr.name;
            selectNominal.name = attr.name;
            selectNominal.required = true;

            // Opción por defecto informativa deshabilitada
            const opcionPorDefecto = document.createElement('option');
            opcionPorDefecto.value = '';
            opcionPorDefecto.textContent = '-- Seleccione una opción --';
            selectNominal.appendChild(opcionPorDefecto);

            // Mapear e inyectar todas las categorías/opciones válidas que acepta el modelo
            attr.options.forEach(opcion => {
                const optionElement = document.createElement('option');
                optionElement.value = opcion;
                optionElement.textContent = opcion;
                selectNominal.appendChild(optionElement);
            });

            grupoDiv.appendChild(selectNominal);
        }

        // Incorporar el bloque estructurado al contenedor principal en pantalla
        contenedorCampos.appendChild(grupoDiv);
    });

    // Hacer visible la sección del formulario adaptado
    seccionFormulario.style.removeProperty ? seccionFormulario.style.removeProperty('display') : seccionFormulario.style.display = 'block';
}

/**
 * EVENTO 2: Envío de respuestas del formulario para procesar la predicción (/predecir)
 */
formPrediccion.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Capturar de forma automática los datos estructurados del formulario
    const formDataObj = new FormData(formPrediccion);
    const datosUsuario = {};

    // Transformar los valores ingresados a un Mapa simple (Clave: Nombre Atributo, Valor: Selección/Valor)
    formDataObj.forEach((value, key) => {
        datosUsuario[key] = value;
    });

    try {
        textoResultado.textContent = "Computando algoritmo de predicción en WEKA...";
        seccionResultado.style.display = 'block';

        // Enviar el JSON mapeado al endpoint especializado del ApiController
        const response = await fetch(`${API_BASE_URL}/predecir`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datosUsuario) // Conversión a cadena de texto JSON compatible
        });

        if (!response.ok) {
            const mensajeErrorBackend = await response.text();
            throw new Error(mensajeErrorBackend || `Error en el cálculo: ${response.status}`);
        }

        // El backend responde con un String puro representativo de la clasificación humana
        const resultadoFinal = await response.text();
        
        // Desplegar el veredicto en la zona de resultados
        textoResultado.textContent = resultadoFinal;

    } catch (error) {
        console.error(error);
        textoResultado.textContent = `Error en predicción: ${error.message}`;
    }
});