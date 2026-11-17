# Auditoría agregada de ENDES 2025: REC84DV

> Informe reproducible. No contiene filas, identificadores ni valores individuales.

## Cobertura y calidad

- Registros: 32,700.
- Variables: 221.
- CASEID no vacíos distintos: 32,700; duplicados: 0; vacíos: 0.
- Filas sin correspondencia al unir el módulo individual por CASEID: 0.
- Conglomerados V021 observados: 3244; estratos V022: 240.
- Pesos V005 vacíos, no numéricos o no positivos: 0.
- Estado de selección V044 en REC0111 (todas las filas del archivo individual): `0`: 10,840; `1`: 23,053; `2`: 18; `3`: 2,349.
- D107 por V044 en REC84DV enlazado: `0`: 0 observados/9,629, 9,629 sin D107 y 0 positivos; `1`: 19,271 observados/23,053, 3,782 sin D107 y 1,306 positivos; `2`: 0 observados/18, 18 sin D107 y 0 positivos.
- D107 por estado civil V501 (RE516171): `0`: 0 observados/9,721, 9,721 sin D107 y 0 positivos; `1`: 3,707 observados/4,399, 692 sin D107 y 177 positivos; `2`: 12,311 observados/14,262, 1,951 sin D107 y 504 positivos; `3`: 28 observados/42, 14 sin D107 y 2 positivos; `4`: 46 observados/62, 16 sin D107 y 14 positivos; `5`: 3,179 observados/4,214, 1,035 sin D107 y 609 positivos.
- D107 por estado civil V501 y situación conyugal V502 (RE516171): `0/0`: 0 observados/9,721, 9,721 sin D107 y 0 positivos; `1/1`: 3,707 observados/4,399, 692 sin D107 y 177 positivos; `2/1`: 12,311 observados/14,262, 1,951 sin D107 y 504 positivos; `3/2`: 28 observados/42, 14 sin D107 y 2 positivos; `4/2`: 46 observados/62, 16 sin D107 y 14 positivos; `5/2`: 3,179 observados/4,214, 1,035 sin D107 y 609 positivos.
- Campos de ponderación específicos del módulo encontrados en REC84DV: ninguno (no se encontró D005 ni V005V).
- En filas con D107 observado: 240 estratos V022; 46 sin positivos y 122 estratos positivos con menos de cinco conglomerados.
- Registros con D101A no vacío: 19,271. Este conteo no se interpreta por sí solo como elegibilidad.

## Identificación y procedencia del archivo

- Archivo local utilizado: `2025.zip` (70,915,430 bytes).
- SHA-256 del archivo local: `dc94de1b2da3cd83b012acb09b494e000b3b0607fb001ac7d5d90140d1473ba6`.
- El catálogo oficial del INEI publica un recurso denominado Data completa ENDES 2025. La estructura interna local incluye tablas ENDES 2025 compatibles con el análisis.
- La huella identifica los bytes usados para reproducir este informe; no demuestra por sí sola que el ZIP local sea idéntico al recurso oficial. No se dispuso de una suma de verificación oficial para compararla.

### Tipos de almacenamiento observados

Los CSV se leen como cadenas para conservar los códigos originales. La siguiente clasificación es solo una comprobación de compatibilidad numérica; la interpretación semántica requiere el diccionario oficial.

- all blank: 76 columnas.
- numeric-compatible codes: 144 columnas.
- text or identifier: 1 columnas.

### Mayor proporción de valores vacíos

| Variable | Vacíos | % |
|---|---:|---:|
| D101G | 32,700 | 100.00% |
| D101H | 32,700 | 100.00% |
| D101I | 32,700 | 100.00% |
| D101J | 32,700 | 100.00% |
| D103C | 32,700 | 100.00% |
| D103E | 32,700 | 100.00% |
| D103F | 32,700 | 100.00% |
| D115XJ | 32,700 | 100.00% |
| D115XH | 32,700 | 100.00% |
| D115XF | 32,700 | 100.00% |
| D115XE | 32,700 | 100.00% |
| D115XI | 32,700 | 100.00% |

## Distribución de códigos de violencia

Las frecuencias son agregadas y conservan el código literal; no traducen códigos especiales automáticamente.

