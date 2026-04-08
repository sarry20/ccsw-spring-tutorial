package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.client.ClientService;
import com.ccsw.tutorial.client.model.Client;
import com.ccsw.tutorial.client.model.ClientDto;
import com.ccsw.tutorial.common.pagination.PageableRequest;
import com.ccsw.tutorial.game.GameService;
import com.ccsw.tutorial.game.model.Game;
import com.ccsw.tutorial.game.model.GameDto;
import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanTest {

    private static final Long EXISTS_LOAN_ID = 1L;
    private static final Long NOT_EXISTS_LOAN_ID = 0L;
    private static final Long GAME_ID = 2L;
    private static final Long CLIENT_ID = 3L;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private GameService gameService;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    public void findAllShouldReturnAllLoans() {

        List<Loan> list = new ArrayList<>();
        list.add(new Loan());

        when(loanRepository.findAll()).thenReturn(list);

        List<Loan> loans = loanService.findAll();

        assertNotNull(loans);
        assertEquals(1, loans.size());
    }

    @Test
    public void getExistsLoanIdShouldReturnLoan() {

        Loan loan = new Loan();
        loan.setId(EXISTS_LOAN_ID);

        when(loanRepository.findById(EXISTS_LOAN_ID)).thenReturn(Optional.of(loan));

        Loan result = loanService.get(EXISTS_LOAN_ID);

        assertNotNull(result);
        assertEquals(EXISTS_LOAN_ID, result.getId());
    }

    @Test
    public void getNotExistsLoanIdShouldReturnNull() {

        when(loanRepository.findById(NOT_EXISTS_LOAN_ID)).thenReturn(Optional.empty());

        Loan result = loanService.get(NOT_EXISTS_LOAN_ID);

        assertNull(result);
    }

    @Test
    public void findPageShouldReturnPageOfLoans() {

        LoanSearchDto searchDto = new LoanSearchDto();
        searchDto.setPageable(new PageableRequest(0, 5));

        Page<Loan> expectedPage = new PageImpl<>(List.of(new Loan()), PageRequest.of(0, 5), 1);
        Date filterDate = new Date();

        when(loanRepository.findAll(any(), eq(searchDto.getPageable().getPageable()))).thenReturn(expectedPage);

        Page<Loan> result = loanService.findPage("Game", "Client", filterDate, searchDto);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(loanRepository).findAll(any(), eq(searchDto.getPageable().getPageable()));
    }

    @Test
    public void saveWithoutIdShouldInsertLoan() throws Exception {

        LoanDto dto = buildLoanDto(GAME_ID, CLIENT_ID, "2024-02-01", "2024-02-10");
        Game game = new Game();
        game.setId(GAME_ID);
        Client client = new Client();
        client.setId(CLIENT_ID);

        when(gameService.get(GAME_ID)).thenReturn(game);
        when(clientService.get(CLIENT_ID)).thenReturn(client);
        when(loanRepository.findAll(any())).thenReturn(Collections.emptyList(), Collections.emptyList());

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);

        loanService.save(null, dto);

        verify(loanRepository).save(loanCaptor.capture());

        Loan savedLoan = loanCaptor.getValue();
        assertEquals(GAME_ID, savedLoan.getGame().getId());
        assertEquals(CLIENT_ID, savedLoan.getClient().getId());
        assertEquals(dto.getStartDate(), savedLoan.getStartDate());
        assertEquals(dto.getEndDate(), savedLoan.getEndDate());
    }

    @Test
    public void saveWithStartDateAfterEndDateShouldThrowException() throws Exception {

        LoanDto dto = buildLoanDto(GAME_ID, CLIENT_ID, "2024-02-10", "2024-02-01");
        Game game = new Game();
        game.setId(GAME_ID);
        Client client = new Client();
        client.setId(CLIENT_ID);

        when(gameService.get(GAME_ID)).thenReturn(game);
        when(clientService.get(CLIENT_ID)).thenReturn(client);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loanService.save(null, dto));

        assertEquals("La fecha de inicio no puede ser posterior a la fecha de fin", exception.getMessage());
    }

    @Test
    public void saveWithMoreThanFourteenDaysShouldThrowException() throws Exception {

        LoanDto dto = buildLoanDto(GAME_ID, CLIENT_ID, "2024-02-01", "2024-02-20");
        Game game = new Game();
        game.setId(GAME_ID);
        Client client = new Client();
        client.setId(CLIENT_ID);

        when(gameService.get(GAME_ID)).thenReturn(game);
        when(clientService.get(CLIENT_ID)).thenReturn(client);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loanService.save(null, dto));

        assertEquals("La duración del préstamo no puede ser superior a 14 días", exception.getMessage());
    }

    @Test
    public void saveWithOverlappingGameLoanShouldThrowException() throws Exception {

        LoanDto dto = buildLoanDto(GAME_ID, CLIENT_ID, "2024-02-01", "2024-02-10");
        Game game = new Game();
        game.setId(GAME_ID);
        Client client = new Client();
        client.setId(CLIENT_ID);

        when(gameService.get(GAME_ID)).thenReturn(game);
        when(clientService.get(CLIENT_ID)).thenReturn(client);
        when(loanRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Loan>>any())).thenReturn(List.of(new Loan()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loanService.save(null, dto));

        assertEquals("El juego ya está prestado en las fechas seleccionadas", exception.getMessage());
    }

    @Test
    public void saveWithOverlappingClientLoanShouldThrowException() throws Exception {

        LoanDto dto = buildLoanDto(GAME_ID, CLIENT_ID, "2024-02-01", "2024-02-10");
        Game game = new Game();
        game.setId(GAME_ID);
        Client client = new Client();
        client.setId(CLIENT_ID);

        when(gameService.get(GAME_ID)).thenReturn(game);
        when(clientService.get(CLIENT_ID)).thenReturn(client);
        when(loanRepository.findAll(any())).thenReturn(Collections.emptyList(), List.of(new Loan()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> loanService.save(null, dto));

        assertEquals("El cliente ya tiene un préstamo activo en las fechas seleccionadas", exception.getMessage());
    }

    @Test
    public void deleteShouldRemoveLoan() {

        loanService.delete(EXISTS_LOAN_ID);

        verify(loanRepository).deleteById(EXISTS_LOAN_ID);
    }

    private LoanDto buildLoanDto(Long gameId, Long clientId, String startDate, String endDate) throws ParseException {

        LoanDto dto = new LoanDto();

        GameDto gameDto = new GameDto();
        gameDto.setId(gameId);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(clientId);

        dto.setGame(gameDto);
        dto.setClient(clientDto);
        dto.setStartDate(parseDate(startDate));
        dto.setEndDate(parseDate(endDate));

        return dto;
    }

    private Date parseDate(String value) throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setLenient(false);
        return formatter.parse(value);
    }
}

