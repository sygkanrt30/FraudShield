package ru.yanin.ingress_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yanin.ingress_service.model.dto.TransactionRequest;
import ru.yanin.ingress_service.model.dto.TransactionResponse;
import ru.yanin.ingress_service.model.entity.Status;
import ru.yanin.ingress_service.service.TransactionIngressService;

/**
 * @author Vyacheslav Yanin
 */
@Validated
@RequiredArgsConstructor
@RestController
public class IngressPoint {

    private final TransactionIngressService ingress;

    @PostMapping("${server.url}")
    public ResponseEntity<?> receiveTransaction(@RequestBody @Valid TransactionRequest request) {
        ingress.receive(request);
        var response = new TransactionResponse(request.transactionId(), Status.PENDING.name());
        return ResponseEntity.ok(response);
    }
}