| Variable | Códigos y recuentos |
|---|---|
| D101A | `0`: 13,639; `1`: 5,574; `8`: 58; `<blank>`: 13,429 |
| D101B | `0`: 16,832; `1`: 2,425; `8`: 14; `<blank>`: 13,429 |
| D101C | `0`: 17,523; `1`: 1,736; `8`: 12; `<blank>`: 13,429 |
| D101D | `0`: 17,911; `1`: 1,349; `8`: 11; `<blank>`: 13,429 |
| D101E | `0`: 15,534; `1`: 3,728; `8`: 9; `<blank>`: 13,429 |
| D101F | `0`: 17,449; `1`: 1,807; `8`: 15; `<blank>`: 13,429 |
| D101G | `<blank>`: 32,700 |
| D101H | `<blank>`: 32,700 |
| D101I | `<blank>`: 32,700 |
| D101J | `<blank>`: 32,700 |
| D103A | `0`: 16,639; `1`: 242; `2`: 1,162; `3`: 1,228; `<blank>`: 13,429 |
| D103B | `0`: 18,203; `1`: 105; `2`: 396; `3`: 567; `<blank>`: 13,429 |
| D103C | `<blank>`: 32,700 |
| D103D | `0`: 16,899; `1`: 245; `2`: 1,119; `3`: 1,008; `<blank>`: 13,429 |
| D103E | `<blank>`: 32,700 |
| D103F | `<blank>`: 32,700 |
| D104 | `0`: 15,532; `1`: 3,739; `<blank>`: 13,429 |
| D105A | `0`: 15,679; `1`: 104; `2`: 989; `3`: 2,499; `<blank>`: 13,429 |
| D105B | `0`: 16,938; `1`: 63; `2`: 597; `3`: 1,673; `<blank>`: 13,429 |
| D105C | `0`: 17,392; `1`: 51; `2`: 448; `3`: 1,380; `<blank>`: 13,429 |
| D105D | `0`: 18,141; `1`: 32; `2`: 238; `3`: 860; `<blank>`: 13,429 |
| D105E | `0`: 18,866; `1`: 14; `2`: 84; `3`: 307; `<blank>`: 13,429 |
| D105F | `0`: 19,047; `1`: 3; `2`: 57; `3`: 164; `<blank>`: 13,429 |
| D105G | `0`: 19,116; `1`: 3; `2`: 37; `3`: 115; `<blank>`: 13,429 |
| D105H | `0`: 18,465; `1`: 37; `2`: 226; `3`: 543; `<blank>`: 13,429 |
| D105I | `0`: 18,807; `1`: 21; `2`: 151; `3`: 292; `<blank>`: 13,429 |
| D105J | `<blank>`: 32,700 |
| D105K | `<blank>`: 32,700 |
| D105L | `<blank>`: 32,700 |
| D105M | `<blank>`: 32,700 |
| D105N | `<blank>`: 32,700 |
| D106 | `0`: 15,204; `1`: 4,067; `<blank>`: 13,429 |
| D107 | `0`: 17,965; `1`: 1,306; `<blank>`: 13,429 |
| D108 | `0`: 18,392; `1`: 879; `<blank>`: 13,429 |
| D109 | `0`: 1,157; `1`: 849; `10`: 96; `11`: 15; `12`: 101; `13`: 22; `14`: 17; `15`: 20; `16`: 13; `17`: 8; `18`: 8; `19`: 3; `2`: 601; `20`: 7; `21`: 5; `22`: 1; `23`: 1; `24`: 2; `25`: 2; `27`: 1; `29`: 2; `3`: 355; `30`: 1; `4`: 218; `5`: 212; `6`: 128; `7`: 94; `8`: 70; `9`: 34; `95`: 142; `96`: 11; `<blank>`: 28,504 |
| D110A | `0`: 1,628; `1`: 2,568; `<blank>`: 28,504 |
| D110B | `<blank>`: 32,700 |
| D110C | `0`: 3,727; `1`: 469; `<blank>`: 28,504 |
| D110D | `0`: 3,747; `1`: 449; `<blank>`: 28,504 |
| D110E | `0`: 4,084; `1`: 112; `<blank>`: 28,504 |
| D110F | `<blank>`: 32,700 |
| D110G | `<blank>`: 32,700 |
| D110H | `<blank>`: 32,700 |
| D112 | `0`: 18,076; `1`: 1,195; `<blank>`: 13,429 |
| D113 | `0`: 3,261; `1`: 16,010; `<blank>`: 13,429 |
| D114 | `0`: 3,717; `1`: 946; `2`: 11,347; `<blank>`: 16,690 |

## Correspondencia preliminar con las preguntas de la aplicación

El frontend y el backend guardan doce booleanos genéricos; no hay correspondencia validada con ENDES. Esta tabla indica cercanía textual, no una recodificación ni autorización para usar estas respuestas como entradas del modelo.

