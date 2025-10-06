package GestorDB;

import javax.swing.text.Document;

public class MongoDB implements DataBase<Document, Document>{

    @Override
    public boolean insertOne(Document data) {
        return false;
    }

    @Override
    public boolean insertMany(Document data) {
        return false;
    }

    @Override
    public boolean removeOne(Document data) {
        return false;
    }

    @Override
    public boolean removeMany(Document data) {
        return false;
    }

    @Override
    public Document find(Document query) {
        return null;
    }
}
