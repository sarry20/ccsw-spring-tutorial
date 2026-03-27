package com.ccsw.tutorial.client;

import com.ccsw.tutorial.client.model.Client;
import com.ccsw.tutorial.client.model.ClientDto;

import java.util.List;

public interface ClientService {

    /**
     * Obtiene un cliente por su ID.
     * @param id ID del cliente a obtener.
     * @return Cliente encontrado o null si no existe.
     */
    Client get(Long id);

    /**
     * Obtiene la lista de todos los clientes.
     *
     * @return Lista de clientes.
     */
    List<Client> findAll();

    /**
     * Obtiene un cliente por su ID.
     *
     * @param id ID del cliente a obtener.
     * @return Cliente encontrado o null si no existe.
     */
    void save(Long id, ClientDto dto);

    /**
     * Elimina un cliente por su ID.
     *
     * @param id ID del cliente a eliminar.
     * @throws Exception Si ocurre un error durante la eliminación.
     */
    void delete(Long id) throws Exception;
}
