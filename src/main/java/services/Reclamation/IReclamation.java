package services.Reclamation;

import java.sql.SQLException;
import java.util.List;

public interface IReclamation<T> {

    void create(T t) throws SQLException;

    void update(T t) throws SQLException;

    void delete(int  t) throws SQLException;

    List<T> readAll() throws SQLException;

    void deleteAttachment(int attachmentId) throws SQLException;
    void deleteAttachmentsByReclamationId(int reclamationId) throws SQLException;



}
