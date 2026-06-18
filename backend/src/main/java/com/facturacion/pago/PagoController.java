package com.facturacion.pago;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @PostMapping("/crear-preferencia")
    public String crearPreferencia(@RequestBody PlanRequest request) throws Exception {
        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(request.getPlanNombre())
                    .quantity(1)
                    .unitPrice(new BigDecimal(String.valueOf(request.getPrecio())))
                    .currencyId("PEN")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(PreferenceBackUrlsRequest.builder()
                            .success("http://localhost:4200/pago-exitoso")
                            .failure("http://localhost:4200/pago-fallido")
                            .pending("http://localhost:4200/pago-pendiente")
                            .build())
                    //.autoReturn("approved")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
            return preference.getInitPoint();
        } catch (com.mercadopago.exceptions.MPApiException e) {
            System.err.println("MP Error status: " + e.getStatusCode());
            System.err.println("MP Error body: " + e.getApiResponse().getContent());
            throw e;
        }
    }
}