package ru.yanin.system_ingress.model.dto.mapper;

import io.micrometer.core.instrument.Timer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.yanin.system_ingress.model.dto.TransactionRequest;
import ru.yanin.system_ingress.model.dto.event.TransactionEventWithTimer;
import ru.yanin.system_ingress.model.entity.TransactionRecord;
import ru.yanin.shared.domain.ClientDto;
import ru.yanin.shared.domain.Currency;
import ru.yanin.shared.domain.TransactionEvent;

import java.time.Instant;

/**
 * @author Vyacheslav Yanin
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionEventMapper {

    @Mapping(target = "from", expression = "java(mapFrom(request))")
    @Mapping(target = "to", expression = "java(mapTo(request))")
    @Mapping(target = "currency", expression = "java(toCurrency(request.currency()))")
    @Mapping(target = "createdAt", expression = "java(getCreatedAt())")
    TransactionEvent toTransactionEvent(TransactionRequest request);

    @Mapping(target = "transactionEvent.from", expression = "java(mapFrom(transactionRecord))")
    @Mapping(target = "transactionEvent.to", expression = "java(mapTo(transactionRecord))")
    @Mapping(target = "kafkaTimer", source = "sample")
    @Mapping(target = "transactionEvent.transactionId", source = "transactionRecord.transactionId")
    @Mapping(target = "transactionEvent.amount", source = "transactionRecord.amount")
    @Mapping(target = "transactionEvent.currency", source = "transactionRecord.currency")
    @Mapping(target = "transactionEvent.createdAt", source = "transactionRecord.createdAt")
    TransactionEventWithTimer toTransactionEventWithTimer(TransactionRecord transactionRecord, Timer.Sample sample);

    default ClientDto mapFrom(TransactionRequest request) {
        return new ClientDto(
                request.fromClientId(),
                request.fromFullName(),
                request.fromEmail()
        );
    }

    default ClientDto mapTo(TransactionRequest request) {
        return new ClientDto(
                request.toClientId(),
                request.toFullName(),
                request.toEmail()
        );
    }

    default ClientDto mapFrom(TransactionRecord record) {
        return new ClientDto(
                record.getFrom().getId(),
                record.getFrom().getFullName(),
                record.getFrom().getEmail()
        );
    }

    default ClientDto mapTo(TransactionRecord record) {
        return new ClientDto(
                record.getTo().getId(),
                record.getTo().getFullName(),
                record.getTo().getEmail()
        );
    }

    default Currency toCurrency(String currency) {
        return Currency.fromString(currency);
    }

    default Instant getCreatedAt() {
        return Instant.now();
    }
}