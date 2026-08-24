package ru.yanin.fraud_detector.service.neo4j;

import ru.yanin.fraud_detector.dto.PageRankResult;

import java.util.Set;

/**
 * @author Vyacheslav Yanin
 */
public interface HubProvider {

    Set<PageRankResult> getHubs();
}
