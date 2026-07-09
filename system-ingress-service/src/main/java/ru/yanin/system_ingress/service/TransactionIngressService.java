package ru.yanin.system_ingress.service;

import ru.yanin.system_ingress.model.dto.TransactionRequest;

/**
 * @author Vyacheslav Yanin
 */
public interface TransactionIngressService {

    void receive(TransactionRequest request);
}
