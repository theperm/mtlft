package uk.co.real_logic;

public interface Drainable<E>
{
    int drain(ElementHandler<E> elementHandler);
}
