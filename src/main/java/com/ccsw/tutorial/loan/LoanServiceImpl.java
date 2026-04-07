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
//        LoanSpecification dateSpec = new LoanSpecification(new SearchCriteria("date", "<>", date));

        Specification<Loan> spec = titleSpec.and(categorySpec); //.and(dateSpec);

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
        System.out.println("Loan: " + loan);
        this.loanRepository.save(loan);
    }

    @Override
    public void delete(Long id) {
        this.loanRepository.deleteById(id);
    }
}