| Pregunta del frontend | Campo(s) ENDES cercano(s) | Diferencia que impide equipararlos |
|---|---|---|
| Celos y aislamiento | D101A, D101C, D101D | ENDES separa celos, impedir visitas de amistades y limitar contactos familiares; la aplicación los agrupa y añade estudios. |
| Control de actividades | D101E, D101F | ENDES pregunta por conocer lugares y dinero; no cubre celular, redes ni ubicación digital. |
| Insultos y humillación | D103A; D103C en diccionario | D103A es humillación delante de otras personas; D103C está vacío en todas las filas observadas. |
| Intimidación / destrucción de objetos | Sin correspondencia exacta | D103B pregunta por amenaza de daño, no por gritos o destrucción de pertenencias. |
| Agresión física | D105A, D105B | Incluyen algunos actos concretos, no toda la formulación agregada. |
| Lesiones graves con atención médica | D110A-C | Hay ítems de dolor/moretones, lesiones y atención médica; deben validarse filtros y códigos antes de mapearlos. |
| Escalamiento en seis meses | Sin correspondencia exacta | No se observó un campo de aumento de frecuencia/intensidad en esa ventana. |
| Alcohol o sustancias | D113-D114 parcialmente | Miden consumo/frecuencia de alcohol de la pareja, no consumo problemático, drogas ni antecedentes. |
| Violencia ante ruptura/separación | D109 solo como contexto | Registra el inicio de ciertos hechos relativo a la unión/separación, no una reacción violenta ante ruptura. |
| Acoso persecutorio | D101E parcialmente | Saber dónde está no equivale a perseguir o acosar. |
| Amenazas graves | D103B y D103D parcialmente | Amenaza de daño o de irse/quitar hijos/ayuda económica; no identifica explícitamente muerte o suicidio. |
| Estrangulamiento o acceso a armas | D105E/F/G parcialmente | D105E incluye estrangulamiento o quemadura; F amenaza con arma; G agresión con arma. El acceso a armas no es medido. |

## Desenlace retrospectivo disponible y fuga de información

El diccionario identifica D107 como indicador derivado de D105E, D105F o D105G (violencia severa reportada). Si se usa D107 como etiqueta experimental, se deben excluir D105E/F/G y los agregados D106/D107/D108 de las entradas. El objetivo mide reporte retrospectivo; no mide feminicidio futuro.

Recuentos observados para D107:

- Código `0`: 17,965.
- Código `1`: 1,306.
- Código `<blank>`: 13,429.

### Diagnóstico de viabilidad

| Objetivo | Estado | Motivo |
|---|---|---|
| A. Clasificar violencia reportada retrospectivamente | APTO CON LIMITACIONES | D104/D106/D107/D108 son indicadores observados; para D107 hay que excluir sus componentes y agregados relacionados de las entradas. La salida solo describiría reporte retrospectivo. |
| B. Estimar indicadores de severidad reportada | APTO CON LIMITACIONES | Es un análisis descriptivo de la definición de la encuesta, no una herramienta clínica ni una predicción futura. |
| C. Predecir violencia grave o feminicidio futuro | NO APTO con ENDES 2025 sola | No hay etiqueta individual posterior ni horizonte temporal de seguimiento en este módulo transversal. |

## Diseño muestral e interpretación

La ficha ENDES 2025 describe una muestra probabilística bietápica, estratificada e independiente. `V021` (conglomerado), `V022` (estrato), `V005` (factor general de ponderación de mujeres) y `V044` (selección/resultado del módulo de violencia) se enlazan desde REC0111. La ficha técnica del indicador ENDES 2024 de violencia física/sexual especifica `V005/1,000,000` para las estimaciones (con excepción de 2020, cuando indica `V005V`); el informe PPR del primer semestre de 2025 también documenta el uso de `V005` en indicadores de mujeres y publica indicadores de violencia. Sin embargo, no se encontró una especificación 2025 que documente el ajuste del factor para el objetivo retrospectivo D107 y la elegibilidad exacta `V044=1`. Este experimento conserva métricas sin ponderar y no las presenta como estimaciones poblacionales; una evaluación ponderada con V005 debe presentarse separadamente como sensibilidad metodológica, no como inferencia oficial validada.
La existencia histórica de `V005V` en 2020 y su ausencia en REC84DV 2025 no prueba por sí sola que el peso V005 sea inadecuado en 2025: la ficha ENDES 2024 de violencia indica V005 salvo para 2020. Se requiere confirmar el año/versión y universo exactos antes de atribuir representatividad poblacional a métricas de D107.

