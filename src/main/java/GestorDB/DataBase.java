package GestorDB;

public interface DataBase<T, R, J> {

    boolean insertOne(J location, R data);

    boolean insertMany(J location, T data);

    boolean remove(J location, R data);

    T find(J location, R query);
}
