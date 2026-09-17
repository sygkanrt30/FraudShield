package ru.yanin.shared.producer;

/**
 * @author Vyacheslav Yanin
 */
public interface Producer<T> {

    void sendEvent(T data);
}