# Modelo sintético académico para V1–V12

Este componente es **B: una demostración técnica**, separada del experimento ENDES D107. No estima la probabilidad de feminicidio, no predice violencia futura y no tiene validez clínica.

## Contrato

Se reciben exactamente V1–V12. Cada respuesta es obligatoria y se codifica como `NO`, `SI` o `NO_ESTOY_SEGURA`. La tercera opción no se convierte en “No”: para el modelo se codifica como 2 y queda identificada como respuesta que requiere revisión. El frontend no permite dejar preguntas sin responder en la finalización; las respuestas de incertidumbre se conservan literalmente.

## Datos y etiqueta sintéticos

El script `scripts/train_synthetic_v1_v12.py` genera 12 000 filas con semilla fija `20261117`, muestreo categórico reproducible y ninguna observación de personas. La etiqueta se fabrica con la regla didáctica `cantidad(SI) + 0.35 × cantidad(NO_ESTOY_SEGURA) >= 4.5`. Las doce variables tienen el mismo peso. No se afirma que esa regla represente evidencia académica o clínica.

XGBoost aprende a aproximar esa regla artificial. El índice 0–100 que devuelve el servicio es el score positivo del clasificador multiplicado por 100 y redondeado; no es una probabilidad individual calibrada ni un porcentaje de riesgo real. Las métricas sobre este conjunto solo describirían la reproducción de la propia regla y no se presentan como validación del sistema.

## Entrenamiento y servicio

Desde `ml-service/`, con el entorno existente activado:

```powershell
python .\scripts\train_synthetic_v1_v12.py
python -m uvicorn app.main:app --reload
```

El artefacto y metadatos son reproducibles y se mantienen bajo `artifacts/` local, excluidos de Git. ENDES D107 conserva su propio esquema y ruta experimental sin conexión a este modelo ni al flujo de casos.

## Salvaguardas

El resultado se presenta como **Índice experimental de señales identificadas (simulación académica)**. No se asignan categorías de riesgo ni recomendaciones automatizadas. Las respuestas V11 (amenazas graves) y V12 (estrangulamiento/asfixia o acceso a armas), cuando son afirmativas, se muestran aparte del índice para que este no las oculte. Siempre se requiere revisión profesional y el sistema no sustituye los canales de emergencia o servicios especializados.
