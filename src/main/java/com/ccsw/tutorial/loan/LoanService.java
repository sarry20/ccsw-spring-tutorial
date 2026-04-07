package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface LoanService {

    /**
     * Obtiene un prestamo por su ID.
     * @param id ID del prestamo a obtener.
     * @return prestamo encontrado o null si no existe.
     */
    Loan get(Long id);

    /**
     * Obtiene la lista de todos los prestamos.
     *
     * @return Lista de prestamos.
     */
    List<Loan> findAll();

    /**
     * Obtiene una página de prestamos filtrados por nombre, cliente y fecha.
     * @param title Nombre del juego a buscar (opcional).
     * @param client Nombre del cliente a buscar (opcional).
     * @param date Fecha del prestamo a buscar (opcional).
     * @param dto DTO que contiene la información de paginación y ordenación.
     *
     * @return Página de prestamos que coinciden con los criterios de búsqueda.
     */
    Page<Loan> findPage(String title, String client, Date date, LoanSearchDto dto);

    /**
     * Obtiene un prestamo por su ID.
     *
     * @param id ID del prestamo a obtener.
     * @return prestamo encontrado o null si no existe.
     */
    void save(Long id, LoanDto dto);

    /**
     * Elimina un prestamo por su ID.
     *
     * @param id ID del prestamo a eliminar.
     */
    void delete(Long id);

}
