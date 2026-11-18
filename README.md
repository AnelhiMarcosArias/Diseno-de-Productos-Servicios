# Diseño de Productos y Servicios

Repositorio académico del proyecto **Sistema Inteligente de Evaluación del Riesgo de Feminicidio mediante Machine Learning**.

La aplicación está organizada en [`sistema-feminicidio/`](sistema-feminicidio/), con frontend, backend, un componente de análisis ML y materiales de investigación separados por función. Los ZIP originales del INEI se mantienen localmente y no se versionan.

El componente ML conserva la investigación ENDES 2025 y su experimento retrospectivo de D107 (violencia severa reportada), que permanece separado del flujo de casos. El flujo de demostración V1–V12 usa un **segundo XGBoost entrenado con datos sintéticos** y produce un índice académico 0–100, no una probabilidad ni una predicción validada para personas reales. Las doce preguntas no se consideran equivalentes a variables ENDES.

Consulta [`sistema-feminicidio/README.md`](sistema-feminicidio/README.md) para ver la estructura y las instrucciones de ejecución.
