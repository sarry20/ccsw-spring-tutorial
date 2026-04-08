package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.client.ClientService;
import com.ccsw.tutorial.common.criteria.SearchCriteria;
import com.ccsw.tutorial.game.GameService;
import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private ClientService clientService;

    @Override
    public Loan get(Long id) {
        return this.loanRepository.findById(id).orElse(null);
    }

    @Override
    public List<Loan> findAll() {
        return (List<Loan>) this.loanRepository.findAll();
    }

    @Override
    public Page<Loan> findPage(String title, String client, Date date, LoanSearchDto dto) {

        LoanSpecification titleSpec = new LoanSpecification(new SearchCriteria("game.title", ":", title));
        LoanSpecification categorySpec = new LoanSpecification(new SearchCriteria("client.name", ":", client));
        LoanSpecification startDateSpec = new LoanSpecification(new SearchCriteria("startDate", "<=", date));
        LoanSpecification endDateSpec = new LoanSpecification(new SearchCriteria("endDate", ">=", date));

        Specification<Loan> spec = titleSpec.and(categorySpec).and(startDateSpec).and(endDateSpec);

        return this.loanRepository.findAll(spec, dto.getPageable().getPageable());
    }

    @Override
    public void save(Long id, LoanDto dto) {
        Loan loan;

        if (id == null) {
            loan = new Loan();
        } else {
            loan = this.get(id);
        }

        BeanUtils.copyProperties(dto, loan, "id", "game", "client");

        loan.setGame(this.gameService.get(dto.getGame().getId()));
        loan.setClient(this.clientService.get(dto.getClient().getId()));

        validate(loan);

        this.loanRepository.save(loan);
    }

    private void validate(Loan loan) {
        if (loan.getStartDate().after(loan.getEndDate())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        long days = loan.getEndDate().getTime() - loan.getStartDate().getTime();

        if (days > 14 * 24 * 60 * 60 * 1000) {
            throw new IllegalArgumentException("La duración del préstamo no puede ser superior a 14 días");
        }

        LoanSpecification gameSpec = new LoanSpecification(new SearchCriteria("game.id", ":", loan.getGame().getId()));
        LoanSpecification startDateSpec = new LoanSpecification(new SearchCriteria("startDate", "<=", loan.getEndDate()));
        LoanSpecification endDateSpec = new LoanSpecification(new SearchCriteria("endDate", ">=", loan.getStartDate()));
        Specification<Loan> loanSpec = gameSpec.and(startDateSpec).and(endDateSpec);

        List<Loan> activeLoans = this.loanRepository.findAll(loanSpec);

        if (!activeLoans.isEmpty()) {
            throw new IllegalArgumentException("El juego ya está prestado en las fechas seleccionadas");
        }

        LoanSpecification clientSpec = new LoanSpecification(new SearchCriteria("client.id", ":", loan.getClient().getId()));
        Specification<Loan> clientLoanSpec = clientSpec.and(startDateSpec).and(endDateSpec);
        List<Loan> clientActiveLoans = this.loanRepository.findAll(clientLoanSpec);

        if (!clientActiveLoans.isEmpty()) {
            throw new IllegalArgumentException("El cliente ya tiene un préstamo activo en las fechas seleccionadas");
        }
    }

    @Override
    public void delete(Long id) {
        this.loanRepository.deleteById(id);
    }
}
