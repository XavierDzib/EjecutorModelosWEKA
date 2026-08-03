document.getElementById('formGenerador').addEventListener('submit', async function(e) {
    e.preventDefault(); // Evita la recarga normal de la página

    const resultadoDiv = document.getElementById('resultado');
    const classifierFile = document.getElementById('classifierFile').files[0];
    const structureFile = document.getElementById('structureFile').files[0];

    // Preparar el multipart/form-data
    const formData = new FormData();
    formData.append('classifierFile', classifierFile);
    formData.append('structureFile', structureFile);

    resultadoDiv.style.display = 'block';
    resultadoDiv.className = '';
    resultadoDiv.innerHTML = "Procesando y generando el modelo compatible en memoria...";

    try {
        // Enviar archivos mediante fetch al endpoint
        const response = await fetch('/api/modelo/generar', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('Error en el servidor al generar el modelo.');
        }

        // Recoger la respuesta como un Blob binario
        const blob = await response.blob();

        // Crear un enlace temporal para forzar la descarga del archivo binario
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'modelo_completo.model'; // Nombre sugerido de descarga
        document.body.appendChild(a);
        a.click(); // Ejecuta la descarga
        
        // Limpiar recursos
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        // Mostrar mensaje de éxito y enlace de retorno
        resultadoDiv.className = 'exito';
        resultadoDiv.innerHTML = `Modelo correcto generado.`;

    } catch (error) {
        console.error(error);
        resultadoDiv.className = 'error';
        resultadoDiv.innerHTML = 'Ocurrió un error al intentar generar el modelo. Revisa las consolas del navegador y del servidor.';
    }
});