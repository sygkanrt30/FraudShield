package ru.yanin.ingress_service.service;

import ru.yanin.ingress_service.model.dto.TransactionRequest;

/**
 * @author Vyacheslav Yanin
 */
public interface TransactionIngressService {

    void receive(TransactionRequest request);
}
