package feedme.sample;

public interface Populator<T> {
    void populate(T repository);
}
