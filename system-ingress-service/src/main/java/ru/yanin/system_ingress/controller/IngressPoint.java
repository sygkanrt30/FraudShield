package ru.yanin.system_ingress.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yanin.system_ingress.model.dto.TransactionRequest;
import ru.yanin.system_ingress.model.dto.TransactionResponse;
import ru.yanin.system_ingress.model.entity.Status;
import ru.yanin.system_ingress.service.TransactionIngressService;

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
