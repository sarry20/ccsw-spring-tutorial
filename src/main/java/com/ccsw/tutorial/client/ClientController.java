package com.ccsw.tutorial.client;

import com.ccsw.tutorial.client.model.ClientDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Client", description = "API for managing clients")
@RestController()
@RequestMapping(value = "/client")
@CrossOrigin(origins = "*")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Operation(summary = "Get Clients", description = "Method that returns a list of clients")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<ClientDto> getClients() {
        return this.clientService.findAll().stream().map(client -> objectMapper.convertValue(client, ClientDto.class)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Operation(summary = "Save or Update Client", description = "Method that saves or updates a client")
    @RequestMapping(path = { "", "/{id}" }, method = RequestMethod.PUT)
    public void saveClient(@PathVariable(name = "id", required = false) Long id, @org.springframework.web.bind.annotation.RequestBody ClientDto clientDto) {
        this.clientService.save(id, clientDto);
    }

    /**
     * {@inheritDoc}
     */
    @Operation(summary = "Delete Client", description = "Method that deletes a client by its ID")
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public void deleteClient(@PathVariable("id") Long id) throws Exception {
        this.clientService.delete(id);
    }

}
