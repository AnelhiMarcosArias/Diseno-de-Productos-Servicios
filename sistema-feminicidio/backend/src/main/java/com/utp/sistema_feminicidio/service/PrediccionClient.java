package com.utp.sistema_feminicidio.service;

import com.utp.sistema_feminicidio.dto.PrediccionRequest;
import com.utp.sistema_feminicidio.dto.PrediccionResponse;

/** Puerto para integrar posteriormente el servicio real de predicción. */
public interface PrediccionClient {
    PrediccionResponse predecir(PrediccionRequest solicitud);
}
