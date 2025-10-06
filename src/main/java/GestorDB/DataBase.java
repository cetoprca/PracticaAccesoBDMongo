package GestorDB;

public interface DataBase<T, R> {

    boolean insertOne(R data);

    boolean insertMany(R data);

    boolean removeOne(R data);

    boolean removeMany(R data);

    T find(R query);
}