## Interpretación y límites

- Entre las filas con D107 observado y V044=1, D114 está vacío en D113=`0`: 3,261/3,261; D113=`1`: 0/16,010. En el extracto ENDES 2025, el vacío de frecuencia se conserva como categoría faltante; no se recodifica a 'Nunca'. La API experimental acepta D114 nulo únicamente cuando D113=0, según este patrón observado.
- Entre las filas con V044=1, V501=0/V502=0 concentra 3,782 registros, de los cuales 3,782 tienen D107 vacío. Los estados V501=1:5/V502=1:2 tienen 0 vacíos. El diccionario local describe V502=0 como 'Nunca casada'. El cuestionario individual ENDES 2025 (páginas impresas 70–72) dirige 'Nunca casada' desde 1001 a 1002, pregunta por el último esposo/compañero en 1005 y pasa de 1006 a 1009 si no hubo respuestas afirmativas; esas páginas no muestran un salto explícito desde 1001 que por sí solo explique D107 vacío. El patrón es consistente con un universo condicionado por relación de pareja, pero el motivo exacto debe confirmarse contra la sintaxis/universo oficial de construcción de REC84DV; no se recodifica como negativo.
- La tabla de violencia REC84DV no contiene las variables de diseño ni un peso específico visible. La auditoría enlaza V021, V022, V005 y V044 desde REC0111 por CASEID y reporta solo recuentos. V044 distingue mujeres no seleccionadas, seleccionadas/entrevistadas, sin privacidad y no entrevistadas; los vacíos de D107 no se convierten en negativos.
- V501 y V502 provienen de RE516171 y solo se usan para describir la asociación con D107 no observado; no son predictores ni filtros de inclusión.
- La ENDES 2025 es una encuesta transversal. Con este módulo no se observa una etiqueta de feminicidio posterior ni un horizonte temporal; no sustenta predicción futura de feminicidio.
- La compatibilidad entre las doce preguntas de la aplicación y los ítems ENDES es parcial y requiere revisión del instrumento. Este informe no recodifica ni renombra esas preguntas.
- No se entrenó ningún modelo en esta auditoría.

## Reproducción

Desde `sistema-feminicidio/ml-service`: `python .\scripts\audit_endes.py`.
La salida contiene agregados y reemplaza este informe. Los ZIP originales se leen en memoria y no se extraen.

## Fuentes metodológicas

- INEI, catálogo oficial de microdatos: <https://proyectos.inei.gob.pe/microdatos/>.
- Plataforma Nacional de Datos Abiertos, conjunto ENDES 2025 publicado por INEI: <https://www.datosabiertos.gob.pe/dataset/encuesta-demogr%C3%A1fica-y-de-salud-familiar-endes-2025-instituto-nacional-de-estad%C3%ADstica-e-5>.
- INEI, ENDES 2025, informe preliminar y ficha técnica: <https://proyectos.inei.gob.pe/endes/2025/ppr/Informe_Indicadores_de_los_Programas_Presupuestales_ENDES_2025.pdf>.
- INEI, ENDES 2025, archivo local `2025.zip`, módulo `1036-Modulo1637.zip`: contiene `Diccionario - REC84DV.pdf` y `CUESTIONARIO INDIVIDUAL ENDES 2025.pdf` (sección 10, páginas impresas 70–72).
- INEI, ficha técnica del Indicador 37 ENDES 2024 (especifica V005/1,000,000, salvo el año 2020): <https://proyectos.inei.gob.pe/endes/2024/ppr2/Indicadores_de_Resultados_de_los_Programas_Presupuestales_ENDES_2024.pdf>.
- INEI, indicadores ENDES primer semestre 2025 (ficha de factores muestrales de mujeres y resultados de violencia): <https://proyectos.inei.gob.pe/endes/2025/ppr2/Indicadores_de_Resultados_de_los_Programas_Presupuestales_ENDES_Primer_Semestre_2025.pdf>.
- INEI, Informe Principal ENDES 2020 (documenta el factor excepcional V005V para 2020): <https://encuestas.inei.gob.pe/endes/2020/INFORME_PRINCIPAL_2020/INFORME_PRINCIPAL_ENDES_2020.pdf>.
- Diccionario CSV local incluido en `Proyecto_Feminicidio_Datos/02_Diccionarios/` para los nombres y códigos del archivo 2025.
