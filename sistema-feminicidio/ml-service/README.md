# Análisis y servicio experimental de Machine Learning

Este componente se mantiene separado del frontend y de Spring Boot. Su primera etapa audita ENDES 2025 directamente desde los ZIP originales, sin extraerlos ni imprimir identificadores o filas. Las salidas del análisis son agregadas.

## Entorno

Requiere Python 3.14. Desde esta carpeta, crea y activa un entorno virtual e instala `requirements-lock.txt` para reproducir las versiones verificadas:

```powershell
py -3.14 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements-lock.txt
```

## Auditoría reproducible

```powershell
python .\scripts\audit_endes.py
python .\scripts\build_feature_dictionary.py
python -m pytest
```

El script espera los archivos locales en `../Proyecto_Feminicidio_Datos/`. La carpeta `01_Datos_Originales` está excluida de Git; no muevas los ZIP originales a una carpeta versionada ni subas registros.

## Experimento retrospectivo y artefacto candidato

```powershell
python .\scripts\train_compare.py
```

Compara Decision Tree, Random Forest, Logistic Regression, XGBoost, SVM y KNN para clasificar D107, un indicador retrospectivo derivado de respuestas del módulo de violencia. Solo incluye D107 observado con V044=1 (seleccionada y entrevistada), separa los registros por conglomerado V021, excluye D105E/F/G y D106/D107/D108 de las entradas, selecciona con validación agrupada y evalúa en un holdout por conglomerado. En el extracto, los D107 vacíos coinciden con V501=0/V502=0 (nunca casada); el cuestionario 2025 dirige esa respuesta desde 1001 a 1002 y no muestra en las páginas pertinentes un salto explícito que explique por sí solo esos vacíos. No se recodifican como negativos ni se filtra por estado conyugal. El experimento conserva métricas sin ponderar: la ficha ENDES 2024 del indicador de violencia especifica V005 salvo en 2020, pero no se encontró una especificación 2025 directa para el objetivo D107 y la elegibilidad exacta V044=1. Las métricas describen la muestra elegible y no se presentan como estimaciones poblacionales.

Además del informe agregado y su figura, el script guarda en `artifacts/` un candidato local versionado por metadatos, elegido solo por la mayor PR-AUC media de validación agrupada. Se reentrena con los registros elegibles después de medir el holdout; **no se selecciona para uso en el producto**. Tanto el binario del modelo como su JSON quedan excluidos de Git por `.gitignore` y deben reconstruirse con el ZIP original local. No se guarda un dataset procesado ni identificadores.

## Servicio FastAPI experimental

Entrena primero el candidato anterior; luego inicia el servicio:

```powershell
python -m uvicorn app.main:app --reload
```

- `GET /health` informa por separado si el proceso está activo y si el artefacto experimental se cargó. Siempre indica `product_prediction_enabled=false`.
- `POST /experimental/classify-d107` exige los 19 campos del esquema ENDES exacto. Devuelve una clase retrospectiva D107 y, si existe, el score bruto del modelo; el score no es una probabilidad individual calibrada. `D114` puede enviarse como `null` únicamente cuando `D113=0`; el valor faltante no se convierte en “Nunca”.
- Sin artefacto, la ruta responde `503`; no genera resultados ficticios. FastAPI publica OpenAPI en `/docs`.

La ruta es solo experimental. No predice feminicidio ni violencia futura, no tiene correspondencia validada con V1–V12 y no está conectada al frontend, Spring Boot ni PostgreSQL. No la expongas a profesionales ni a Internet.

**No ejecuta ni publica un predictor de feminicidio futuro.** La interfaz del producto y el modelo experimental no comparten un diccionario de variables validado.

## Informes y alcance metodológico

- [`docs/data_audit.md`](docs/data_audit.md): auditoría agregada de estructura, códigos, faltantes, enlace de diseño, correspondencia tentativa con V1–V12 y viabilidad científica.
- [`docs/feature_dictionary.md`](docs/feature_dictionary.md): variables, códigos oficiales y tratamiento aplicado en el experimento.
- [`docs/model_results.md`](docs/model_results.md): resultados y métricas del último experimento ejecutado.
- [`docs/model_comparison.png`](docs/model_comparison.png): curvas ROC, precisión-recall y calibración sin ponderar de la muestra elegible, además del resumen PR-AUC.

La ENDES es transversal. El módulo REC84DV contiene indicadores de violencia reportada y agregados derivados; no contiene un desenlace futuro de feminicidio. El artefacto local conserva solo un pipeline experimental; no es un modelo seleccionado para la aplicación. El servicio no se conecta al cuestionario de 12 preguntas, al frontend, a Spring Boot ni a PostgreSQL.
